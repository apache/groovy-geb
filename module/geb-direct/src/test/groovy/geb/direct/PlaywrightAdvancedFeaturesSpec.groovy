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
import geb.direct.support.PlaywrightSpecSupport
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.file.Files
import java.nio.file.Path

@IgnoreIf({ PlaywrightSpecSupport.skipped() })
class PlaywrightAdvancedFeaturesSpec extends Specification {
    @Shared HttpServer server
    @Shared String baseUrl
    PlaywrightWebDriver driver

    def setupSpec() {
        server = PlaywrightSpecSupport.server { String path, headers ->
            if (path == '/header') {
                return "<title>${headers.getFirst('X-Direct')}</title>"
            }
            '''<!doctype html><html><head><title>Advanced</title></head><body>
              <input id="keyboard"><button id="console" onclick="console.log('direct-log')">Log</button>
              <button id="download" onclick="location.href='/file'">Download</button></body></html>'''
        }
        baseUrl = PlaywrightSpecSupport.url(server)
    }

    def cleanup() {
        driver?.quit()
    }

    def cleanupSpec() {
        server?.stop(0)
    }

    def 'wait, storage, emulation, keyboard, mouse, and console helpers use the active page'() {
        given:
        driver = PlaywrightSpecSupport.driver()
        driver.get(baseUrl)

        when:
        driver.wait.waitForLoadState('domcontentloaded')
        driver.wait.waitForSelector('#keyboard')
        driver.storage.local('local-key', 'local-value')
        driver.storage.session('session-key', 'session-value')
        driver.emulation.setViewportSize(640, 480)
        driver.emulation.extraHTTPHeaders = ['X-Direct': 'header-value']
        driver.findElement(org.openqa.selenium.By.id('keyboard')).click()
        driver.keyboard.type('Geb')
        driver.keyboard.press('End')
        driver.mouse.move(1, 1)
        driver.console.clear()
        driver.findElement(org.openqa.selenium.By.id('console')).click()

        then:
        driver.storage.local('local-key') == 'local-value'
        driver.storage.localKeys().contains('local-key')
        driver.storage.session('session-key') == 'session-value'
        driver.storage.sessionKeys().contains('session-key')
        driver.page.viewportSize().width == 640
        driver.findElement(org.openqa.selenium.By.id('keyboard')).getAttribute('value') == 'Geb'
        new PollingConditions(timeout: 3).eventually {
            assert driver.console.messages*.text().contains('direct-log')
        }

        when:
        driver.get("$baseUrl/header")

        then:
        driver.currentUrl.endsWith('/header')
    }

    def 'network interception, tracing, screenshot PDF, and API client produce tangible results'() {
        given:
        driver = PlaywrightSpecSupport.driver()
        Path trace = Files.createTempFile('geb-direct-trace-', '.zip')
        Files.delete(trace)
        Path pdf = Files.createTempFile('geb-direct-pdf-', '.pdf')

        when:
        driver.network.route('**/intercept') { route -> driver.network.fulfill(route, 200, '<title>Intercepted</title>') }
        driver.tracing.start()
        driver.get("$baseUrl/intercept")
        Path writtenTrace = driver.tracing.stop(trace)
        driver.pdf.saveAs(pdf)
        def response = driver.apiClient.get(baseUrl)

        then:
        driver.title == 'Intercepted'
        writtenTrace == trace
        Files.size(trace) > 0
        Files.size(pdf) > 0
        response.status() == 200

        cleanup:
        driver.network.unroute('**/intercept')
        Files.deleteIfExists(trace)
        Files.deleteIfExists(pdf)
    }
}
