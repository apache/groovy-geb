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

class CheckboxSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(id: "checked", type: "checkbox", checked: "checked")
            input(id: "unchecked", type: "checkbox")
        }
    }

    def "checked"() {
        expect:
        $("#checked").module(Checkbox).checked
        !$("#unchecked").module(Checkbox).checked
    }

    def "unchecked"() {
        expect:
        $("#unchecked").module(Checkbox).unchecked
        !$("#checked").module(Checkbox).unchecked
    }

    def "check"() {
        when:
        $("#checked").module(Checkbox).check()

        then:
        $("#checked").value()

        when:
        $("#unchecked").module(Checkbox).check()

        then:
        $("#unchecked").value()
    }

    def "uncheck"() {
        when:
        $("#unchecked").module(Checkbox).uncheck()

        then:
        !$("#unchecked").value()

        when:
        $("#checked").module(Checkbox).uncheck()

        then:
        !$("#checked").value()
    }

    def "can call check and uncheck an empty navigator based checkbox"() {
        given:
        def checkbox = $("#i-dont-exist").module(Checkbox)

        when:
        checkbox.check()
        checkbox.uncheck()

        then:
        noExceptionThrown()
    }

    def "cannot check if an empty navigator based checkbox is checked"() {
        given:
        def exceptionMessage = "This operation is not supported on an empty navigator based ${Checkbox.name} module"
        def checkbox = $("#i-dont-exist").module(Checkbox)

        when:
        checkbox.checked

        then:
        UnsupportedOperationException e = thrown()
        e.message == exceptionMessage

        when:
        checkbox.unchecked

        then:
        e = thrown()
        e.message == exceptionMessage
    }
}
