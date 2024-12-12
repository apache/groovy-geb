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

class ContentDslToParameterSpec extends DriveMethodSupportingSpecWithServer {

    def "using content dsl to parameter"() {
        given:
        server.html { HttpServletRequest request ->
            switch (request.requestURL.toString()) {
                case ~/.*login/:
                    form(action: "admin") {
                        input(type: "text", name: "username")
                        input(type: "text", name: "password")
                        input(type: "submit", name: "login")
                    }
                    break
                case ~/.*admin/:
                    h1("Admin Page")
                    break
            }
        }

        expect:
        // tag::to_parameter[]
        Browser.drive {
            to LoginPage

            username.value("admin")
            password.value("p4sw0rd")
            loginButton.click()

            assert page instanceof AdminPage
        }
        // end::to_parameter[]
    }
}

// tag::pages[]
class LoginPage extends Page {
    static url = "/login"

    static content = {
        loginButton(to: AdminPage) { $("input", type: "submit") }
        username { $("input", name: "username") }
        password { $("input", name: "password") }
    }
}

class AdminPage extends Page {
    static at = {
        $("h1").text() == "Admin Page"
    }
}
// end::pages[]
