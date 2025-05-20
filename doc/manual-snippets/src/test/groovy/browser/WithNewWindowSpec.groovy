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

class WithNewWindowSpec extends DriveMethodSupportingSpecWithServer {

    def "using withNewWindow"() {
        given:
        server.responseHtml """
            // tag::html[]
            <a href="http://groovy.apache.org/geb/" target="_blank">Geb</a>
            // end::html[]
        """

        expect:
        // tag::new_window[]
        Browser.drive {
            go()
            withNewWindow({ $('a').click() }) {
                assert title == 'Geb - Very Groovy Browser Automation'
            }
        }
        // end::new_window[]
    }

    def "using withNewWindow with waiting"() {
        given:
        server.responseHtml """
            <head>
                <script type="text/javascript">
                    // tag::js[]
                    function openNewWindow() {
                        setTimeout(function() {
                            document.getElementById('new-window-link').click();
                        }, 200);
                    }
                    // end::js[]
                </script>
            </head>
            // tag::waiting_html[]
            <a href="http://google.com" target="_blank" id="new-window-link">Google</a>
            // end::waiting_html[]
        """

        expect:
        // tag::new_window_with_wait[]
        Browser.drive {
            go()
            withNewWindow({ js.openNewWindow() }, wait: true) {
                // end::new_window_with_wait[]
                waitFor { title } // loading the page in a new window is not a blocking operation?
                // tag::new_window_with_wait[]
                assert title == 'Google'
            }
        }
        // end::new_window_with_wait[]
    }
}
