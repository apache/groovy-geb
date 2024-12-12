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
package modules

import geb.module.MonthInput
import geb.test.GebSpecWithCallbackServer

import java.time.YearMonth

class MonthInputSnippetSpec extends GebSpecWithCallbackServer {

    def "using month input module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <input type="month" name="employment-start"/>
                </body>
            </html>
            // end::html[]
        """
        // tag::example_month[]
        def input = $(name: "employment-start").module(MonthInput)

        // end::example_month[]
        when:
        // tag::example_month[]
        input.month = YearMonth.of(2018, 12)

        // end::example_month[]
        then:
        // tag::example_month[]
        assert input.month == YearMonth.of(2018, 12)

        // end::example_month[]
        when:
        // tag::example_string[]
        input.month = "2019-01"

        // end::example_string[]
        then:
        // tag::example_string[]
        assert input.month == YearMonth.of(2019, 1)
        // end::example_string[]
    }

}
