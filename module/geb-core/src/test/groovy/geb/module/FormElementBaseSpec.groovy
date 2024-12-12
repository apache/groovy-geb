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
import spock.lang.Unroll

class FormElementBaseSpec extends GebSpecWithCallbackServer {

    @Unroll
    def "can base the module on '#tag'"() {
        given:
        html {
            button()
            input()
            select {
                option()
            }
            textarea()
        }

        when:
        $(tag).module(FormElement)

        then:
        noExceptionThrown()

        where:
        tag << ['button', 'input', 'option', 'select', 'textarea']
    }

    def "can base the module on an empty navigator"() {
        given:
        html {
        }

        when:
        $("div").module(FormElement)

        then:
        noExceptionThrown()
    }

    def "creating the module for anything other than button, input, option, select or textarea results in an exception"() {
        given:
        html {
            div("div")
        }

        when:
        $("div").module(FormElement)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base element for ${FormElement.name} module was 'div' but only the following are allowed: button, input, option, select, textarea"
    }

    def "creating the module with a base navigator containing more than one element results in error"() {
        given:
        html {
            input()
            input()
        }

        when:
        $("input").module(FormElement)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base navigator for ${FormElement.name} module has 2 elements but at most one element is allowed."
    }
}
