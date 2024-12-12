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

class SelectSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            select(id: "selected") {
                option("Option #1", value: "1")
                option("Option #2", value: "2", selected: "selected")
            }
            select(id: "not-selected") {
                option("Option #1", value: "1")
                option("Option #2", value: "2")
            }
        }
        to SelectPage
    }

    def "selected text"() {
        expect:
        selected.selectedText == "Option #2"
        notSelected.selectedText == "Option #1"
    }

    def "selected value"() {
        expect:
        selected.selected == "2"
        notSelected.selected == "1"
    }

    def "selecting"() {
        when:
        selected.selected = "1"
        notSelected.selected = "Option #2"

        then:
        selected.selected == "1"
        notSelected.selected == "2"
    }

    def "selecting on an empty navigator base"() {
        given:
        def select = $("i-dont-exist").module(Select)

        when:
        select.selected = "foo"

        then:
        noExceptionThrown()
        select.selected == null
    }
}

class SelectPage extends Page {
    static content = {
        selected { $("#selected").module(Select) }
        notSelected { $("#not-selected").module(Select) }
    }
}
