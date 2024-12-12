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
package geb.navigator.event

import geb.Module
import geb.Page
import spock.lang.Unroll

class NavigatorClickEventListenerSpec extends AbstractBrowserConfiguredNavigatorEventListenerSpec {

    def setup() {
        bodyWithJquery {
            button "button"
            script(type: "text/javascript") {
                mkp.yieldUnescaped '''
                    $("button").click(function() {
                        $(this).attr("clicked", "true");
                    });
                '''
            }
        }
    }

    def scenarios() {
        [
                ["Navigator", { $("button") }],
                ["Module", { $("button").module(Module) }],
                ["TemplateDerivedPageContent", { page(NavigatorClickEventListenerSpecPage).button }]
        ]
    }

    @SuppressWarnings("ClosureAsLastMethodParameter")
    @Unroll("event listener is notified before #scenario is clicked")
    def "event listener is notified before navigator is clicked"() {
        given:
        def navigator = navigatorProvider.call()

        when:
        navigator.click()

        then:
        1 * listener.beforeClick(browser, { it.is(navigator) && !it.@clicked })

        where:
        [scenario, navigatorProvider] << scenarios()
    }

    @SuppressWarnings("ClosureAsLastMethodParameter")
    @Unroll("event listener is notified after #scenario is clicked")
    def "event listener is notified after navigator is clicked"() {
        given:
        def navigator = navigatorProvider.call()

        when:
        navigator.click()

        then:
        1 * listener.afterClick(browser, { it.is(navigator) && it.@clicked })

        where:
        [scenario, navigatorProvider] << scenarios()
    }
}

class NavigatorClickEventListenerSpecPage extends Page {
    static content = {
        button { $("button") }
    }
}
