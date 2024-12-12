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

import javax.servlet.http.HttpServletRequest

class DriveSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html { HttpServletRequest request ->
            if (request.requestURI.endsWith("/signup")) {
                h1("Signup Page")
            }
        }
    }

    def "signup"() {
        expect:
        // tag::using_drive[]
        Browser.drive {
            go "signup"
            assert $("h1").text() == "Signup Page"
        }
        // end::using_drive[]

        and:
        // tag::explicit[]
        def browser = new Browser()
        browser.go "signup"
        assert browser.$("h1").text() == "Signup Page"
        // end::explicit[]
    }

    def "calling methods on browser is like calling them on the page"() {
        expect:
        // tag::browser_delegates_to_page[]
        Browser.drive {
            go "signup"

            assert $("h1").text() == "Signup Page" //<1>
            assert page.$("h1").text() == "Signup Page" //<1>
        }
        // end::browser_delegates_to_page[]
    }

}

