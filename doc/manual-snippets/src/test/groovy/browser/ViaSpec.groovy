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
import javax.servlet.http.HttpServletResponse

class ViaSpec extends DriveMethodSupportingSpecWithServer {

    def "using via"() {
        given:
        server.html { HttpServletRequest request, HttpServletResponse response ->
            switch (request.requestURL.toString()) {
                case ~/.*secure/:
                    response.sendRedirect("$server.baseUrl/accessDenied")
                    break
                case ~/.*accessDenied/:
                    head {
                        title("Access denied")
                    }
                    break
            }
        }

        expect:
        // tag::using_via[]
        Browser.drive {
            via SecurePage
            at AccessDeniedPage
        }
        // end::using_via[]
    }
}

class SecurePage extends Page {
    static url = "secure"
}

class AccessDeniedPage extends Page {
    static at = {
        title == "Access denied"
    }
}
