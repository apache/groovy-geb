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

import geb.Page
import geb.test.GebSpecWithCallbackServer

class RadioButtonsSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(name: "checked", type: "radio", value: "first")
            label(for: "checked-second", "second label")
            input(id: "checked-second", name: "checked", type: "radio", value: "second", checked: "")
            label(for: "unchecked-first", "first label")
            input(id: "unchecked-first", name: "unchecked", type: "radio", value: "first")
            input(name: "unchecked", type: "radio", value: "second")
            label {
                mkp.yield("wrapping label")
                input(name: "nested-in-label", type: "radio", value: "second", checked: "")
            }
        }
        to RadioButtonsPage
    }

    def "checked value"() {
        expect:
        checked.checked == "second"
        unchecked.checked == null
    }

    def "checked label"() {
        expect:
        checked.checkedLabel == "second label"
        unchecked.checkedLabel == null
    }

    def "checking"() {
        when:
        checked.checked = "first"
        unchecked.checked = "first label"

        then:
        checked.checked == "first"
        checked.checkedLabel == null
        unchecked.checked == "first"
        unchecked.checkedLabel == "first label"
    }

    def "checking with an empty base navigator"() {
        given:
        def radio = $("#i-dont-exist").module(RadioButtons)

        when:
        radio.checked = "foo"

        then:
        radio.checked == null
        radio.checkedLabel == null
    }

    def "text of a label wrapping a checked radio is returned even if it has no 'for' attribute"() {
        expect:
        nestedInLabel.checkedLabel == "wrapping label"
    }

    def "module is extendable"() {
        expect:
        $(name: "checked").module(ExtendedRadioButtons).checked == "second"
    }
}

class RadioButtonsPage extends Page {
    static content = {
        checked { $(name: "checked").module(RadioButtons) }
        unchecked { $(name: "unchecked").module(RadioButtons) }
        nestedInLabel { $(name: "nested-in-label").module(RadioButtons) }
    }
}

class ExtendedRadioButtons extends RadioButtons {
}
