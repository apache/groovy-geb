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

class GebSpecSpec extends GebSpec {

    @Shared
        server = new CallbackHttpServer(browser.config)

    def setupSpec() {
        server.start()
        server.get = { req, res ->
            res.outputStream << """
            <html>
            <body>
                <div class="d1" id="d1">d1</div>
            </body>
            </html>"""
        }
    }

    def setup() {
        baseUrl = server.baseUrl
        go()
    }

    def "missing methods are invoked on the driver instance"() {
        // This also verifies that the driver instance is instantiated correctly
        when:
        go("/")
        then:
        notThrown(Exception)
    }

    def "missing property access are requested on the driver instance"() {
        given:
        page SomePage
        when:
        prop
        then:
        notThrown(Exception)
    }

    def "missing property assignments are forwarded to the driver instance"() {
        given:
        page SomePage
        when:
        prop = 2
        then:
        notThrown(Exception)
    }

    def cleanupSpec() {
        server.stop()
    }

}

class SomePage extends Page {
    def prop = 1
}
