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
package geb.spock

import geb.Page
import spock.lang.*
import geb.test.CallbackHttpServer

@Stepwise
class GebSpecStepwiseSpec extends GebReportingSpec {

    @Shared
        server = new CallbackHttpServer(browser.config)

    def setupSpec() {
        server.start()
        server.get = { req, res ->
            res.outputStream << """
            <html>
            <body>
                <p>stuff</p>
            </body>
            </html>"""
        }
    }

    def setup() {
        baseUrl = server.baseUrl
        go()
    }

    def "go to the page"() {
        when:
        to FirstPage
        then:
        at FirstPage
    }

    def "make sure we are still at the page"() {
        expect:
        at FirstPage
    }

    def cleanupSpec() {
        server.stop()
    }
}

class FirstPage extends Page {
    static url = "/"
    static at = { $("p", 0).text() == "stuff" }
}
