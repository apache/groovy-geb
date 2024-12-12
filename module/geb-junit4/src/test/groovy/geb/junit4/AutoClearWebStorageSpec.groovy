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
package geb.junit4

import geb.Browser
import geb.test.CallbackHttpServer
import geb.test.GebTestManager
import geb.test.GebTestManagerBuilder
import geb.test.browsers.Chrome
import geb.test.browsers.RequiresRealBrowser
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@RequiresRealBrowser
@Chrome
class AutoClearWebStorageSpec extends Specification {

    @Shared
    @AutoCleanup("stop")
    def server = new CallbackHttpServer()

    def setupSpec() {
        server.start()
    }

    def "web storage is cleaned after the test if configured to do so"() {
        given:
        def browser = new Browser()

        when:
        new JUnitCore().run(Request.aClass(WebStorageModifyingTest))

        then:
        browser.localStorage.size() == 0
        browser.sessionStorage.size() == 0
    }
}

@Category(DoNotRunFromGradle)
class WebStorageModifyingTest extends GebTest {

    private final static GebTestManager TEST_MANAGER = new GebTestManagerBuilder()
        .withBrowserCreator { new Browser() }
        .build()

    @Delegate(includes = ["getBrowser"])
    static GebTestManager getTestManager() {
        TEST_MANAGER
    }

    def server = new CallbackHttpServer(browser.config)

    @Before
    void setUp() {
        server.start()
        server.html {}
        browser.baseUrl = server.baseUrl
        browser.config.autoClearWebStorage = true
    }

    @Test
    void modifyingWebStorageDuringATest() {
        go()
        localStorage["test"] = "test"
        sessionStorage["test"] = "test"
    }

    @After
    void tearDown() {
        server.stop()
    }
}
