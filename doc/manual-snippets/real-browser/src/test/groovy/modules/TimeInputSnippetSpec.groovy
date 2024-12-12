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

import geb.module.TimeInput
import geb.test.GebSpecWithCallbackServer

import java.time.LocalTime

class TimeInputSnippetSpec extends GebSpecWithCallbackServer {

    def "using time input module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <input type="time" name="start" min="09:00:00" max="17:00:00" step="300" />
                </body>
            </html>
            // end::html[]
        """
        // tag::example_time[]
        def input = $(name: "start").module(TimeInput)

        // end::example_time[]
        when:
        // tag::example_time[]
        input.time = LocalTime.of(14, 5)

        // end::example_time[]
        then:
        // tag::example_time[]
        assert input.time == LocalTime.of(14, 5)

        // end::example_time[]
        when:
        // tag::example_string[]
        input.time = "15:15"

        // end::example_string[]
        then:
        // tag::example_string[]
        assert input.time == LocalTime.of(15, 15)
        // end::example_string[]
    }

}
