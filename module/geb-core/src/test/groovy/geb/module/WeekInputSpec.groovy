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
import org.threeten.extra.YearWeek

@Chrome
@Firefox
@RequiresRealBrowser // maybe due to https://sourceforge.net/p/htmlunit/bugs/1923/
class WeekInputSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(type: 'week', min: '2018-W01', max: '2019-W01', step: '1')
        }
    }

    WeekInput getInput() {
        $("input").module(WeekInput)
    }

    def 'unset'() {
        expect:
        input.week == null
    }

    def 'setting using week'() {
        when:
        input.week = week

        then:
        input.week == week

        where:
        week = YearWeek.of(2018, 9)
    }

    def 'setting using ISO 8601 string'() {
        when:
        input.week = week.toString()

        then:
        input.week == week

        where:
        week = YearWeek.of(2018, 10)
    }

    def 'updating'() {
        when:
        input.week = week

        and:
        input.week = week.plusWeeks(1)

        then:
        input.week == week.plusWeeks(1)

        where:
        week = YearWeek.of(2018, 11)
    }

    def 'get min, max and step'() {
        expect:
        input.min == YearWeek.of(2018, 1)

        and:
        input.max == YearWeek.of(2019, 1)

        and:
        input.step == 1
    }

}
