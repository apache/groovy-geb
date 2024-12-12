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
package browser

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Page

import javax.servlet.http.HttpServletRequest

class PageSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html { HttpServletRequest request ->
            if (request.requestURI.endsWith("/signup")) {
                h1("Signup Page")
            }
        }
    }

    def "using pages"() {
        expect:
        // tag::using_pages[]
        Browser.drive {
            to SignupPage
            assert $("h1").text() == "Signup Page"
            assert page instanceof SignupPage
        }
        // end::using_pages[]
    }

    def "using page content"() {
        expect:
        // tag::using_pages_content[]
        Browser.drive {
            to SignupPage
            assert heading == "Signup Page"
        }
        // end::using_pages_content[]
    }

    def "using to method"() {
        expect:
        // tag::using_to[]
        Browser.drive {
            to SignupPage
        }
        // end::using_to[]
    }
}

// tag::signup_page[]
// tag::signup_page_with_at_checker[]
// tag::signup_page_with_content[]
class SignupPage extends Page {
    static url = "signup"
    // end::signup_page[]

    // end::signup_page_with_content[]
    static at = {
        $("h1").text() == "Signup Page"
    }
    // end::signup_page_with_at_checker[]
    // tag::signup_page_with_content[]
    static content = {
        heading { $("h1").text() }
    }
    // tag::signup_page[]
    // tag::signup_page_with_at_checker[]
}
// end::signup_page[]
// end::signup_page_with_at_checker[]
// end::signup_page_with_content[]
