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
package geb.test

import geb.Browser
import geb.Configuration
import geb.ConfigurationLoader
import geb.test.browsers.LocalChrome
import geb.test.browsers.RequiresRealBrowser
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@RequiresRealBrowser
@LocalChrome
class GebTestManagerWebStorageCleanupSpec extends Specification {

    Configuration configuration = new ConfigurationLoader().conf

    @Shared
    @AutoCleanup("stop")
    def server = new CallbackHttpServer()

    def setupSpec() {
        server.start()
    }

    @Delegate
    GebTestManager gebTestManager

    def setup() {
        configuration.baseUrl = server.baseUrl
        configuration.autoClearWebStorage = true
        gebTestManager = new GebTestManagerBuilder()
            .withBrowserCreator {
                new Browser(configuration)
            }
            .build()
        beforeTestClass(getClass())
        beforeTest(getClass(), specificationContext.currentIteration.name)
    }

    def cleanup() {
        afterTestClass()
    }

    def "web storage is cleaned after the test when configured to do so"() {
        given:
        browser.go()
        browser.localStorage["test"] = "test"

        when:
        afterTest()

        then:
        browser.localStorage.keySet().empty
    }

    def "errors from cleaning web storage after the test are suppressed"() {
        given:
        browser.go("about:blank")

        when:
        afterTest()

        then:
        noExceptionThrown()
    }
}
