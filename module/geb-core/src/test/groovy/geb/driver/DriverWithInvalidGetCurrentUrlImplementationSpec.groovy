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
package geb.driver

import geb.test.GebSpecWithCallbackServer
import geb.test.StandaloneWebDriverServer
import geb.test.browsers.LocalChrome
import geb.test.browsers.RequiresRealBrowser
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.Response
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared

import static org.openqa.selenium.remote.DriverCommand.GET_CURRENT_URL

@RequiresRealBrowser
@LocalChrome
class DriverWithInvalidGetCurrentUrlImplementationSpec extends GebSpecWithCallbackServer {

    @Shared
    @AutoCleanup
    StandaloneWebDriverServer webDriverServer = new StandaloneWebDriverServer()

    def setup() {
        browser.config.cacheDriver = false
        responseHtml {}
    }

    @Issue('https://github.com/geb/issues/issues/291')
    void 'go() does not fail even if the driver returns null for get current url command'() {
        given:
        currentUrlResponseValue = null

        when:
        go()

        then:
        notThrown(Exception)
    }

    @Issue('https://github.com/geb/issues/issues/492')
    void 'go() does not fail even if the driver returns an incorrectly encoded uri for get current url command'() {
        given:
        currentUrlResponseValue = 'http://abcd1234.abcd.abcd.com:1234/ab-cd/index.html#!/abcd#abcd-abcd-abcd-abcd'

        when:
        go()

        then:
        notThrown(Exception)
    }

    void setCurrentUrlResponseValue(String value) {
        browser.driver = new DriverWithInvalidGetCurrentUrlImplementation(webDriverServer.url, value)
    }
}

class DriverWithInvalidGetCurrentUrlImplementation extends RemoteWebDriver {

    private final String currentUrl

    DriverWithInvalidGetCurrentUrlImplementation(URL remoteAddress, String currentUrl) {
        super(remoteAddress, new ChromeOptions()
                .addArguments('headless'))
        this.currentUrl = currentUrl
    }

    @Override
    protected Response execute(String command) {
        command == GET_CURRENT_URL ? new Response(value: currentUrl) : super.execute(command)
    }
}
