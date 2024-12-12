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
package intellij

import geb.Page
import geb.test.GebSpecWithCallbackServer

import javax.servlet.http.HttpServletRequest

class StrongTypingSpec extends GebSpecWithCallbackServer {

    def setup() {
        html { HttpServletRequest request ->
            switch (request.requestURI) {
                case ~'/loginPage$':
                    head {
                        title "Login Page"
                    }
                    form(action: "/login") {
                        input(type: "text", name: "username")
                        input(type: "password", name: "password")
                        input(type: "submit", value: "login")
                    }
                    break
                case ~'/login$':
                    head {
                        title "Secure Page"
                    }
                    break
                default:
                    a(href: "/loginPage", id: "loginLink", "Login")
            }
        }
    }

    @SuppressWarnings('UnusedVariable')
    def "typed"() {
        when:
        // tag::typed[]
        def homePage = to HomePage
        homePage.loginPageLink.click()

        def loginPage = at LoginPage
        // end::typed[]

        then:
        // tag::typed[]
        def securePage = loginPage.login("user1", "password1")
        // end::typed[]
    }
}

// tag::pages[]
class HomePage extends Page {
    static content = {
        loginPageLink { $("#loginLink") }
    }
}

class LoginPage extends Page {

    static at = { title == "Login Page" }

    static content = {
        loginButton { $("input", type: "submit") }
    }

    SecurePage login(String username, String password) {
        $(name: "username").value username
        $(name: "password").value password
        loginButton.click()
        browser.at SecurePage
    }
}

class SecurePage extends Page {
    static at = { title == "Secure Page" }
}
// end::pages[]