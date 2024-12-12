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

import geb.module.MultipleSelect
import geb.test.GebSpecWithCallbackServer

class MultipleSelectSnippetSpec extends GebSpecWithCallbackServer {

    def "using multiple select module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <select name="genres" multiple>
                        <option value="1">Alt folk</option>
                        <option value="2">Chiptunes</option>
                        <option value="3">Electroclash</option>
                        <option value="4">G-Funk</option>
                        <option value="5">Hair metal</option>
                    </select>
                </body>
            </html>
            // end::html[]
        """

        when:
        // tag::example[]
        def multipleSelect = $(name: "genres").module(MultipleSelect)
        multipleSelect.selected = ["2", "3"]

        // end::example[]
        then:
        // tag::example[]
        assert multipleSelect.selected == ["2", "3"]
        assert multipleSelect.selectedText == ["Chiptunes", "Electroclash"]

        // end::example[]
        when:
        // tag::example[]
        multipleSelect.selected = ["G-Funk", "Hair metal"]

        // end::example[]
        then:
        // tag::example[]
        assert multipleSelect.selected == ["4", "5"]
        assert multipleSelect.selectedText == ["G-Funk", "Hair metal"]
        // end::example[]
    }
}
