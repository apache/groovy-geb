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
import geb.test.browsers.CrossBrowser

@CrossBrowser
class MultipleSelectCrossBrowserSpec extends GebSpecWithCallbackServer {

    def setup() {
        responseHtml {
            select(id: "with-whitespace-in-option", multiple: "") {
                option("   Option   #1 ", value: "1")
            }
        }
    }

    def "selecting an option by text when it contains additional space in the dom"() {
        given:
        to MultipleSelectCrossBrowserSpecPage

        when:
        withWhitespaceInOption.selected = ["Option #1"]

        then:
        withWhitespaceInOption.selected == ["1"]
    }
}

class MultipleSelectCrossBrowserSpecPage extends Page {
    static content = {
        withWhitespaceInOption { $("#with-whitespace-in-option").module(MultipleSelect) }
    }
}
