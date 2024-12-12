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

import geb.test.GebSpecWithCallbackServer

class TextareaSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            textarea()
        }
    }

    Textarea getTextarea() {
        $("textarea").module(Textarea)
    }

    def "getting and setting text"() {
        expect:
        textarea.text == ""

        when:
        textarea.text = "foo"

        then:
        textarea.text == "foo"
    }

    def "can use left shift on the module"() {
        when:
        textarea << "foo"

        then:
        textarea.text == "foo"
    }

    def "getting and setting text on an empty navigator based textarea"() {
        given:
        def textarea = $("i-dont-exist").module(Textarea)

        when:
        textarea.text = "foo"

        then:
        noExceptionThrown()
        textarea.text == null
    }
}
