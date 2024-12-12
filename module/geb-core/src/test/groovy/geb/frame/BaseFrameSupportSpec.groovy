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

abstract class BaseFrameSupportSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml { request, response ->
            def pageText = (~'/(.*)').matcher(request.requestURI)[0][1]
            if (pageText == "frames") {
                response.outputStream << "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">"
            }
            head {
                title pageText
            }
            switch (pageText) {
                case "frames":
                    frameset(rows: "25%,75%") {
                        frame(name: 'header', id: 'header-id', src: '/header')
                        frame(id: 'footer', src: '/footer')
                    }
                    break
                case "iframe":
                    body { iframe(id: 'inline', src: '/inline') }
                    break
                default:
                    body { span("$pageText") }
            }
        }
    }

    def setup() {
        to FrameSupportSpecPage
    }

    def cleanup() {
        browser.driver.switchTo().defaultContent()
    }

    protected String getFrameText(frame) {
        withFrame(frame) {
            $("span").text()
        }
    }
}
