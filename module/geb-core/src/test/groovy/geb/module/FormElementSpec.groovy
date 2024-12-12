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

import geb.test.browsers.CrossBrowser
import geb.test.GebSpecWithCallbackServer

@CrossBrowser
class FormElementSpec extends GebSpecWithCallbackServer {

    def disabled() {
        given:
        html {
            input(id: "noDisabledAttr")
            input(id: "disabledAttr", disabled: 'disabled')
            input(id: "disabledAttr2", disabled: 'xyz')
        }

        expect:
        $("#noDisabledAttr").module(FormElement).enabled
        !$("#noDisabledAttr").module(FormElement).disabled
        $("#disabledAttr").module(FormElement).disabled
        !$("#disabledAttr").module(FormElement).enabled
        $("#disabledAttr2").module(FormElement).disabled
        !$("#disabledAttr2").module(FormElement).enabled
    }

    def readOnly() {
        given:
        html {
            input(id: "noReadonlyAttr")
            input(id: "readonlyAttr", readonly: 'readonly')
            input(id: "readonlyAttr2", readonly: 'xyz')
        }

        expect:
        $("#noReadonlyAttr").module(FormElement).editable
        !$("#noReadonlyAttr").module(FormElement).readOnly
        $("#readonlyAttr").module(FormElement).readOnly
        !$("#readonlyAttr").module(FormElement).editable
        $("#readonlyAttr2").module(FormElement).readOnly
        !$("#readonlyAttr2").module(FormElement).editable
    }

    def "empty navigator based module"() {
        given:
        def exceptionMessage = "This operation is not supported on an empty navigator based ${FormElement.name} module"
        def module = $("i-dont-exist").module(FormElement)

        when:
        module.enabled

        then:
        UnsupportedOperationException e = thrown()
        e.message == exceptionMessage

        when:
        module.disabled

        then:
        e = thrown()
        e.message == exceptionMessage

        when:
        module.editable

        then:
        e = thrown()
        e.message == exceptionMessage

        when:
        module.readOnly

        then:
        e = thrown()
        e.message == exceptionMessage
    }
}
