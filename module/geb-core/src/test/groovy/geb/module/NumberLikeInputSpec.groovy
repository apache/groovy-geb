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
import spock.lang.Unroll

abstract class NumberLikeInputSpec extends GebSpecWithCallbackServer {

    abstract NumberLikeInput getInput()

    @Unroll
    def 'set #number'() {
        when:
        input.number = number

        then:
        input.number == number

        and:
        input.value() == "${number}"

        where:
        number << [-2.5, 0, 1, 2.5]
    }

    def 'get min, max and step'() {
        expect:
        input.min == -2.5

        and:
        input.max == 2.5

        and:
        input.step == 0.5
    }

    def "getting and setting text on an empty navigator based number input"() {
        given:
        def input = $("i-dont-exist").module(NumberInput)

        when:
        input.number = 3.2

        then:
        noExceptionThrown()
        input.number == null
    }

}
