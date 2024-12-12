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

import fixture.GebSpecWithServerUsingJavascript
import geb.Page
import org.apache.http.entity.ContentType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JQuerySupportSpec extends GebSpecWithServerUsingJavascript {

    def setup() {
        callbackServer.get = { HttpServletRequest request, HttpServletResponse response ->
            response.characterEncoding = "utf8"
            if (request.requestURI.endsWith("/js/jquery-2.1.4.min.js")) {
                response.contentType = "text/javascript"
                response.writer << jqueryResource()
            } else {
                response.contentType = ContentType.TEXT_HTML.toString()
                response.writer << '''
                    //tag::html[]
                    <html>
                        <head>
                            <script type="text/javascript" src="/js/jquery-2.1.4.min.js"></script>
                            <script type="text/javascript">
                                $(function() {
                                    $("#a").mouseover(function() {
                                       $("#b").show();
                                    });
                                });
                            </script>
                        </head>
                        <body>
                            <div id="a"></div>
                            <div id="b" style="display:none;"><a href="http://www.gebish.org">Geb!</a></div>
                        </body>
                    </html>
                    //end::html[]
                '''
            }
        }
        go()
    }

    def "calling jquery code via javascript executor"() {
        when:
        // tag::using_javascript_executor[]
        js.exec 'jQuery("div#a").mouseover()'
        // end::using_javascript_executor[]

        then:
        $("#b").displayed
    }

    def "calling jquery code via jquery adapter"() {
        when:
        // tag::using_adapter[]
        $("div#a").jquery.mouseover()
        // end::using_adapter[]

        then:
        $("#b").displayed
    }

    def "using jquery adapter on content"() {
        when:
        // tag::on_content[]
        to JQueryPage
        divA.jquery.mouseover()
        // end::on_content[]

        then:
        // tag::on_content[]
        assert divB.displayed
        // end::on_content[]
    }

    def "calling jquery that takes parameters"() {
        when:
        // tag::with_parameters[]
        $("#a").jquery.trigger('mouseover')
        // end::with_parameters[]

        then:
        $("#b").displayed
    }
}

// tag::page[]
class JQueryPage extends Page {
    static content = {
        divA { $("#a") }
        divB { $("#b") }
    }
}
// end::page[]
