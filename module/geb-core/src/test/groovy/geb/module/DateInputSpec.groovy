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

import java.time.LocalDate

class DateInputSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(type: 'date')
        }
    }

    DateInput getInput() {
        $("input").module(DateInput)
    }

    def 'unset'() {
        expect:
        input.date == null
    }

    def 'setting using date'() {
        when:
        input.date = date

        then:
        input.date == date

        where:
        date = LocalDate.now()
    }

    def 'setting using ISO 8601 string'() {
        when:
        input.date = date.toString()

        then:
        input.date == date

        where:
        date = LocalDate.now()
    }

    def 'updating'() {
        when:
        input.date = date

        and:
        input.date = date.plusDays(1)

        then:
        input.date == date.plusDays(1)

        where:
        date = LocalDate.now()
    }

}
