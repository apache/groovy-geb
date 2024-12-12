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
package pages

import fixture.GebSpecWithServerUsingJavascript
import geb.Page

class ToWaitOptionSpec extends GebSpecWithServerUsingJavascript {

    def "to wait option"() {
        given:
        html """
            <html>
                <body>
                    <button id="load-content"/>
                    ${javascript '''
                        document.getElementById("load-content").addEventListener("click", function() {
                            setTimeout(function() {
                                var p = document.createElement("p");
                                p.setAttribute("id", "async-content");
                                document.body.appendChild(p);
                            }, 300);
                        });
                    '''}
                </body>
            </html>
        """

        expect:
        // tag::to_wait[]
        to PageWithTemplateUsingToWaitOption
        asyncPageLoadButton.click()
        assert page instanceof AsyncPage
        // end::to_wait[]
    }
}

// tag::to_wait_page[]
class PageWithTemplateUsingToWaitOption extends Page {
    static content = {
        asyncPageLoadButton(to: AsyncPage, toWait: true) { $("button#load-content") } //<1>
    }
}

class AsyncPage extends Page {
    static at = { $("#async-content") }
}
// end::to_wait_page[]
