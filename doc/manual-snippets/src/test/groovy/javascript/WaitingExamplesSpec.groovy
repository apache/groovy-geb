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
package javascript

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Page

class WaitingExamplesSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html {
            input(type: "button", value: "Make Request", onclick: """
                setTimeout(function() {
                    var div = document.createElement("div");
                    div.innerHTML = "The Result";
                    div.setAttribute("id", "result");
                    document.body.appendChild(div);
                }, 200)
            """)
        }
    }

    def "using page objects"() {
        expect:
        // tag::using_page[]
        Browser.drive {
            to DynamicPage
            makeRequest()
            assert theResultDiv.text() == "The Result"
        }
        // end::using_page[]
    }

    def "not using page objects"() {
        expect:
        // tag::not_using_page[]
        Browser.drive {
            go "/"
            $("input", value: "Make Request").click()
            waitFor { $("div#result") }
            assert $("div#result").text() == "The Result"
        }
        // end::not_using_page[]
    }

    def "multiline"() {
        expect:
        Browser.drive {
            go "/"
            $("input", value: "Make Request").click()
            // tag::multiline[]
            waitFor {
                def result = $("div#result")
                result.text() == "The Result"
            }
            // end::multiline[]
        }
    }

    def "multiple conditions"() {
        expect:
        Browser.drive {
            go "/"
            $("input", value: "Make Request").click()
            // tag::multiple_conditions[]
            waitFor {
                def result = $("div")
                result.@id == "result"
                result.text() == "The Result"
            }
            // end::multiple_conditions[]
        }
    }
}

// tag::page[]
class DynamicPage extends Page {
    static content = {
        theButton { $("input", value: "Make Request") }
        theResultDiv { $("div#result") }
    }

    def makeRequest() {
        theButton.click()
        waitFor { theResultDiv.present }
    }
}
// end::page[]