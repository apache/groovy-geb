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
package geb.navigator

import geb.test.browsers.CrossBrowser
import geb.test.GebSpecWithCallbackServer
import geb.test.browsers.RequiresRealBrowser

@CrossBrowser
@RequiresRealBrowser
class ReloadOnValueChangeSpec extends GebSpecWithCallbackServer {

    def "can set values on radio buttons that load a page from change event handlers"() {
        given:
        htmlWithReloadingChangeHandler {
            input(type: "radio", name: "reloading", value: "r1")
            input(type: "radio", name: "reloading", value: "r2", checked: "checked")
        }

        when:
        $().reloading = "r1"

        then:
        noExceptionThrown()
    }

    def "can set values on selects that load a page from change event handlers"() {
        given:
        htmlWithReloadingChangeHandler {
            select(name: "reloading") {
                option(value: "1", "Foo")
                option(value: "2", selected: "selected", "Bar")
            }
        }

        when:
        $().reloading = "1"

        then:
        noExceptionThrown()
    }

    def "can set values on checkboxes that load a page from change event handlers"() {
        given:
        htmlWithReloadingChangeHandler {
            input(type: "checkbox", name: "reloading", value: "c1")
            input(type: "checkbox", name: "reloading", value: "c2", checked: "checked")
        }

        when:
        $().reloading = "c1"

        then:
        noExceptionThrown()
    }

    private void htmlWithReloadingChangeHandler(Closure<?> additionalMarkup) {
        additionalMarkup.resolveStrategy = Closure.DELEGATE_FIRST
        html {
            additionalMarkup.delegate = delegate
            additionalMarkup.call()

            script(type: "text/javascript") {
                mkp.yieldUnescaped(getClass().getResource("/jquery-1.4.2.min.js").text)
            }

            script(type: "text/javascript", '''
                $(name: "reloading").change(function () {
                    window.location.reload(true);
                });
            ''')
        }
    }

}
