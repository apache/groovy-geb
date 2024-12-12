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
package geb.frame

import geb.test.GebSpecWithCallbackServer

class NestedFrameSupportSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml { request, response ->
            def pageText = (~'/(.*)').matcher(request.requestURI)[0][1]
            head {
                title pageText
            }
            body {
                div("$pageText")
                switch (pageText) {
                    case "top":
                        iframe(src: '/middle')
                        break
                    case "middle":
                        iframe(src: '/bottom')
                        break
                }
            }
        }
    }

    def "context is returned to parent frame rather than default context after withFrame() call"() {
        when:
        go "/top"

        then:
        withFrame($("iframe")) {
            withFrame($("iframe")) {
                assert $("div").text() == "bottom"
            }
            $("div").text()
        } == "middle"

        and:
        $("div").text() == "top"
    }
}
