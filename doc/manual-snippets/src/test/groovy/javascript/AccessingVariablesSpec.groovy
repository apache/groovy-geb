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
package javascript

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Page

class AccessingVariablesSpec extends DriveMethodSupportingSpecWithServer {

    def htmlWithJsVariable() {
        server.html """
            // tag::html[]
            <html>
                <head>
                    <script type="text/javascript">
                        var aVariable = 1;
                    </script>
                </head>
            </html>
            // end::html[]
        """
    }

    def "accessing a variable"() {
        given:
        htmlWithJsVariable()

        expect:
        // tag::accessing[]
        Browser.drive {
            go "/"
            assert js.aVariable == 1
        }
        // end::accessing[]
    }

    def "mapping a variable to content"() {
        given:
        htmlWithJsVariable()

        expect:
        // tag::mapping[]
        Browser.drive {
            to JsVariablePage
            assert aVar == 1
        }
        // end::mapping[]
    }

    def "accessing nested variables"() {
        given:
        server.html {
            title "Book of Geb"
        }

        expect:
        Browser.drive {
            go()
            // tag::nested[]
            assert js."document.title" == "Book of Geb"
            // end::nested[]
        }
    }
}

// tag::mapping_class[]
class JsVariablePage extends Page {
    static content = {
        aVar { js.aVariable }
    }
}
// end::mapping_class[]
