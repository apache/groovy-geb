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

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
// tag::import[]
import geb.Page

// end::import[]
import javax.servlet.http.HttpServletRequest

class PageObjectPatternSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html { HttpServletRequest request ->
            if (request.requestURI.endsWith("search")) {
                head {
                    title("Search engine")
                }
                body {
                    form(action: "results") {
                        input(type: "text", name: "q")
                        input(type: "submit", value: "Search")
                    }
                }
            } else {
                head {
                    title("Results")
                }
                body {
                    ul {
                        li(request.getParameter("q"))
                    }
                }
            }
        }
    }

    def "using navigators"() {
        expect:
        // tag::using_navigators[]
        Browser.drive {
            go "search"
            $("input[name='q']").value "Chuck Norris"
            $("input[value='Search']").click()
            assert $("li", 0).text().contains("Chuck")
        }
        // end::using_navigators[]
    }

    def "using page objects"() {
        expect:
        // tag::using_pages[]
        Browser.drive {
            to SearchPage
            search "Chuck Norris"
            assert result(0).text().contains("Chuck")
        }
        // end::using_pages[]
    }
}

// tag::pages[]
class SearchPage extends Page {
    static url = "search"
    static at = { title == "Search engine" }
    static content = {
        searchField { $("input[name=q]") }
        searchButton(to: ResultsPage) { $("input[value='Search']") }
    }

    void search(String searchTerm) {
        searchField.value searchTerm
        searchButton.click()
    }
}

class ResultsPage extends Page {
    static at = { title == "Results" }
    static content = {
        results { $("li") }
        result { index -> results[index] }
    }
}
// end::pages[]