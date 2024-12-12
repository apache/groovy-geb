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

class TextareaBaseSpec extends GebSpecWithCallbackServer {

    def "can base a textarea on an empty navigator"() {
        given:
        html {
        }

        when:
        $("textarea").module(Textarea)

        then:
        noExceptionThrown()
    }

    def "basing a textarea on anything other than an input throws an exception"() {
        given:
        html {
            div()
        }

        when:
        $("div").module(Textarea)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base element for ${Textarea.name} module was 'div' but only textarea is allowed as the base element."
    }

    def "can create the module based on a textarea"() {
        given:
        html {
            textarea()
        }

        when:
        $("textarea").module(Textarea)

        then:
        noExceptionThrown()
    }

    def "creating the module with a base navigator containing more than one element results in error"() {
        given:
        html {
            textarea()
            textarea()
        }

        when:
        $("textarea").module(Textarea)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base navigator for ${Textarea.name} module has 2 elements but at most one element is allowed."
    }
}
