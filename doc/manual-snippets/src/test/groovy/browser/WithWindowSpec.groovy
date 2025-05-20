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

class WithWindowSpec extends DriveMethodSupportingSpecWithServer {

    def "withWindow using name"() {
        given:
        server.responseHtml """
            // tag::using_name_html[]
            <a href="http://groovy.apache.org/geb/" target="myWindow">Geb</a>
            // end::using_name_html[]
        """

        expect:
        // tag::using_name[]
        Browser.drive {
            go()
            $("a").click()
            withWindow("myWindow") {
                assert title == "Geb - Very Groovy Browser Automation"
            }
        }
        // end::using_name[]
    }

    def "withWindow using closure"() {
        given:
        server.responseHtml """
            // tag::using_closure_html[]
            <a href="http://groovy.apache.org/geb/" target="_blank">Geb</a>
            // end::using_closure_html[]
        """

        expect:
        // tag::using_closure[]
        Browser.drive {
            go()
            $("a").click()
            withWindow({ title == "Geb - Very Groovy Browser Automation" }) {
                assert $(".slogan").text().startsWith("Very Groovy browser automation.")
            }
        }
        // end::using_closure[]
    }
}
