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
package downloading

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Page
import org.apache.http.entity.ContentType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DownloadingSpec extends DriveMethodSupportingSpecWithServer {

    def "downloading a file directly"() {
        given:
        server.get = { HttpServletRequest request, HttpServletResponse response ->
            response.characterEncoding = "utf8"
            if (request.requestURI.endsWith("example.pdf")) {
                response.contentType = "text/plain"
                response.outputStream << "downloaded pdf"
            } else {
                response.contentType = ContentType.TEXT_HTML.toString()
                response.outputStream << """
                    <html>
                        <input name="username"/>
                        <input name="password"/>
                        <input type="button" name="login"/>
                        <a id="pdf-download-link" href="/example.pdf">Link</a>
                    </html>
                """
            }
        }

        expect:
        // tag::example[]
        Browser.drive {
            to LoginPage
            login("me", "secret")

            def pdfBytes = downloadBytes(pdfLink.@href)
            // end::example[]
            assert new String(pdfBytes, "UTF-8") == "downloaded pdf"
            // tag::example[]
        }
        // end::example[]
    }
}

// tag::pages[]
class LoginPage extends Page {
    static content = {
        loginButton(to: PageWithPdfLink) { $("input", name: "login") }
    }

    void login(String user, String pass) {
        username = user
        password = pass
        loginButton.click()
    }
}

class PageWithPdfLink extends Page {
    static content = {
        pdfLink { $("a#pdf-download-link") }
    }
}
// end::pages[]
