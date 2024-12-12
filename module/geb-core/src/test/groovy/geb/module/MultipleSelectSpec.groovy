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

class MultipleSelectSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            select(id: "selected", multiple: "") {
                option("Option #1", value: "1")
                option("Option #2", value: "2", selected: "selected")
                option("Option #3", value: "3", selected: "selected")
            }
            select(id: "not-selected", multiple: "") {
                option("Option #1", value: "1")
                option("Option #2", value: "2")
                option("Option #3", value: "3")
            }
        }
        to MultipleSelectPage
    }

    def "selected text"() {
        expect:
        selected.selectedText == ["Option #2", "Option #3"]
        notSelected.selectedText == []
    }

    def "selected value"() {
        expect:
        selected.selected == ["2", "3"]
        notSelected.selected == []
    }

    def "selecting"() {
        when:
        selected.selected = ["1"]
        notSelected.selected = ["Option #2", "Option #3"]

        then:
        selected.selected == ["1"]
        notSelected.selected == ["2", "3"]
    }

    def "selecting on an empty navigator base"() {
        given:
        def select = $("i-dont-exist").module(MultipleSelect)

        when:
        select.selected = ["foo", "bar"]

        then:
        noExceptionThrown()
        select.selected == null
    }
}

class MultipleSelectPage extends Page {
    static content = {
        selected { $("#selected").module(MultipleSelect) }
        notSelected { $("#not-selected").module(MultipleSelect) }
    }
}

