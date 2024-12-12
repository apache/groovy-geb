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
package geb.module

import geb.Module
import geb.navigator.Navigator
import geb.test.GebSpecWithCallbackServer

class DynamicNavigatorBasedModuleSpec extends GebSpecWithCallbackServer {

    final static String MODULE_ELEMENT_CLASS = "module-element"
    private final static String MODULE_ELEMENT_TEXT = "module text"

    def "basing modules off dynamic navigators"() {
        given:
        bodyWithJquery()
        def module = $(".$MODULE_ELEMENT_CLASS", dynamic: true).module(Module)

        when:
        appendModuleElementTo($("body"))

        then:
        module.text() == MODULE_ELEMENT_TEXT
    }

    def "modules with bases defined as dynamic navigators"() {
        given:
        bodyWithJquery()
        def module = $().module(DynamicNavigatorBaseModule)

        when:
        appendModuleElementTo($("body"))

        then:
        module.text() == MODULE_ELEMENT_TEXT
    }

    def "modules with bases defined as dynamic navigators based off dynamic navigators"() {
        given:
        bodyWithJquery()
        def module = $("body > div", dynamic: true).module(DynamicNavigatorBaseModule)

        when:
        $("body").jquery.append("<div></div>")
        appendModuleElementTo($("div"))

        then:
        module.text() == MODULE_ELEMENT_TEXT
    }

    void appendModuleElementTo(Navigator navigator) {
        navigator.jquery.append("""<div class="$MODULE_ELEMENT_CLASS">$MODULE_ELEMENT_TEXT</div>""".toString())
    }
}

class DynamicNavigatorBaseModule extends Module {
    static base = { $(".$DynamicNavigatorBasedModuleSpec.MODULE_ELEMENT_CLASS", dynamic: true) }
}
