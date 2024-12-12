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
import geb.test.browsers.Chrome
import geb.test.browsers.Firefox
import geb.test.browsers.RequiresRealBrowser

import java.time.LocalTime

@Chrome
@Firefox
@RequiresRealBrowser // maybe due to https://sourceforge.net/p/htmlunit/bugs/1923/
class TimeInputSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(type: 'time', min: '09:00:00', max: '17:00:00', step: '30')
        }
    }

    TimeInput getInput() {
        $("input").module(TimeInput)
    }

    def 'unset'() {
        expect:
        input.time == null
    }

    def 'setting using time'() {
        when:
        input.time = time

        then:
        input.time == time

        where:
        time = LocalTime.of(12, 00, 00)
    }

    def 'setting using ISO 8601 string'() {
        when:
        input.time = time.toString()

        then:
        input.time == time

        where:
        time = LocalTime.of(12, 45, 30)
    }

    def 'updating'() {
        when:
        input.time = time

        and:
        input.time = time.plusSeconds(30)

        then:
        input.time == time.plusSeconds(30)

        where:
        time = LocalTime.of(15, 00, 00)
    }

    def 'get min, max and step'() {
        expect:
        input.min == LocalTime.of(9, 0)

        and:
        input.max == LocalTime.of(17, 0)

        and:
        input.step == 30
    }

}
