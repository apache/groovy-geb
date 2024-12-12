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

import geb.module.FormElement
import geb.test.GebSpecWithCallbackServer

class FormElementSnippetSpec extends GebSpecWithCallbackServer {

    def "using form element module"() {
        given:
        html """
            // tag::html[]
            <html>
                <body>
                    <input disabled="disabled" name="disabled"/>
                    <input readonly="readonly" name="readonly"/>
                </body>
            </html>
            // end::html[]
        """

        expect:
        // tag::example[]
        assert $(name: "disabled").module(FormElement).disabled
        assert !$(name: "disabled").module(FormElement).enabled
        assert $(name: "readonly").module(FormElement).readOnly
        assert !$(name: "readonly").module(FormElement).editable
        // end::example[]
    }
}
