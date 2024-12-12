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
package pages

import geb.Page
import geb.test.GebSpecWithCallbackServer
import org.apache.http.entity.ContentType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FramesSpec extends GebSpecWithCallbackServer {

    def setup() {
        callbackServer.get = { HttpServletRequest request, HttpServletResponse response ->
            response.contentType = ContentType.TEXT_HTML.toString()
            response.characterEncoding = "utf8"
            if (request.requestURI.endsWith("frame.html")) {
                response.outputStream << """
                    // tag::frame[]
                    <html>
                        <body>
                            <span>frame text</span>
                        </body>
                    </html>
                    // end::frame[]
                """
            } else {
                response.outputStream << """
                    // tag::html[]
                    <html>
                        <body>
                            <iframe name="header" src="frame.html"></iframe>
                            <iframe id="footer" src="frame.html"></iframe>
                            <iframe id="inline" src="frame.html"></iframe>
                            <span>main</span>
                        <body>
                    </html>
                    // end::html[]
                """
            }
        }
    }

    def "using frames"() {
        when:
        // tag::example[]
        to PageWithFrames

        withFrame('header') { assert $('span').text() == 'frame text' }
        withFrame('footer') { assert $('span').text() == 'frame text' }
        withFrame(0) { assert $('span').text() == 'frame text' }
        withFrame($('#footer')) { assert $('span').text() == 'frame text' }
        withFrame(footerFrame) { assert $('span').text() == 'frame text' }

        // end::example[]
        then:
        // tag::example[]
        assert $('span').text() == 'main'
        // end::example[]
    }

    def "switching page class when using frames"() {
        when:
        // tag::switching_class[]
        to PageWithFrames

        withFrame('header', PageDescribingFrame) {
            assert page instanceof PageDescribingFrame
            assert text == "frame text"
        }

        // end::switching_class[]
        then:
        // tag::switching_class[]
        assert page instanceof PageWithFrames
        // end::switching_class[]
    }

    def "switching page instance when using frames"() {
        when:
        // tag::switching_instance[]
        to PageWithFrames

        withFrame('header', new ParameterizedPageDescribingFrame(expectedFrameText: "frame text")) {
            assert page instanceof ParameterizedPageDescribingFrame
        }

        // end::switching_instance[]
        then:
        // tag::switching_instance[]
        assert page instanceof PageWithFrames
        // end::switching_instance[]
    }
}

// tag::page[]
class PageWithFrames extends Page {
    static content = {
        footerFrame { $('#footer') }
    }
}
// end::page[]

// tag::frame_page[]
class PageDescribingFrame extends Page {
    static content = {
        text { $("span").text() }
    }
}
// end::frame_page[]

// tag::parameterized_frame_page[]
class ParameterizedPageDescribingFrame extends Page {
    static at = { text == expectedFrameText }

    static content = {
        text { $("span").text() }
    }

    String expectedFrameText
}
// end::parameterized_frame_page[]
