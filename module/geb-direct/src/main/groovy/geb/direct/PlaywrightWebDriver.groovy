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
import com.microsoft.playwright.options.WaitForSelectorState
import org.openqa.selenium.*
import org.openqa.selenium.interactions.Interactive
import org.openqa.selenium.interactions.Sequence
import org.openqa.selenium.logging.Logs

/**
 * A single-threaded WebDriver facade over one Playwright BrowserContext.
 * Do not share an instance between threads.
 */
@SuppressWarnings(['PublicMethodsBeforeNonPublicMethods', 'NoDouble', 'EmptyMethod', 'StaticMethodsBeforeInstanceMethods'])
class PlaywrightWebDriver implements WebDriver, JavascriptExecutor, TakesScreenshot, HasCapabilities, Interactive {
    private static final int ELEMENT_CENTER_DIVISOR = 2

    final PlaywrightSession session
    private Page currentPage
    private com.microsoft.playwright.Frame currentFrame
    private double pointerX
    private double pointerY
    private final Map<Page, String> pageHandles = [:]
    private long nextHandle = 1
    private final PlaywrightNavigation navigation
    private final PlaywrightTargetLocator targetLocator
    private final PlaywrightCookieJar cookieJar
    private final PlaywrightWindow window
    private final PlaywrightTimeouts timeouts
    private final PlaywrightNetwork network
    private final PlaywrightTracing tracing
    private final PlaywrightLocators locators
    private final PlaywrightWait wait
    private final PlaywrightKeyboard keyboard
    private final PlaywrightMouse mouse
    private final PlaywrightTouchscreen touchscreen
    private final PlaywrightStorage storage
    private final PlaywrightEmulation emulation
    private final PlaywrightDownloads downloads
    private final PlaywrightHar har
    private final PlaywrightConsole console
    private final PlaywrightApiClient apiClient
    private final PlaywrightCoverage coverage
    private final PlaywrightPdf pdf
    private final PlaywrightAccessibility accessibility
    private final PlaywrightVideoSupport video

    PlaywrightWebDriver(PlaywrightOptions options = new PlaywrightOptions()) {
        session = new PlaywrightSession(options)
        session.start()
        currentPage = session.page
        registerPages()
        navigation = new PlaywrightNavigation(this)
        targetLocator = new PlaywrightTargetLocator(this)
        cookieJar = new PlaywrightCookieJar(this)
        window = new PlaywrightWindow(this)
        timeouts = new PlaywrightTimeouts(this)
        network = new PlaywrightNetwork(this)
        tracing = new PlaywrightTracing(this)
        locators = new PlaywrightLocators(this)
        wait = new PlaywrightWait(this)
        keyboard = new PlaywrightKeyboard(this)
        mouse = new PlaywrightMouse(this)
        touchscreen = new PlaywrightTouchscreen(this)
        storage = new PlaywrightStorage(this)
        emulation = new PlaywrightEmulation(this)
        downloads = new PlaywrightDownloads(this)
        har = new PlaywrightHar(this)
        console = new PlaywrightConsole(this)
        apiClient = new PlaywrightApiClient(this)
        coverage = new PlaywrightCoverage(this)
        pdf = new PlaywrightPdf(this)
        accessibility = new PlaywrightAccessibility(this)
        video = new PlaywrightVideoSupport(this)
    }

    Page getPage() { currentPage }
    BrowserContext getContext() { session.context }
    Browser getBrowser() { session.browser }
    Playwright getPlaywright() { session.playwright }
    PlaywrightNetwork getNetwork() { network }
    PlaywrightTracing getTracing() { tracing }
    PlaywrightLocators getLocators() { locators }
    PlaywrightWait getWait() { wait }
    PlaywrightKeyboard getKeyboard() { keyboard }
    PlaywrightMouse getMouse() { mouse }
    PlaywrightTouchscreen getTouchscreen() { touchscreen }
    PlaywrightStorage getStorage() { storage }
    PlaywrightEmulation getEmulation() { emulation }
    PlaywrightDownloads getDownloads() { downloads }
    PlaywrightHar getHar() { har }
    PlaywrightConsole getConsole() { console }
    PlaywrightApiClient getApiClient() { apiClient }
    PlaywrightCoverage getCoverage() { coverage }
    PlaywrightPdf getPdf() { pdf }
    PlaywrightAccessibility getAccessibility() { accessibility }
    PlaywrightVideoSupport getVideo() { video }
    com.microsoft.playwright.Frame getFrame() { currentFrame ?: currentPage.mainFrame() }
    void setCurrentPage(Page page) {
        currentPage = page
        currentFrame = null
        session.activatePage(page)
        registerPages()
    }
    void setCurrentFrame(com.microsoft.playwright.Frame frame) { currentFrame = frame }

    private void registerPages() { session.context.pages().each { page -> pageHandles.computeIfAbsent(page) { "playwright-${nextHandle++}".toString() } } }
    String handleFor(Page page) { registerPages(); pageHandles[page] }
    Page pageFor(String handle) { registerPages(); pageHandles.find { it.value == handle }?.key }

    void get(String url) { currentPage.navigate(url); currentFrame = null }
    String getCurrentUrl() { currentPage.url() }
    String getTitle() { currentPage.title() }
    List<WebElement> findElements(By by) {
        findElements(getFrame().locator(PlaywrightBy.selector(by)))
    }

    List<WebElement> findElements(Locator locator) {
        waitForElement(locator)
        locator.all().collect { new PlaywrightWebElement(this, it) }
    }

    WebElement findElement(By by) {
        def elements = findElements(by)
        if (elements.empty) {
            throw new NoSuchElementException("Unable to locate element: $by")
        }
        elements.first()
    }
    String getPageSource() { currentPage.content() }
    void close() {
        currentPage.close()
        registerPages()
        Page replacement = session.context.pages().find { !it.isClosed() }
        if (replacement) {
            setCurrentPage(replacement)
        }
    }
    void quit() {
        apiClient.close()
        session.close()
    }
    Set<String> getWindowHandles() { registerPages(); new LinkedHashSet<>(pageHandles.findAll { !it.key.isClosed() }.values()) }
    String getWindowHandle() { handleFor(currentPage) }
    TargetLocator switchTo() { targetLocator }
    Navigation navigate() { navigation }
    Options manage() { new Options(cookieJar, window, timeouts) }
    long getAlertTimeoutMillis() { timeouts.alertTimeoutMillis }

    void perform(Collection<Sequence> sequences) {
        List<Map<String, Object>> encodedSequences = sequences.collect { it.toJson() as Map<String, Object> }
        int ticks = encodedSequences.collect { ((it.actions ?: []) as List).size() }.max() ?: 0
        for (int tick = 0; tick < ticks; tick++) {
            encodedSequences.each { sequence ->
                List<Map<String, Object>> actions = sequence.actions as List<Map<String, Object>>
                if (tick < actions.size()) {
                    performAction(actions[tick])
                }
            }
        }
    }

    void resetInputState() { }

    Object executeScript(String script, Object... args) { evaluate(script, args) }

    Object executeAsyncScript(String script, Object... args) {
        evaluateAsync(script, args)
    }

    private Object evaluate(String script, Object[] args) {
        List values = args.collect { it instanceof PlaywrightWebElement ? it.handle() : it }
        getFrame().evaluate("args => (function() { ${script} }).apply(null, args)", values)
    }

    private Object evaluateAsync(String script, Object[] args) {
        List values = args.collect { it instanceof PlaywrightWebElement ? it.handle() : it }
        int timeoutMs = Math.max(timeouts.scriptTimeoutMillis, 0L) as int
        getFrame().evaluate("""async (args) => {
            const timeoutMs = ${timeoutMs};
            return await Promise.race([
                new Promise((resolve, reject) => {
                    const callback = (result) => resolve(result);
                    args.push(callback);
                    try {
                        (function() { ${script} }).apply(null, args);
                    } catch (error) {
                        reject(error);
                    }
                }),
                new Promise((_, reject) => setTimeout(
                    () => reject(new Error('script timeout after ' + timeoutMs + 'ms')),
                    timeoutMs
                ))
            ]);
        }""", values)
    }

    private void waitForElement(Locator locator) {
        if (timeouts.implicitWaitMillis <= 0) {
            return
        }
        try {
            locator.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(timeouts.implicitWaitMillis))
        } catch (PlaywrightException ignored) {
        }
    }

    private void performAction(Map<String, Object> action) {
        switch (action.type) {
            case 'pointerMove':
                movePointer(action)
                break
            case 'pointerDown':
                mouse.down()
                break
            case 'pointerUp':
                mouse.up()
                break
            case 'keyDown':
                keyboard.down(playwrightKey(action.value as String))
                break
            case 'keyUp':
                keyboard.up(playwrightKey(action.value as String))
                break
        }
    }

    private void movePointer(Map<String, Object> action) {
        double x = (action.x ?: 0) as double
        double y = (action.y ?: 0) as double
        Object origin = action.origin
        if (origin instanceof PlaywrightWebElement) {
            def box = ((PlaywrightWebElement) origin).boundingBox
            pointerX = box.x + (box.width / ELEMENT_CENTER_DIVISOR) + x
            pointerY = box.y + (box.height / ELEMENT_CENTER_DIVISOR) + y
        } else if (origin == 'pointer') {
            pointerX += x
            pointerY += y
        } else {
            pointerX = x
            pointerY = y
        }
        mouse.move(pointerX, pointerY)
    }

    private static String playwrightKey(String value) {
        Keys key = value?.length() == 1 ? Keys.getKeyFromUnicode(value.charAt(0)) : null
        key ? PlaywrightWebElement.playwrightKey(key) : value
    }
    <X> X getScreenshotAs(OutputType<X> target) { target.convertFromPngBytes(currentPage.screenshot()) }
    Capabilities getCapabilities() { new ImmutableCapabilities([browserName: "playwright-${session.options.browserType}", browserVersion: session.browser.version()]) }

    private static class Options implements WebDriver.Options {
        final PlaywrightCookieJar cookieJar
        final PlaywrightWindow window
        final PlaywrightTimeouts timeouts
        Options(PlaywrightCookieJar cookieJar, PlaywrightWindow window, PlaywrightTimeouts timeouts) { this.cookieJar = cookieJar; this.window = window; this.timeouts = timeouts }
        void addCookie(Cookie cookie) { cookieJar.addCookie(cookie) }
        void deleteCookieNamed(String name) { cookieJar.deleteCookieNamed(name) }
        void deleteCookie(Cookie cookie) { cookieJar.deleteCookie(cookie) }
        void deleteAllCookies() { cookieJar.deleteAllCookies() }
        Set<Cookie> getCookies() { cookieJar.cookies }
        Cookie getCookieNamed(String name) { cookieJar.getCookieNamed(name) }
        WebDriver.Timeouts timeouts() { timeouts }
        WebDriver.Window window() { window }
        Logs logs() { throw new UnsupportedOperationException('Playwright does not expose Selenium logs') }
    }
}
