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
import org.apache.http.entity.ContentType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FineGrainedRequestSpec extends DriveMethodSupportingSpecWithServer {

    def "performing fine grained requests"() {
        given:
        server.get = { HttpServletRequest request, HttpServletResponse response ->
            response.characterEncoding = "utf8"
            if (request.getHeader("Accept") == "application/json") {
                response.contentType = "application/json"
                response.outputStream << "{}"
            } else {
                response.contentType = ContentType.TEXT_HTML.toString()
                response.outputStream << "<html></html>"
            }
        }

        expect:
        // tag::example[]
        Browser.drive {
            go "/"
            def jsonBytes = downloadBytes { HttpURLConnection connection ->
                connection.setRequestProperty("Accept", "application/json")
            }
            // end::example[]
            assert new String(jsonBytes, "UTF-8") == "{}"
            // tag::example[]
        }
        // end::example[]
    }
}
