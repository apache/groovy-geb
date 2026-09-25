/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package geb.direct

import com.microsoft.playwright.*
import com.microsoft.playwright.options.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.BooleanSupplier
import java.util.function.Consumer

@SuppressWarnings(['PublicMethodsBeforeNonPublicMethods', 'UnnecessarySetter'])
class PlaywrightSession implements Closeable {
    final PlaywrightOptions options
    Playwright playwright
    Browser browser
    BrowserContext context
    Page page
    final List<ConsoleMessage> consoleMessages = new CopyOnWriteArrayList<>()
    final List<String> pageErrors = new CopyOnWriteArrayList<>()
    final LinkedBlockingQueue<Dialog> dialogs = new LinkedBlockingQueue<>()
    private final Map<Page, Consumer<Dialog>> dialogHandlers = new ConcurrentHashMap<>()
    private Consumer<Page> pageHandler
    private boolean tracingStarted

    PlaywrightSession(PlaywrightOptions options) { this.options = options.copy() }

    void start() {
        playwright = Playwright.create()
        BrowserType type = ['chromium': playwright.chromium(), 'firefox': playwright.firefox(), 'webkit': playwright.webkit()][options.browserType]
        if (!type) {
            throw new IllegalArgumentException("Unsupported Playwright browser type: $options.browserType")
        }
        BrowserType.LaunchOptions launch = new BrowserType.LaunchOptions().setHeadless(options.headless).setSlowMo(options.slowMo)
        if (options.channel) {
            launch.setChannel(options.channel)
        }
        if (options.launchArgs) {
            launch.setArgs(options.launchArgs)
        }
        browser = type.launch(launch)
        Browser.NewContextOptions newContext = new Browser.NewContextOptions().setViewportSize(options.viewportWidth, options.viewportHeight).setIgnoreHTTPSErrors(options.ignoreHTTPSErrors)
            .setAcceptDownloads(options.acceptDownloads).setJavaScriptEnabled(options.javaScriptEnabled).setBypassCSP(options.bypassCSP)
            .setDeviceScaleFactor(options.deviceScaleFactor).setIsMobile(options.isMobile).setHasTouch(options.hasTouch)
        if (options.recordVideo) {
            newContext.setRecordVideoDir(Files.createDirectories(Path.of(options.videoDir)))
        }
        if (options.locale) {
            newContext.setLocale(options.locale)
        }
        if (options.timezoneId) {
            newContext.setTimezoneId(options.timezoneId)
        }
        if (options.userAgent) {
            newContext.setUserAgent(options.userAgent)
        }
        if (options.baseURL) {
            newContext.setBaseURL(options.baseURL)
        }
        if (options.geolocation) {
            Geolocation geolocation = new Geolocation(options.geolocation.longitude as double, options.geolocation.latitude as double)
            if (options.geolocation.accuracy != null) {
                geolocation.setAccuracy(options.geolocation.accuracy as double)
            }
            newContext.setGeolocation(geolocation)
        }
        if (options.extraHTTPHeaders) {
            newContext.setExtraHTTPHeaders(options.extraHTTPHeaders)
        }
        if (options.colorScheme) {
            newContext.setColorScheme(ColorScheme.valueOf(options.colorScheme.toUpperCase()))
        }
        if (options.reducedMotion) {
            newContext.setReducedMotion(ReducedMotion.valueOf(options.reducedMotion.toUpperCase()))
        }
        if (options.recordHarPath) {
            newContext.setRecordHarPath(Path.of(options.recordHarPath))
        }
        if (options.recordHarMode) {
            newContext.setRecordHarMode(HarMode.valueOf(options.recordHarMode.toUpperCase()))
        }
        if (options.httpCredentials) {
            newContext.setHttpCredentials(new HttpCredentials(options.httpCredentials.username, options.httpCredentials.password))
        }
        if (options.proxy) {
            Proxy proxy = new Proxy(options.proxy.server)
            if (options.proxy.bypass) {
                proxy.setBypass(options.proxy.bypass)
            }
            if (options.proxy.username) {
                proxy.setUsername(options.proxy.username)
            }
            if (options.proxy.password) {
                proxy.setPassword(options.proxy.password)
            }
            newContext.setProxy(proxy)
        }
        if (options.blockServiceWorkers || options.serviceWorkers?.equalsIgnoreCase('block')) {
            newContext.setServiceWorkers(ServiceWorkerPolicy.BLOCK)
        }
        context = browser.newContext(newContext)
        if (options.offline) {
            context.setOffline(true)
        }
        if (options.permissions) {
            context.grantPermissions(options.permissions)
        }
        context.setDefaultTimeout(options.defaultTimeoutMs)
        context.setDefaultNavigationTimeout(options.navigationTimeoutMs)
        page = context.newPage()
        attachPage(page)
        pageHandler = { Page newPage -> attachPage(newPage) } as Consumer<Page>
        context.onPage(pageHandler)
        if (options.tracing) {
            startTracing()
        }
    }

    void startTracing() {
        context.tracing().start(new Tracing.StartOptions().setScreenshots(options.screenshotsOnTrace).setSnapshots(options.snapshotsOnTrace).setSources(options.sourcesOnTrace))
        tracingStarted = true
    }

    Path stopTracing(Path target = null) {
        if (!tracingStarted) {
            return null
        }
        Path path = target ?: Files.createDirectories(Path.of(options.tracesDir)).resolve("trace-${System.currentTimeMillis()}.zip")
        context.tracing().stop(new Tracing.StopOptions().setPath(path))
        tracingStarted = false
        path
    }

    boolean isTracingStarted() { tracingStarted }

    private void attachPage(Page target) {
        if (dialogHandlers.containsKey(target)) {
            return
        }
        Consumer<Dialog> dialogHandler = { Dialog dialog -> dialogs.offer(dialog) } as Consumer<Dialog>
        dialogHandlers[target] = dialogHandler
        target.onDialog(dialogHandler)
        target.onClose { detachDialogHandler(target) }
        target.onConsoleMessage { ConsoleMessage message -> consoleMessages.add(message) }
        target.onPageError { String error -> pageErrors.add(error) }
    }

    Dialog waitForDialog(Page target, long timeoutMillis) {
        try {
            target.waitForCondition(
                { !dialogs.empty } as BooleanSupplier,
                new Page.WaitForConditionOptions().setTimeout(timeoutMillis)
            )
            dialogs.poll()
        } catch (PlaywrightException ignored) {
            null
        }
    }

    void activatePage(Page target) {
        dialogHandlers.keySet().findAll { it != target }.each { detachDialogHandler(it) }
        attachPage(target)
        dialogs.clear()
    }

    private void detachDialogHandler(Page target) {
        Consumer<Dialog> dialogHandler = dialogHandlers.remove(target)
        if (dialogHandler) {
            try { target.offDialog(dialogHandler) } catch (Exception ignored) { }
        }
    }

    void close() {
        try {
            if (pageHandler) {
                context?.offPage(pageHandler)
            }
        } catch (Exception ignored) { }
        dialogHandlers.keySet().toList().each { detachDialogHandler(it) }
        dialogs.clear()
        try {
            if (tracingStarted) {
                stopTracing()
            }
        } catch (Exception ignored) { }
        try { page?.close() } catch (Exception ignored) { }
        try { context?.close() } catch (Exception ignored) { }
        try { browser?.close() } catch (Exception ignored) { }
        try { playwright?.close() } catch (Exception ignored) { }
    }
}
