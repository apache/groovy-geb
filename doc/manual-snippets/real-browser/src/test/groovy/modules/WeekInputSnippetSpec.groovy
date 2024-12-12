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

import geb.module.WeekInput
import geb.test.GebSpecWithCallbackServer
import org.threeten.extra.YearWeek

class WeekInputSnippetSpec extends GebSpecWithCallbackServer {

    def "using week input module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <input type="week" name="delivery-week" min="2018-W01" max="2019-W01" step="1" />
                </body>
            </html>
            // end::html[]
        """
        // tag::example_week[]
        def input = $(name: "delivery-week").module(WeekInput)

        // end::example_week[]
        when:
        // tag::example_week[]
        input.week = YearWeek.of(2018, 5)

        // end::example_week[]
        then:
        // tag::example_week[]
        assert input.week == YearWeek.of(2018, 5)

        // end::example_week[]
        when:
        // tag::example_string[]
        input.week = "2018-W52"

        // end::example_string[]
        then:
        // tag::example_string[]
        assert input.week == YearWeek.of(2018, 52)
        // end::example_string[]
    }

}
