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

import geb.module.DateTimeLocalInput
import geb.test.GebSpecWithCallbackServer

import java.time.LocalDateTime

class DateTimeLocalInputSnippetSpec extends GebSpecWithCallbackServer {

    def "using datetime-local input module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <input type="datetime-local" name="next-meeting"/>
                </body>
            </html>
            // end::html[]
        """

        when:
        // tag::example[]
        def input = $(name: "next-meeting").module(DateTimeLocalInput)
        input.dateTime = "2018-12-09T20:16"

        // end::example[]
        then:
        // tag::example[]
        assert input.dateTime == LocalDateTime.of(2018, 12, 9, 20, 16)

        // end::example[]

        when:
        // tag::example[]
        input.dateTime = LocalDateTime.of(2018, 12, 31, 0, 0)

        // end::example[]
        then:
        // tag::example[]
        assert input.dateTime == LocalDateTime.parse("2018-12-31T00:00")
        // end::example[]
    }

}
