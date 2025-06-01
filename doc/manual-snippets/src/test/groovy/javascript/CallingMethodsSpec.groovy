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

class CallingMethodsSpec extends DriveMethodSupportingSpecWithServer {

    def "calling methods"() {
        given:
        server.html """
            // tag::calling_methods_html[]
            <html>
                <head>
                    <script type="text/javascript">
                        function addThem(a,b) {
                            return a + b;
                        }
                    </script>
                </head>
            </html>
            // end::calling_methods_html[]
        """

        expect:
        // tag::calling_methods[]
        Browser.drive {
            go "/"
            assert js.addThem(1, 2) == 3
        }
        // end::calling_methods[]
    }

    def "calling nested methods"() {
        given:
        server.html """
            // tag::nested_methods_html[]
            <html>
                <head>
                    <script type="text/javascript">
                        functionContainer = {
                            addThem: function(a,b) {
                                return a + b;
                            }
                        }
                    </script>
                </head>
            </html>
            // end::nested_methods_html[]
        """

        expect:
        // tag::nested_methods[]
        Browser.drive {
            go "/"
            assert js."functionContainer.addThem"(1, 2) == 3
        }
        // end::nested_methods[]
    }
}
