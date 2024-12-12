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

import java.time.YearMonth

@Chrome
@Firefox
@RequiresRealBrowser // maybe due to https://sourceforge.net/p/htmlunit/bugs/1923/
class MonthInputSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(type: 'month', min: '2018-01', max: '2018-12', step: '2')
        }
    }

    MonthInput getInput() {
        $("input").module(MonthInput)
    }

    def 'unset'() {
        expect:
        input.month == null
    }

    def 'setting using time'() {
        when:
        input.month = month

        then:
        input.month == month

        where:
        month = YearMonth.of(2018, 1)
    }

    def 'setting using ISO 8601 string'() {
        when:
        input.month = month.toString()

        then:
        input.month == month

        where:
        month = YearMonth.of(2018, 3)
    }

    def 'updating'() {
        when:
        input.month = month

        and:
        input.month = month.plusMonths(2)

        then:
        input.month == month.plusMonths(2)

        where:
        month = YearMonth.of(2018, 5)
    }

    def 'get min, max and step'() {
        expect:
        input.min == YearMonth.of(2018, 1)

        and:
        input.max == YearMonth.of(2018, 12)

        and:
        input.step == 2
    }

}
