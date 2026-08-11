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

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.opentest4j.TestAbortedException
import org.openqa.selenium.By
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

@IgnoreIf({ System.getProperty('geb.direct.playwright.skip') == 'true' || System.getenv('PLAYWRIGHT_SKIP') == 'true' })
class PlaywrightWebDriverSmokeSpec extends Specification {

    @Shared
    Server server

    @Shared
    PlaywrightWebDriver driver

    @Shared
    String baseUrl

    def setupSpec() {
        server = new Server(0)
        def context = new ServletContextHandler(ServletContextHandler.SESSIONS)
        context.contextPath = '/'
        context.addServlet(new ServletHolder(new SmokePageServlet()), '/')
        server.handler = context
        server.start()
        baseUrl = "http://localhost:${server.connectors[0].localPort}/"

        try {
            driver = PlaywrightDriver.create {
                headless = true
                browserType = 'chromium'
            }
        } catch (Exception exception) {
            server.stop()
            throw new TestAbortedException("Playwright Chromium is unavailable: ${exception.message}", exception)
        }
    }

    def cleanupSpec() {
        driver?.quit()
        server?.stop()
    }

    def "navigates, enters text, and clicks a button"() {
        when:
        driver.get(baseUrl)
        def input = driver.findElement(By.cssSelector('#name'))
        input.sendKeys('Ada')
        driver.findElement(By.cssSelector('#submit')).click()

        then:
        driver.title == 'Playwright smoke test'
        driver.findElement(By.cssSelector('#result')).text == 'Hello Ada'
    }

    static class SmokePageServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) {
            response.contentType = 'text/html;charset=UTF-8'
            response.writer.write('''<!doctype html>
<html>
<head><title>Playwright smoke test</title></head>
<body>
  <label for="name">Name</label>
  <input id="name" type="text">
  <button id="submit" onclick="document.getElementById('result').textContent = 'Hello ' + document.getElementById('name').value">Submit</button>
  <p id="result"></p>
</body>
</html>''')
        }
    }
}
