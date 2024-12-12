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

import geb.Page
import geb.module.TextInput
import spock.lang.Unroll

class NavigatorSendKeysEventListenerSpec extends AbstractBrowserConfiguredNavigatorEventListenerSpec {

    def setup() {
        html {
            input(type: "text", name: "textInput")
        }
    }

    def scenarios() {
        [
                ["Navigator", { $("input") }],
                ["Module", { $("input").module(TextInput) }],
                ["TemplateDerivedPageContent", { page(NavigatorSendKeysEventListenerSpecPage).input }]
        ]
    }

    @Unroll("event listener is notified before keys are sent to #scenario")
    def "event listener is notified before keys are sent to navigator"() {
        given:
        def value = "new value"
        def navigator = navigatorProvider.call()

        when:
        navigator << value

        then:
        1 * listener.beforeSendKeys(browser, { it.is(navigator) && !it.value() }, value)

        where:
        [scenario, navigatorProvider] << scenarios()
    }

    @Unroll("event listener is notified after keys are sent to #scenario")
    def "event listener is notified after keys are sent to navigator"() {
        given:
        def value = "new value"
        def navigator = navigatorProvider.call()

        when:
        navigator << value

        then:
        1 * listener.afterSendKeys(browser, { it.is(navigator) && it.value() == value }, value)

        where:
        [scenario, navigatorProvider] << scenarios()
    }
}

class NavigatorSendKeysEventListenerSpecPage extends Page {
    static content = {
        input { $("input") }
    }
}
