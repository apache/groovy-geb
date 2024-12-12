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
package navigator

import geb.test.GebSpecWithCallbackServer

class SelectSpec extends GebSpecWithCallbackServer {

    def "setting select element value"() {
        given:
        html """
            <html>
                // tag::html[]
                <form>
                    <select name="artist">
                        <option value="1">Ima Robot</option>
                        <option value="2">Edward Sharpe and the Magnetic Zeros</option>
                        <option value="3">Alexander</option>
                    </select>
                </form>
                // end::html[]
            </html>
        """

        when:
        // tag::setting_value[]
        $("form").artist = "1" //<1>
        // end::setting_value[]

        then:
        $("form").artist == "1"

        when:
        // tag::setting_value[]
        $("form").artist = 2 //<2>
        // end::setting_value[]

        then:
        $("form").artist == "2"

        when:
        // tag::setting_value[]
        $("form").artist = "Alexander" //<3>
        // end::setting_value[]

        then:
        $("form").artist == "3"
    }
}
