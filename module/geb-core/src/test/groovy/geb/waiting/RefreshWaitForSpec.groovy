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
package geb.waiting

import geb.Page
import geb.test.GebSpecWithCallbackServer
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError

class RefreshWaitForSpec extends GebSpecWithCallbackServer {

    def setup() {
        def loadCount = 0
        callbackServer.get = { req, res ->
            loadCount++
            res.outputStream << """
                <html>
                <body>
                    $loadCount
                </body>
                </html>
            """
        }
        to RefreshWaitForSpecPage
    }

    def 'page is reloaded before each try'() {
        expect:
        refreshWaitFor {
            loadCount > 2
        }
    }

    def 'failed waiting'() {
        when:
        refreshWaitFor(0.1) {
            loadCount < 1
        }

        then:
        WaitTimeoutException exception = thrown()
        exception.cause in PowerAssertionError
        exception.cause.message.contains('loadCount < 1')
    }

    def 'using timeout and interval'() {
        expect:
        refreshWaitFor(1, 0.01) {
            loadCount > 5
        }
    }

    def 'using preset'() {
        given:
        browser.config.setWaitPreset("custom", 1, 0.01)

        expect:
        refreshWaitFor("custom") {
            loadCount > 5
        }
    }

}

class RefreshWaitForSpecPage extends Page {
    static content = {
        loadCount { $().text().toInteger() }
    }
}
