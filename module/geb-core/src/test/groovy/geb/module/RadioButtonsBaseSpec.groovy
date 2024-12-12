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

import geb.error.InvalidModuleBaseException
import geb.test.GebSpecWithCallbackServer

class RadioButtonsBaseSpec extends GebSpecWithCallbackServer {

    def "can base radio buttons on an empty navigator"() {
        given:
        html {
        }

        when:
        $("input").module(RadioButtons)

        then:
        noExceptionThrown()
    }

    def "cannot base radio buttons on multiple types of elements"() {
        given:
        html {
            input(class: "foo")
            div(class: "foo")
        }

        when:
        $(".foo").module(RadioButtons)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "All elements of the base navigator for ${RadioButtons.name} module have to be inputs but found the following elements: [div, input]"
    }

    def "cannot base radio buttons on inputs that are not radios"() {
        given:
        html {
            input(type: "radio")
            input()
        }

        when:
        $("input").module(RadioButtons)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "All elements of the base navigator for ${RadioButtons.name} module have to be radio buttons but found the following input types: [radio, text]"
    }

    def "cannot base radio buttons on inputs with different names"() {
        given:
        html {
            input(type: "radio", name: "foo")
            input(type: "radio", name: "bar")
        }

        when:
        $("input").module(RadioButtons)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "All elements of the base navigator for ${RadioButtons.name} module have to have the same names but found the following names: [bar, foo]"
    }
}
