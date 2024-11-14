/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geb.module

import geb.test.GebSpecWithCallbackServer
import geb.test.browsers.Chrome
import geb.test.browsers.RequiresRealBrowser

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Chrome
@RequiresRealBrowser // maybe due to https://sourceforge.net/p/htmlunit/bugs/1923/
class DateTimeLocalInputSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(type: 'datetime-local')
        }
    }

    DateTimeLocalInput getInput() {
        $("input").module(DateTimeLocalInput)
    }

    def 'unset'() {
        expect:
        input.dateTime == null
    }

    def 'setting using datetime'() {
        when:
        input.dateTime = dateTime

        then:
        input.dateTime == truncated(dateTime)

        where:
        dateTime = LocalDateTime.now()
    }

    def 'setting using ISO 8601 string'() {
        when:
        input.dateTime = dateTime.toString()

        then:
        input.dateTime == truncated(dateTime)

        where:
        dateTime = LocalDateTime.now()
    }

    def 'updating'() {
        when:
        input.dateTime = dateTime

        and:
        input.dateTime = dateTime.plusDays(1)

        then:
        input.dateTime == truncated(dateTime.plusDays(1))

        where:
        dateTime = LocalDateTime.now()
    }
    
    private static LocalDateTime truncated(LocalDateTime ldt) {
        return ldt.truncatedTo(ChronoUnit.MILLIS)
    }

}
