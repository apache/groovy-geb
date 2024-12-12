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
package pages

import configuration.InlineConfigurationLoader
import geb.Page
import geb.test.GebSpecWithCallbackServer

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut

class PageEventListenerSpec extends GebSpecWithCallbackServer implements InlineConfigurationLoader {

    def setup() {
        html {
            head {
                title "test page"
            }
        }
    }

    @SuppressWarnings("GStringExpressionWithinString")
    String getPageEventListenerConfiguration() {
        '''
            // tag::config[]
            import geb.Browser
            import geb.Page
            import geb.PageEventListenerSupport

            pageEventListener = new PageEventListenerSupport() {
                void onAtCheckFailure(Browser browser, Page page) {
                    println "At check failed for page titled '${browser.title}' at url ${browser.currentUrl}"
                }
            }
            // end::config[]
        '''
    }

    def "registering a page event listener"() {
        when:
        configScript(pageEventListenerConfiguration)
        browser.config.merge(config)

        and:
        def assertionError
        def output = tapSystemOut {
            try {
                to PageEventListenerSpecPage
            } catch (AssertionError e) {
                assertionError = e
            }
        }

        then:
        assertionError

        and:
        output =~ /At check failed for page titled 'test page' at url http:\/\/localhost:\d+\/main.html/
    }
}

class PageEventListenerSpecPage extends Page {
    static at = { false }

    static url = "main.html"
}
