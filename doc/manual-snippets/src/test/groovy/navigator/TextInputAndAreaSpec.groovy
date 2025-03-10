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

class TextInputAndAreaSpec extends GebSpecWithCallbackServer {

    def "manipulating text input value"() {
        given:
        html """
            <html>
                // tag::html[]
                <form>
                    <input type="text" name="language"/>
                    <input type="text" name="description"/>
                </form>
                // end::html[]
            </html>
        """

        when:
        // tag::setting_value[]
        $("form").language = "gro"
        $("form").description = "Optionally statically typed dynamic lang"
        // end::setting_value[]

        then:
        // tag::setting_value[]
        assert $("form").language == "gro"
        assert $("form").description == "Optionally statically typed dynamic lang"
        // end::setting_value[]

        when:
        // tag::typing[]
        $("form").language() << "ovy"
        $("form").description() << "uage"
        // end::typing[]

        then:
        // tag::typing[]
        assert $("form").language == "groovy"
        assert $("form").description == "Optionally statically typed dynamic language"
        // end::typing[]
    }
}
