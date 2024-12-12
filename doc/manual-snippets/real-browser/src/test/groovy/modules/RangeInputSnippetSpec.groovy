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

import geb.module.RangeInput
import geb.test.GebSpecWithCallbackServer

class RangeInputSnippetSpec extends GebSpecWithCallbackServer {

    def "using range input module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <input type="range" name="volume" min="0" max="10" step="0.1"/>
                </body>
            </html>
            // end::html[]
        """

        when:
        // tag::example[]
        def input = $(name: "volume").module(RangeInput)
        input.number = 3.5

        // end::example[]
        then:
        // tag::example[]
        assert input.number == 3.5
        assert input.min == 0
        assert input.max == 10
        assert input.step == 0.1
        // end::example[]
    }

}
