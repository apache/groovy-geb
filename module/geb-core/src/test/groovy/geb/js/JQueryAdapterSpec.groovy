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
package geb.js

import geb.test.*

class JQueryAdapterSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        def jquery = getClass().getResource("/jquery-1.4.2.min.js")

        callbackServer.get = { req, res ->
            res.outputStream << """
                <html>
                <head>
                    <script type="text/javascript">
                        ${jquery.openStream().text}
                    </script>
                    <script type="text/javascript">
                        var i = false;
                        \$(function() {
                            \$("#a").click(function() {
                                i = true;
                            })
                        });
                    </script>
                </head>
                <body>
                    <div id="a"></div>
                    <div id="b" class="x"></div>
                </body>
                </html>
            """
        }
    }

    def setup() {
        go()
    }

    def "simple method"() {
        expect:
        js.i == false
        when:
        $("div#a").jquery.click()
        then:
        js.i == true
    }

    def "method with arg"() {
        expect:
        js.i == false
        when:
        $("div#a").jquery.trigger('click')
        then:
        js.i == true
    }

    def "String return value"() {
        expect:
        $("#a").jquery.attr("id") == "a"
    }

    def "Navigator return value"() {
        expect:
        $("#a").jquery.next().@id == "b"
    }

    def "boolean return value"() {
        expect:
        $("#a").jquery.hasClass("x") == false
        $("#b").jquery.hasClass("x") == true
    }

    def "int return value"() {
        expect:
        $("div").jquery.size() == 2
    }

    def "non jquery object return value"() {
        expect:
        $("#a").jquery.offset().top instanceof Number
    }
}
