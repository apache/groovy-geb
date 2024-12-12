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
import geb.error.ContentCountOutOfBoundsException
import geb.error.RequiredPageContentNotPresent
import geb.test.GebSpecWithCallbackServer
import org.apache.http.entity.ContentType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static geb.Browser.UTF8

class TemplateOptionsSpec extends GebSpecWithCallbackServer {

    def "example of using content template options"() {
        given:
        html { }

        when:
        to TemplateOptionsIntroductionPage

        then:
        noExceptionThrown()
    }

    def "required"() {
        given:
        html { }

        expect:
        // tag::required[]
        to PageWithTemplatesUsingRequiredOption

        assert !notRequiredDiv

        def thrown = false
        try {
            page.requiredDiv
        } catch (RequiredPageContentNotPresent e) {
            thrown = true
        }
        assert thrown
        // end::required[]
    }

    def "cache"() {
        given:
        html { }

        expect:
        // tag::cache[]
        to PageWithTemplateUsingCacheOption
        assert notCachedValue == 1
        assert cachedValue == 1

        // end::cache[]

        when:
        // tag::cache[]
        value = 2

        // end::cache[]

        then:
        // tag::cache[]
        assert notCachedValue == 2
        assert cachedValue == 1
        // end::cache[]
    }

    def "to"() {
        given:
        html {
            a("Help")
        }

        expect:
        // tag::to[]
        to PageWithTemplateUsingToOption
        helpLink.click()
        assert page instanceof HelpPage
        // end::to[]
    }

    def "list to"() {
        given:
        html {
            input(class: "loginButton")
        }

        when:
        to PageWithTemplateUsingListToOption
        loginButton.click()

        then:
        assert page instanceof LoginFailedPage
    }

    def "waiting content"() {
        given:
        dynamicHtml()

        expect:
        // tag::waiting_content[]
        to DynamicPageWithWaiting
        assert dynamicallyAdded.text() == "I'm here now"
        // end::waiting_content[]
    }

    def "wrapped waiting content"() {
        given:
        dynamicHtml()

        expect:
        // tag::wrapped_waiting_content[]
        to DynamicPageWithoutWaiting
        assert waitFor { dynamicallyAdded }.text() == "I'm here now"
        // end::wrapped_waiting_content[]
    }

    def "non navigator waiting content"() {
        given:
        dynamicHtml("Success", "status")

        expect:
        // tag::non_navigator_waiting[]
        to DynamicPageWithNonNavigatorWaitingContent
        assert success
        // end::non_navigator_waiting[]
    }

    def "not required waiting content"() {
        given:
        html { }
        browser.config.defaultWaitTimeout = 0.2

        when:
        to DynamicPageWithNotRequiredWait

        then:
        dynamicallyAdded.empty
    }

    def "waitCondition content"() {
        given:
        def jquery = getClass().getResource("/jquery-2.1.4.min.js")
        html """
            <html>
                <head>
                    <script type="text/javascript">
                    $jquery.text
                    </script>
                    <script type="text/javascript">
                        setTimeout(function() {
                            \$("p").show();
                        }, 500);
                    </script>
                </head>
                <body>
                    <p class="dynamic" style="display: none;">Dynamically shown paragraph</p>
                </body>
            </html>
        """

        when:
        to(PageWithTemplateUsingWaitConditionOption)

        then:
        // tag::wait_condition_page_content_access[]
        dynamicallyShown
        // end::wait_condition_page_content_access[]
                .displayed
    }

    def "page option"() {
        callbackServer.get = { HttpServletRequest request, HttpServletResponse response ->
            response.contentType = ContentType.TEXT_HTML.toString()
            response.characterEncoding = UTF8
            if (request.requestURI.endsWith("frame.html")) {
                response.writer << """
                    // tag::frame_html[]
                    <html>
                        <body>
                            <span>frame text</span>
                        </body>
                    </html>
                    // end::frame_html[]
                """
            } else {
                response.writer << """
                    // tag::page_html[]
                    <html>
                        <body>
                            <iframe id="frame-id" src="frame.html"></iframe>
                        <body>
                    </html>
                    // end::page_html[]
                """
            }
        }

        expect:
        // tag::page[]
        to PageWithFrame
        withFrame(myFrame) {
            assert frameContentsText == 'frame text'
            // end::page[]
            true
            // tag::page[]
        }
        // end::page[]
    }

    def "min option"() {
        given:
        html """
            // tag::min_html[]
            <html>
                <body>
                    <p>first paragraph</p>
                    <p>second paragraph</p>
                <body>
            </html>
            // end::min_html[]
        """
        def exceptionMessage =
        // tag::min_exception_message[]
        "Page content 'pages.PageWithTemplateUsingMinOption -> atLeastThreeElementNavigator: geb.navigator.DefaultNavigator' should return a navigator with at least 3 elements but has returned a navigator with 2 elements"
        // end::min_exception_message[]

        when:
        to(PageWithTemplateUsingMinOption).atLeastThreeElementNavigator

        then:
        ContentCountOutOfBoundsException e = thrown()
        e.message == exceptionMessage
    }

    def "max option"() {
        given:
        html """
            // tag::max_html[]
            <html>
                <body>
                    <p>first paragraph</p>
                    <p>second paragraph</p>
                    <p>third paragraph</p>
                    <p>fourth paragraph</p>
                <body>
            </html>
            // end::max_html[]
        """
        def exceptionMessage =
        // tag::max_exception_message[]
        "Page content 'pages.PageWithTemplateUsingMaxOption -> atMostThreeElementNavigator: geb.navigator.DefaultNavigator' should return a navigator with at most 3 elements but has returned a navigator with 4 elements"
        // end::max_exception_message[]

        when:
        to(PageWithTemplateUsingMaxOption).atMostThreeElementNavigator

        then:
        ContentCountOutOfBoundsException e = thrown()
        e.message == exceptionMessage
    }

    def "times option"() {
        given:
        html """
            // tag::times_html[]
            <html>
                <body>
                    <p>first paragraph</p>
                    <p>second paragraph</p>
                    <p>third paragraph</p>
                    <p>fourth paragraph</p>
                <body>
            </html>
            // end::times_html[]
        """
        def exceptionMessage =
        // tag::times_exception_message[]
        "Page content 'pages.PageWithTemplateUsingTimesOption -> twoToThreeElementNavigator: geb.navigator.DefaultNavigator' should return a navigator with at most 3 elements but has returned a navigator with 4 elements"
        // end::times_exception_message[]

        when:
        to(PageWithTemplateUsingTimesOption).twoToThreeElementNavigator

        then:
        ContentCountOutOfBoundsException e = thrown()
        e.message == exceptionMessage
    }

    private void dynamicHtml(String text = "I'm here now", String className = "dynamic") {
        html """
            <html>
                <head>
                    <script type="text/javascript">
                        setTimeout(function() {
                            var p = document.createElement("p");
                            p.innerHTML = "$text";
                            p.className = "$className"
                            document.body.appendChild(p);
                        }, 200);
                    </script>
                </head>
                <body></body>
            </html>
        """
    }
}

class TemplateOptionsIntroductionPage extends Page {
    static content = {
        // tag::introduction[]
        theDiv(cache: false, required: false) { $("div", id: "a") }
        // end::introduction[]
    }
}

// tag::required_page[]
class PageWithTemplatesUsingRequiredOption extends Page {
    static content = {
        requiredDiv { $("div", id: "b") }
        notRequiredDiv(required: false) { $("div", id: "b") }
    }
}
// end::required_page[]

// tag::cache_page[]
class PageWithTemplateUsingCacheOption extends Page {
    static content = {
        notCachedValue { value }
        cachedValue(cache: true) { value }
    }
    def value = 1
}
// end::cache_page[]

// tag::to_page[]
class PageWithTemplateUsingToOption extends Page {
    static content = {
        helpLink(to: HelpPage) { $("a", text: "Help") }
    }
}

class HelpPage extends Page { }
// end::to_page[]

class PageWithTemplateUsingListToOption extends Page {
    // tag::to_list_page[]
    static content = {
        loginButton(to: [LoginSuccessfulPage, LoginFailedPage]) { $("input.loginButton") }
    }
    // end::to_list_page[]
}

class LoginSuccessfulPage extends Page {
    static at = { false }
}

class LoginFailedPage extends Page {
    static at = { true }
}

// tag::waiting_page[]
class DynamicPageWithWaiting extends Page {
    static content = {
        dynamicallyAdded(wait: true) { $("p.dynamic") }
    }
}
// end::waiting_page[]

// tag::not_waiting_page[]
class DynamicPageWithoutWaiting extends Page {
    static content = {
        dynamicallyAdded { $("p.dynamic") }
    }
}
// end::not_waiting_page[]

// tag::non_navigator_waiting_page[]
class DynamicPageWithNonNavigatorWaitingContent extends Page {
    static content = {
        status { $("p.status") }
        success(wait: true) { status.text().contains("Success") }
    }
}
// end::non_navigator_waiting_page[]

class DynamicPageWithNotRequiredWait extends Page {
    // tag::not_required_waiting_page[]
    static content = {
        dynamicallyAdded(wait: true, required: false) { $("p.dynamic") }
    }
    // end::not_required_waiting_page[]
}

// tag::page_page[]
class PageWithFrame extends Page {
    static content = {
        myFrame(page: FrameDescribingPage) { $('#frame-id') }
    }
}

class FrameDescribingPage extends Page {
    static content = {
        frameContentsText { $('span').text() }
    }
}
// end::page_page[]

class PageWithTemplateUsingMinOption extends Page {
    static content = {
        // tag::min_option[]
        atLeastThreeElementNavigator(min: 3) { $('p') }
        // end::min_option[]
    }
}

class PageWithTemplateUsingMaxOption extends Page {
    static content = {
        // tag::max_option[]
        atMostThreeElementNavigator(max: 3) { $('p') }
        // end::max_option[]
    }
}

class PageWithTemplateUsingTimesOption extends Page {
    static content = {
        // tag::times_option[]
        twoToThreeElementNavigator(times: 2..3) { $('p') }
        // end::times_option[]
    }
}

class PageWithTemplateUsingWaitConditionOption extends Page {
    // tag::wait_condition_page[]
    static content = {
        dynamicallyShown(waitCondition: { it.displayed }) { $("p.dynamic") }
    }
    // end::wait_condition_page[]
}