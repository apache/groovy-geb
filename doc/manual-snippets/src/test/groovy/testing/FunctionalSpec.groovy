/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testing

// tag::concise[]
// tag::verbose[]
import geb.spock.GebSpec

// end::concise[]
// end::verbose[]
import geb.test.CallbackHttpServer
import spock.lang.AutoCleanup
import spock.lang.Shared

import javax.servlet.http.HttpServletRequest

// tag::concise[]
// tag::verbose[]
class FunctionalSpec extends GebSpec {
    // end::concise[]
    // end::verbose[]
    @Shared
    @AutoCleanup("stop")
    CallbackHttpServer callbackServer = new CallbackHttpServer(browser.config)

    def setupSpec() {
        browser.driver = null
        callbackServer.start()
        callbackServer.html { HttpServletRequest request ->
            if (request.requestURI.endsWith("/login")) {
                head {
                    title "Login Screen"
                }
            }
        }
    }

    def setup() {
        browser.baseUrl = callbackServer.baseUrl
    }

    def cleanupSpec() {
        callbackServer.stop()
    }

    // tag::concise[]
    def "go to login"() {
        when:
        go "/login"

        then:
        title == "Login Screen"
    }
    // end::concise[]

    // tag::verbose[]
    def "verbosely go to login"() {
        when:
        browser.go "/login"

        then:
        browser.page.title == "Login Screen"
    }
    // tag::concise[]
}
// end::concise[]
// end::verbose[]