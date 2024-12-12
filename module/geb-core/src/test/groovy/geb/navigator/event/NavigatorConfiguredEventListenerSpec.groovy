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
import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class NavigatorConfiguredEventListenerSpec extends GebSpecWithCallbackServer {

    NavigatorEventListener listener = Mock(NavigatorEventListener)

    def setup() {
        html {
            button "button"
        }
    }

    @SuppressWarnings("ClosureAsLastMethodParameter")
    @Unroll("event listener configured on navigator is notified after #scenario is clicked")
    def "event listener configured on navigator is notified after navigator is clicked"() {
        given:
        def navigator = navigatorProvider.call()
        navigator.eventListener = listener

        when:
        navigator.click()

        then:
        1 * listener.afterClick(browser, { it.is(navigator) })

        where:
        scenario                     | navigatorProvider
        "Navigator"                  | { $("button") }
        "Module"                     | { $("button").module(Module) }
        "TemplateDerivedPageContent" | { page(NavigatorConfiguredEventListenerSpecPage).button }
    }

}

class NavigatorConfiguredEventListenerSpecPage extends Page {
    static content = {
        button { $("button") }
    }
}
