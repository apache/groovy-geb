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

class NavigatorValueEventListenerSpec extends AbstractBrowserConfiguredNavigatorEventListenerSpec {

    def setup() {
        html {
            input(type: "text", name: "textInput", value: "original value")
        }
    }

    def scenarios() {
        [
                ["Navigator", { $("input") }],
                ["Module", { $("input").module(TextInput) }],
                ["TemplateDerivedPageContent", { page(NavigatorValueEventListenerSpecPage).input }]
        ]
    }

    @Unroll("event listener is notified before #scenario value is set")
    def "event listener is notified before navigator value is set"() {
        given:
        def value = "new value"
        def navigator = navigatorProvider.call()

        when:
        navigator.value(value)

        then:
        1 * listener.beforeValueSet(browser, { it.is(navigator) && it.value() == "original value" }, value)

        where:
        [scenario, navigatorProvider] << scenarios()
    }

    @Unroll("event listener is notified after #scenario value is set")
    def "event listener is notified after navigator value is set"() {
        given:
        def value = "new value"
        def navigator = navigatorProvider.call()

        when:
        navigator.value(value)

        then:
        1 * listener.afterValueSet(browser, { it.is(navigator) && it.value() == value }, value)

        where:
        [scenario, navigatorProvider] << scenarios()
    }
}

class NavigatorValueEventListenerSpecPage extends Page {
    static content = {
        input { $("input") }
    }
}
