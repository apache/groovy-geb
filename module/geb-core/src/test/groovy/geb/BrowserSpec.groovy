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
package geb

import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class BrowserSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml {
            body {
            }
        }
    }

    def "clear cookies"() {
        when:
        browser.clearCookies()

        then:
        notThrown(Throwable)
    }

    def "clear multiple cookies"() {
        given:
        browser.driver.javascriptEnabled = false
        go()

        when:
        browser.clearCookies('http://groovy.apache.org/geb/')

        then:
        notThrown(Throwable)

        and:
        browser.currentUrl.contains('groovy.apache.org/geb/')
    }

    def "load default config"() {
        expect:
        browser.config.rawConfig.testValue == true
    }

    def "current url is returned from browser"() {
        when:
        go()

        then:
        browser.currentUrl == server.baseUrl
    }

    @Unroll
    def "page setting methods return an instance set as the current page when using #scenario"() {
        expect:
        page(argument).getClass() == BrowserSpecPage

        where:
        scenario             | argument
        "class"              | BrowserSpecPage
        "instance"           | new BrowserSpecPage()
        "array of classes"   | [BrowserSpecPage] as Class[]
        "array of instances" | [new BrowserSpecPage()] as Page[]
    }
}

class BrowserSpecPage extends Page {
    static at = { true }
}