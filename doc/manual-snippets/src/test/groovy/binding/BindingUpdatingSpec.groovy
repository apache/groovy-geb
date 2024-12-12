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
package binding

// tag::imports[]
import geb.Browser
import geb.binding.BindingUpdater
// end::imports[]
import geb.test.GebSpecWithCallbackServer
import org.apache.http.entity.ContentType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BindingUpdatingSpec extends GebSpecWithCallbackServer {

    def executeScript(String resourcePath) {
        // tag::example[]
        def binding = new Binding()
        def browser = new Browser()
        // end::example[]
        browser.baseUrl = server.baseUrl
        // tag::example[]
        def updater = new BindingUpdater(binding, browser)

        updater.initialize() //<1>

        def script = getClass().getResource(resourcePath).text
        def result = new GroovyShell(binding).evaluate(script) //<2>

        updater.remove() //<3>
        // end::example[]

        result
    }

    def "using BindingUpdater"() {
        given:
        callbackServer.get = { HttpServletRequest request, HttpServletResponse response ->
            if (request.requestURI.endsWith("some/page")) {
                response.contentType = ContentType.TEXT_HTML.toString()
                response.characterEncoding = "utf8"
                response.outputStream << """
                    <html>
                        <head>
                            <title>Some page</title>
                            <script type="text/javascript">
                                function someJavaScriptFunction() {
                                };
                                setTimeout(function() {
                                    var p = document.getElementById("status")
                                    p.innerHTML = "ready";
                                }, 100);
                            </script>
                        </head>
                        <body>
                            <p id="status"></p>
                            <a class="textFile" href="file.txt"></a>
                        </body>
                    </html>
                """
            }
            if (request.requestURI.endsWith("file.txt")) {
                response.contentType = "text/plain"
                response.outputStream << "text file"
            }
        }

        expect:
        executeScript("/gebScript.groovy") == "text file"
    }

    def "using pages in scripts treated with BindingUpdater"() {
        given:
        html {
            input(type: "button", class: "do-stuff")
        }

        when:
        executeScript("/gebScriptUsingPages.groovy")

        then:
        noExceptionThrown()
    }
}
