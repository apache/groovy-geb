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

class MultiSelectSpec extends GebSpecWithCallbackServer {

    def "setting multiple values of a select element"() {
        given:
        html """
            <html>
                // tag::html[]
                <form>
                    <select name="genres" multiple>
                        <option value="1">Alt folk</option>
                        <option value="2">Chiptunes</option>
                        <option value="3">Electroclash</option>
                        <option value="4">G-Funk</option>
                        <option value="5">Hair metal</option>
                    </select>
                </form>
                // end::html[]
            </html>
        """

        when:
        // tag::setting_values[]
        $("form").genres = ["2", "3"] //<1>
        // end::setting_values[]

        then:
        $("form").genres == ["2", "3"]

        when:
        // tag::setting_values[]
        $("form").genres = [1, 4, 5] //<2>
        // end::setting_values[]

        then:
        $("form").genres == ["1", "4", "5"]

        when:
        // tag::setting_values[]
        $("form").genres = ["Alt folk", "Hair metal"] //<3>
        // end::setting_values[]

        then:
        $("form").genres == ["1", "5"]

        when:
        // tag::setting_values[]
        $("form").genres = [] //<4>
        // end::setting_values[]

        then:
        $("form").genres == []
    }
}
