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
package ide

import geb.Page
import geb.navigator.Navigator
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
                    script(type: "text/javascript", """
                        setTimeout(function() {
                            var div = document.createElement("div");
                            div.innerHTML = "added!";
                            div.setAttribute("id", "async");
                            document.body.appendChild(div);
                        }, 100)
                    """)
            }
        }
    }

    def "untyped"() {
        when:
        // tag::untyped[]
        to HomePage
        loginPageLink.click()

        at LoginPage
        username = "user1"
        password = "password1"
        loginButton.click()

        // end::untyped[]

        then:
        // tag::untyped[]
        at SecurePage
        // end::untyped[]
    }

    @SuppressWarnings('UnusedVariable')
    def "typed"() {
        when:
        // tag::typed[]
        HomePage homePage = browser.to HomePage
        homePage.loginPageLink.click()

        LoginPage loginPage = browser.at LoginPage
        // end::typed[]

        then:
        // tag::typed[]
        SecurePage securePage = loginPage.login("user1", "password1")
        // end::typed[]
    }

    def "at"() {
        when:
        // tag::at[]
        HomePage homePage = browser.to HomePage
        homePage.loginPageLink.click()

        browser.at(LoginPage) {
            login("user1", "password1")
        }

        // end::at[]

        then:
        // tag::at[]
        at SecurePage
        // end::at[]
    }

    def "typed content definitions"() {
        when:
        to AsyncPage

        then:
        asyncText() == "added!"
    }
}

// tag::pages[]
class HomePage extends Page {
    Navigator getLoginPageLink() {
        $("#loginLink")
    }
}

class LoginPage extends Page {

    static at = { title == "Login Page" }

    Navigator getLoginButton() {
        $("input", type: "submit")
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

class AsyncPage extends Page {
    //tag::types_with_content[]
    static content = {
        async(wait: true) { $("#async") }
    }

    String asyncText() {
        async.text() //<1>
    }
    //end::types_with_content[]
}