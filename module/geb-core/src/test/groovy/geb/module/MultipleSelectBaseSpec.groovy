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

class MultipleSelectBaseSpec extends GebSpecWithCallbackServer {

    def "can base a multiple select on an empty navigator"() {
        given:
        html {
        }

        when:
        $("select").module(MultipleSelect)

        then:
        noExceptionThrown()
    }

    def "basing a multiple select on anything other than a select element throws an exception"() {
        given:
        html {
            input()
        }

        when:
        $("input").module(MultipleSelect)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base element for ${MultipleSelect.name} module was 'input' but only select is allowed as the base element."
    }

    def "basing a multiple select on single choice select throws an exception"() {
        given:
        html {
            select()
        }

        when:
        $("select").module(MultipleSelect)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base element for ${MultipleSelect.name} module was a single choice select but only multiple choice select is allowed as the base element."
    }

    def "can base a select on a single choice select"() {
        given:
        html {
            select(multiple: "")
        }

        when:
        $("select").module(MultipleSelect)

        then:
        noExceptionThrown()
    }

    def "creating the module with a base navigator containing more than one element results in error"() {
        given:
        html {
            select()
            select()
        }

        when:
        $("select").module(MultipleSelect)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base navigator for ${MultipleSelect.name} module has 2 elements but at most one element is allowed."
    }
}
