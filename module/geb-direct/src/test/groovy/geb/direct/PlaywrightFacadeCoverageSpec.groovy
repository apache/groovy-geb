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

import com.sun.net.httpserver.HttpServer
import geb.Browser
import geb.direct.report.PlaywrightTraceReporter
import geb.direct.support.PlaywrightSpecSupport
import geb.report.ReportState
import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.OutputType
import org.openqa.selenium.WindowType
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

@IgnoreIf({ PlaywrightSpecSupport.skipped() })
class PlaywrightFacadeCoverageSpec extends Specification {
    @Shared HttpServer server
    @Shared String baseUrl
    @Shared Browser browser
    @Shared PlaywrightWebDriver driver

    def setupSpec() {
        server = PlaywrightSpecSupport.server { String path, headers -> pageFor(path) }
        baseUrl = PlaywrightSpecSupport.url(server, '')
        driver = PlaywrightSpecSupport.driver {
            tracing = true
            hasTouch = true
        }
        browser = new Browser()
        browser.driver = driver
    }

    def cleanupSpec() {
        browser?.quit()
        server?.stop(0)
    }

    def 'browser support exposes every facade and trace reporter writes a fresh trace'() {
        given:
        Path reportDirectory = Files.createTempDirectory('geb-direct-report-')
        def reporter = new PlaywrightTraceReporter()

        when:
        def facades = [
            PlaywrightBrowserSupport.page(browser),
            PlaywrightBrowserSupport.context(browser),
            PlaywrightBrowserSupport.network(browser),
            PlaywrightBrowserSupport.tracing(browser),
            PlaywrightBrowserSupport.locators(browser),
            PlaywrightBrowserSupport.wait(browser),
            PlaywrightBrowserSupport.keyboard(browser),
            PlaywrightBrowserSupport.mouse(browser),
            PlaywrightBrowserSupport.touchscreen(browser),
            PlaywrightBrowserSupport.storage(browser),
            PlaywrightBrowserSupport.emulation(browser),
            PlaywrightBrowserSupport.downloads(browser),
            PlaywrightBrowserSupport.har(browser),
            PlaywrightBrowserSupport.console(browser),
            PlaywrightBrowserSupport.apiClient(browser),
            PlaywrightBrowserSupport.coverage(browser),
            PlaywrightBrowserSupport.pdf(browser),
            PlaywrightBrowserSupport.accessibility(browser),
            PlaywrightBrowserSupport.video(browser)
        ]
        reporter.writeReport(new ReportState(browser, 'facade trace', reportDirectory.toFile()))

        then:
        PlaywrightBrowserSupport.driver(browser).is(driver)
        PlaywrightBrowserSupport.driver(driver).is(driver)
        facades.every { it != null }
        Files.size(reportDirectory.resolve('facade trace.zip')) > 0
        driver.tracing.started

        cleanup:
        Files.deleteIfExists(reportDirectory.resolve('facade trace.zip'))
        Files.deleteIfExists(reportDirectory)
    }

    def 'element, window, frame, cookie, and error facades use real Playwright pages'() {
        given:
        driver.get(featureUrl())
        def field = driver.findElement(By.id('field'))
        def checkbox = driver.findElement(By.id('check'))
        def select = driver.findElement(By.id('select'))
        def source = driver.findElement(By.id('source'))
        def target = driver.findElement(By.id('target'))
        Path upload = PlaywrightSpecSupport.tempFile('geb-direct-element-', '.txt')

        when:
        field.hover()
        field.focus()
        field.press('End')
        field.sendKeys('Geb')
        driver.findElement(By.id('double')).doubleClick()
        checkbox.check()
        checkbox.uncheck()
        checkbox.check()
        select.selectOption('two')
        source.dragTo(target)
        field.scrollIntoViewIfNeeded()
        driver.findElement(By.id('upload')).inputFiles = [upload.toString()] as String[]
        driver.touchscreen.tap(4, 4)
        field.focus()
        String activeElementId = driver.switchTo().activeElement().getAttribute('id')
        String fieldValue = field.inputValue
        boolean checked = checkbox.checked
        String selectedValue = select.getAttribute('value')
        String doubleClickCount = driver.findElement(By.id('double')).text
        String uploadValue = driver.findElement(By.id('upload')).getAttribute('value')
        driver.manage().window().position = new org.openqa.selenium.Point(4, 5)
        driver.manage().window().maximize()
        driver.manage().window().minimize()
        driver.manage().window().fullscreen()
        driver.get(baseUrl)
        driver.manage().addCookie(new Cookie.Builder('domain-cookie', 'value').domain('localhost').path('/').isHttpOnly(true).expiresOn(new Date(System.currentTimeMillis() + 60000)).build())
        driver.manage().addCookie(new Cookie('url-cookie', 'value'))
        driver.get("$baseUrl/frames")
        driver.switchTo().frame(0)
        driver.switchTo().parentFrame()
        def parentHandle = driver.windowHandle
        driver.switchTo().newWindow(WindowType.TAB)
        def newWindowUrl = driver.currentUrl
        driver.switchTo().window(parentHandle)
        driver.get(featureUrl())

        then:
        activeElementId == 'field'
        fieldValue == 'Geb'
        field.innerHTML == ''
        driver.findElement(By.id('editable')).editable
        driver.findElement(By.id('hidden')).hidden
        checked
        selectedValue == 'two'
        doubleClickCount == '2'
        uploadValue.contains(upload.fileName.toString())
        driver.manage().window().position == new org.openqa.selenium.Point(4, 5)
        driver.manage().getCookieNamed('domain-cookie').httpOnly
        driver.manage().getCookieNamed('url-cookie').value == 'value'
        newWindowUrl == 'about:blank'

        when:
        driver.findElement(By.id('missing'))

        then:
        thrown(NoSuchElementException)

        when:
        driver.findElements(new By() {
            @SuppressWarnings('UnusedMethodParameter')
            List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.SearchContext context) { [] }
            String toString() { 'By.unsupported: value' }
        })

        then:
        thrown(IllegalArgumentException)

        when:
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(10))
        driver.switchTo().alert()

        then:
        thrown(NoAlertPresentException)

        cleanup:
        Files.deleteIfExists(upload)
    }

    def 'remaining page and context wait variants return their Playwright events'() {
        given:
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30))
        driver.get(featureUrl())

        when:
        driver.wait.waitForLoadState('domcontentloaded')
        driver.wait.waitForURL(featureUrl())
        def selector = driver.wait.waitForSelector('#field')
        def function = driver.wait.waitForFunction('() => document.readyState === "complete"')
        driver.wait.waitForTimeout(1)
        def request = driver.wait.waitForRequest({ it.url().endsWith('/predicate-request') }) { driver.get("$baseUrl/predicate-request") }
        def response = driver.wait.waitForResponse("$baseUrl/url-response") { driver.get("$baseUrl/url-response") }
        driver.get(featureUrl())
        def popup = driver.wait.waitForPopup { driver.page.evaluate('() => window.open("about:blank")') }
        popup.close()
        def download = driver.wait.waitForDownload { driver.findElement(By.id('download')).click() }
        def console = driver.wait.waitForConsoleMessage { driver.findElement(By.id('console')).click() }
        def contextConsole = driver.wait.waitForContextEvent('consolemessage') { driver.findElement(By.id('console')).click() }
        def contextPage = driver.wait.waitForContextEvent('page') { driver.page.evaluate('() => window.open("about:blank")') }
        contextPage.close()

        then:
        selector != null
        function != null
        request.url().endsWith('/predicate-request')
        response.url().endsWith('/url-response')
        download.suggestedFilename() == 'facade.txt'
        console.text().contains('facade-console')
        contextConsole.text().contains('facade-console')

        when:
        driver.wait.waitForEvent('unsupported') { }

        then:
        thrown(UnsupportedOperationException)
    }

    def 'wait, network, download, PDF, accessibility, video, HAR, and coverage helpers execute their paths'() {
        given:
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30))
        driver.get(featureUrl())
        Path download = Files.createTempFile('geb-direct-download-', '.txt')
        Path pdf = Files.createTempFile('geb-direct-pdf-', '.pdf')

        when:
        def request = driver.wait.waitForRequest("$baseUrl/request") { driver.get("$baseUrl/request") }
        def response = driver.wait.waitForResponse({ it.url().endsWith('/response') }) { driver.get("$baseUrl/response") }
        def navigation = driver.wait.waitForNavigation { driver.get("$baseUrl/navigation") }
        driver.get(featureUrl())
        def chooser = driver.wait.waitForFileChooser { driver.findElement(By.id('upload')).click() }
        def console = driver.wait.waitForEvent('consolemessage') { driver.findElement(By.id('console')).click() }
        def downloaded = driver.downloads.waitForDownload { driver.findElement(By.id('download')).click() }
        driver.downloads.saveAs(downloaded, download)
        driver.network.route('**/fulfilled') { route -> driver.network.fulfill(route, 201, '<title>fulfilled</title>') }
        driver.get("$baseUrl/fulfilled")
        driver.network.unroute('**/fulfilled')
        driver.network.route('**/continued') { route -> driver.network.continueRequest(route) }
        driver.get("$baseUrl/continued")
        driver.network.unroute('**/continued')
        driver.network.route('**/resumed') { route -> driver.network.resume(route, ['X-Direct': 'resumed']) }
        driver.get("$baseUrl/resumed")
        driver.network.unroute('**/resumed')
        driver.har.unrouteFromHAR(Path.of('unused.har'))
        byte[] pdfBytes = driver.pdf.bytes()
        driver.pdf.saveAs(pdf)
        def accessibility = driver.accessibility.snapshot()

        then:
        request.url().endsWith('/request')
        response.status() == 200
        navigation.url().endsWith('/navigation')
        chooser != null
        console.text().contains('facade-console')
        Files.size(download) > 0
        driver.title == '/resumed'
        pdfBytes.length > 0
        Files.size(pdf) > 0
        accessibility.contains('button')

        cleanup:
        driver.network.unroute('**/fulfilled')
        driver.network.unroute('**/continued')
        driver.network.unroute('**/resumed')
        Files.deleteIfExists(download)
        Files.deleteIfExists(pdf)
    }

    def 'coverage facade reports unavailable Playwright Java coverage explicitly'() {
        when:
        driver.coverage.startJSCoverage()

        then:
        thrown(UnsupportedOperationException)
    }

    def 'element geometry, screenshot, submission, and missing-child paths use live locators'() {
        given:
        driver.get(featureUrl())
        def field = driver.findElement(By.id('field'))
        def container = driver.findElement(By.id('container'))

        when:
        def boundingBox = field.boundingBox
        def location = field.location
        def size = field.size
        def rectangle = field.rect
        def screenshot = field.getScreenshotAs(OutputType.BYTES)
        def cssDisplay = field.getCssValue('display')
        driver.findElement(By.id('submit')).submit()

        then:
        field.count == 1
        boundingBox != null
        location.x >= 0
        size.width > 0
        rectangle.width == size.width
        screenshot.length > 0
        cssDisplay
        driver.title == 'submitted'
        container.findElement(By.className('child')).text == 'nested'

        when:
        container.findElement(By.className('missing'))

        then:
        thrown(NoSuchElementException)
    }

    def 'API client performs post and fetch requests against the local server'() {
        when:
        def post = driver.apiClient.post("$baseUrl/api-post")
        def fetch = driver.apiClient.fetch("$baseUrl/api-fetch")

        then:
        post.status() == 200
        fetch.status() == 200
    }

    def 'session applies browser context option branches before it is closed'() {
        given:
        Path har = Files.createTempFile('geb-direct-', '.har')
        Files.deleteIfExists(har)
        Path videoDirectory = Files.createTempDirectory('geb-direct-video-')

        when:
        def configured = PlaywrightSpecSupport.driver {
            locale = 'en-US'
            timezoneId = 'UTC'
            userAgent = 'Geb Direct Coverage'
            permissions = ['geolocation']
            geolocation = [longitude: 1d, latitude: 2d, accuracy: 3d]
            extraHTTPHeaders = ['X-Direct': 'coverage']
            colorScheme = 'dark'
            reducedMotion = 'reduce'
            recordHarPath = har.toString()
            recordHarMode = 'minimal'
            httpCredentials = [username: 'user', password: 'password']
            proxy = [server: 'http://127.0.0.1:9', bypass: 'localhost', username: 'user', password: 'password']
            serviceWorkers = 'block'
            recordVideo = true
            videoDir = videoDirectory.toString()
        }
        Path recordedVideo = configured.video.path()
        configured.quit()

        then:
        Files.exists(har)
        Files.exists(recordedVideo)

        cleanup:
        Files.deleteIfExists(har)
        Files.deleteIfExists(recordedVideo)
        videoDirectory.toFile().deleteDir()
    }

    private static String pageFor(String path) {
        switch (path) {
            case '/frames':
                return '<iframe id="frame" src="/inner"></iframe>'
            case '/inner':
                return '<p id="inside">inside</p>'
            case '/download':
                return '<title>/download</title>'
        }
        """<!doctype html><html><head><title>${path}</title></head><body>
          <input id="field" onmouseenter="this.dataset.hovered='true'">
          <input id="check" type="checkbox"><select id="select"><option value="one">one</option><option value="two">two</option></select>
          <div id="container"><span class="child">nested</span></div>
          <form onsubmit="event.preventDefault(); document.title='submitted'"><button id="submit" type="submit">submit</button></form>
          <button id="double" ondblclick="this.textContent=Number(this.textContent)+1">1</button>
          <div id="source" draggable="true">source</div><div id="target">target</div>
          <div id="editable" contenteditable="true">editable</div><div id="hidden" hidden>hidden</div>
          <input id="upload" type="file"><button id="console" onclick="console.log('facade-console')">console</button>
          <a id="download" download="facade.txt" href="data:text/plain,geb-direct-download">download</a>
          <script>document.addEventListener('touchstart', () => document.body.dataset.touched = 'true')</script>
        </body></html>"""
    }

    private static String featureUrl() {
        "data:text/html;base64,${Base64.encoder.encodeToString(pageFor('/features').bytes)}"
    }
}
