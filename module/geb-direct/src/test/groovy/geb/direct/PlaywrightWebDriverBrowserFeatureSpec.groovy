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
import geb.Configuration
import org.opentest4j.TestAbortedException
import org.openqa.selenium.By
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.file.Files
import java.nio.file.Path

@IgnoreIf({ System.getProperty('geb.direct.playwright.skip') == 'true' || System.getenv('PLAYWRIGHT_SKIP') == 'true' })
class PlaywrightWebDriverBrowserFeatureSpec extends Specification {

    @Shared
    HttpServer server

    @Shared
    String baseUrl

    PlaywrightWebDriver driver
    Browser browser
    Path uploadFile

    def setupSpec() {
        server = HttpServer.create(new InetSocketAddress(0), 0)
        server.createContext('/') { exchange ->
            byte[] body = pageFor(exchange.requestURI.path).getBytes('UTF-8')
            exchange.responseHeaders.add('Content-Type', 'text/html; charset=UTF-8')
            exchange.sendResponseHeaders(200, body.length)
            exchange.responseBody.withCloseable { it.write(body) }
        }
        server.start()
        baseUrl = "http://localhost:${server.address.port}"
    }

    def cleanup() {
        browser?.quit()
        driver?.quit()
        if (uploadFile) {
            Files.deleteIfExists(uploadFile)
        }
    }

    def cleanupSpec() {
        server?.stop(0)
    }

    def 'switches nested frames by index, name, and frame element through Geb Browser'() {
        given:
        browser = new Browser(configuration())

        when:
        browser.go('/frames')
        def webDriver = PlaywrightBrowserSupport.driver(browser)

        then:
        browser.$('#main').text() == 'Main document'

        when:
        webDriver.switchTo().frame(0).switchTo().frame(0)

        then:
        webDriver.findElement(By.id('nested-message')).text == 'Nested frame'

        when:
        webDriver.switchTo().defaultContent().switchTo().frame('outer').switchTo().frame('inner')

        then:
        webDriver.findElement(By.id('nested-message')).text == 'Nested frame'

        when:
        webDriver.switchTo().defaultContent()
        def outerFrame = webDriver.findElement(By.id('outer'))
        webDriver.switchTo().frame(outerFrame).switchTo().frame('inner')

        then:
        webDriver.findElement(By.id('nested-message')).text == 'Nested frame'

        when:
        webDriver.switchTo().defaultContent()

        then:
        browser.$('#main').text() == 'Main document'
    }

    def 'switches to a popup window, closes it, and returns to its parent'() {
        given:
        driver = newDriver()
        driver.get("$baseUrl/windows")
        def parentHandle = driver.windowHandle

        when:
        def popup = driver.page.waitForPopup {
            driver.findElement(By.id('open-window')).click()
        }
        new PollingConditions(timeout: 5).eventually {
            assert driver.windowHandles.size() == 2
        }
        def childHandle = (driver.windowHandles - parentHandle).first()
        driver.switchTo().window(childHandle)

        then:
        popup.url().endsWith('/child')
        driver.title == 'Child window'
        driver.findElement(By.id('child-message')).text == 'Child window content'

        when:
        driver.close()
        driver.switchTo().window(parentHandle)

        then:
        driver.windowHandles == [parentHandle] as Set
        driver.title == 'Parent window'
    }

    def 'uploads an absolute local file path through a file input'() {
        given:
        driver = newDriver()
        uploadFile = Files.createTempFile('geb-direct-upload-', '.txt')
        Files.writeString(uploadFile, 'Playwright upload coverage')
        driver.get("$baseUrl/upload")

        when:
        def fileInput = driver.findElement(By.id('upload'))
        fileInput.sendKeys(uploadFile.toAbsolutePath().toString())

        then:
        (driver.executeScript('return arguments[0].value', fileInput) as String).contains(uploadFile.fileName.toString())
        driver.executeScript('return arguments[0].files[0].name', fileInput) == uploadFile.fileName.toString()
        driver.findElement(By.id('selected-file')).text == uploadFile.fileName.toString()
    }

    private Configuration configuration() {
        def configuration = new Configuration()
        configuration.baseUrl = baseUrl
        configuration.driverConf = PlaywrightDriver.config {
            headless = true
            browserType = 'chromium'
        }
        configuration.cacheDriver = false
        configuration
    }

    private PlaywrightWebDriver newDriver() {
        try {
            PlaywrightDriver.create {
                headless = true
                browserType = 'chromium'
            }
        } catch (Exception exception) {
            throw new TestAbortedException("Playwright Chromium is unavailable: ${exception.message}", exception)
        }
    }

    private String pageFor(String path) {
        switch (path) {
            case '/frames':
                return '''<!doctype html>
<html><head><title>Frames</title></head><body>
  <p id="main">Main document</p>
  <iframe id="outer" name="outer" src="/outer"></iframe>
</body></html>'''
            case '/outer':
                return '''<!doctype html>
<html><body><iframe id="inner" name="inner" src="/inner"></iframe></body></html>'''
            case '/inner':
                return '''<!doctype html>
<html><body><p id="nested-message">Nested frame</p></body></html>'''
            case '/windows':
                return '''<!doctype html>
<html><head><title>Parent window</title></head><body>
  <a id="open-window" href="/child" target="_blank">Open child</a>
</body></html>'''
            case '/child':
                return '''<!doctype html>
<html><head><title>Child window</title></head><body>
  <p id="child-message">Child window content</p>
</body></html>'''
            case '/upload':
                return '''<!doctype html>
<html><body>
  <input id="upload" type="file" onchange="document.getElementById('selected-file').textContent = this.files[0].name">
  <p id="selected-file"></p>
</body></html>'''
            default:
                return '<!doctype html><html><body>Not found</body></html>'
        }
    }
}
