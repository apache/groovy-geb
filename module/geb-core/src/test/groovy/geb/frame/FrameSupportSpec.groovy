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

import geb.Module
import geb.Page
import spock.lang.Unroll

class FrameSupportSpec extends BaseFrameSupportSpec {

    def "verify the server is configured correctly for main page"() {
        expect:
        $('frame').size() == 2
    }

    @Unroll("verify that server is configured correctly for frame page: #text")
    def "verify that server is configured correctly for frame pages"() {
        when:
        go "$text"

        then:
        $('span').text() == text

        where:
        text << ['header', 'footer', 'inline']
    }

    def "expect the closure argument passed to withFrame to be executed for navigator as frame identifier"() {
        when:
        boolean called = false
        withFrame($('#footer')) {
            called = true
        }

        then:
        called
    }

    @Unroll
    def "ensure original context is kept after a withFrame call"() {
        when:
        withFrame(frameFactory.call()) {
            assert page in FrameSupportSpecPage
        }

        then:
        inFramesContext

        when:
        withFrame(frameFactory.call()) {
            throw new Exception()
        }

        then:
        thrown(Exception)
        inFramesContext

        where:
        frameFactory << [{ 'header' }, { 0 }, { $('#header-id') }, { page.footer }]
    }

    @Unroll
    def "page is restored to what it was before a withFrame call"() {
        when:
        withFrame(frameFactory.call()) {
            page FrameSupportSpecFramePage
        }

        then:
        page in FrameSupportSpecPage

        when:
        withFrame(frameFactory.call()) {
            page FrameSupportSpecFramePage
            throw new Exception()
        }

        then:
        thrown(Exception)
        page in FrameSupportSpecPage

        where:
        frameFactory << [{ 'header' }, { 0 }, { $('#header-id') }, { page.footer }]
    }

    def "page content with page parameter specified changes the page for the closure body"() {
        when:
        withFrame(footerWithPageParam) {
            assert page in FrameSupportSpecFramePage
        }

        then:
        page in FrameSupportSpecPage

        when:
        withFrame(footerWithPageParam, FrameSupportSpecFramePage) {
            throw new Exception()
        }

        then:
        thrown(Exception)
        page in FrameSupportSpecPage
    }

    @Unroll
    def "page content using #scenario & parametrized page instance changes the page for the closure body"() {
        given:
        def parametrizedPage = new FrameSupportSpecParametrizedPage(tag: "span")

        when:
        withFrame(frameFactory.call(), parametrizedPage) {
            assert page == parametrizedPage
            assert getElementText() == expectedText
        }

        then:
        page in FrameSupportSpecPage

        where:
        scenario              | frameFactory     | expectedText
        "frame name"          | { 'header' }     | "header"
        "index"               | { 0 }            | "header"
        "navigator"           | { $('#footer') } | "footer"
        "simple page content" | { page.footer }  | "footer"
    }

    def "ensure pages and modules have withFrame available"() {
        when:
        to FrameSupportSpecPage

        then:
        page.callAllVariantsOfWithFrame() == 3
        page.returnValueOfWithFrameCallForPageContent == 'footer'
        mod.callAllVariantsOfWithFrame() == 3
    }

    def "ensure page context changes to given frame page only if its at verification is true"() {
        when:
        withFrame(footer, FrameSupportSpecPageWithPassingAtChecker) {
            assert page in FrameSupportSpecPageWithPassingAtChecker
        }

        then:
        page in FrameSupportSpecPage
    }

    def "ensure exception is thrown while changing page context for a given frame page when its at verification fails"() {
        when:
        withFrame(footer, FrameSupportSpecPageWithFailingAtChecker) {
        }

        then:
        thrown(AssertionError)
        page in FrameSupportSpecPage
    }

    def "ensure page context changes to given frame page instance only if its at verification is true"() {
        given:
        def parameterizedPage = new FrameSupportSpecParametrizedPage(tag: "span")

        when:
        withFrame(footer, parameterizedPage) {
            assert page == parameterizedPage
        }

        then:
        page in FrameSupportSpecPage
    }

    def "ensure exception is thrown while changing page context for a given frame page instance when its at verification fails"() {
        given:
        def parameterizedPage = new FrameSupportSpecParametrizedPage(tag: "test")

        when:
        withFrame(footer, parameterizedPage) {
        }

        then:
        thrown(AssertionError)
        page in FrameSupportSpecPage
    }

    def "can access frame contents from within a module using default delegate of the block passed to withFrame"() {
        when:
        to FrameSupportSpecPage

        then:
        mod.footerText == "footer"
    }

    @Unroll
    def "value returned by the closure passed as the last argument is returned from withFrame"() {
        expect:
        withFrame(frameFactory.call()) { 'from closure' } == 'from closure'

        where:
        frameFactory << [{ 'header' }, { 0 }, { $('#header-id') }, { page.footer }]
    }

    def "no method dispatch errors happen when passing page content instance and root Page class to withFrame"() {
        when:
        withFrame(footer, Page) {}

        then:
        notThrown(MissingMethodException)
    }

    private boolean isInFramesContext() {
        title == 'frames'
    }

}

class FrameSupportSpecPage extends Page {
    static url = "/frames"
    static content = {
        footer { $('#footer') }
        footerWithPageParam(page: FrameSupportSpecFramePage) { footer }
        mod { module FrameSupportSpecModule }
    }

    int callAllVariantsOfWithFrame() {
        def count = 0
        def block = { count++ }
        withFrame(0, block)
        withFrame('header', block)
        withFrame(footer, block)
        count
    }

    String getReturnValueOfWithFrameCallForPageContent() {
        withFrame(footer) { $('span').text() }
    }
}

class FrameSupportSpecFramePage extends Page {
}

class FrameSupportSpecModule extends Module {
    int callAllVariantsOfWithFrame() {
        def count = 0
        def block = { count++ }
        withFrame(0, block)
        withFrame('header', block)
        withFrame(find('#footer'), block)
        count
    }

    String getFooterText() {
        withFrame('footer', FrameSupportSpecPageWithPassingAtChecker) {
            span.text()
        }
    }
}

class FrameSupportSpecParametrizedPage extends Page {
    static at = { element }
    static content = {
        element { $(tag) }
    }

    String tag

    String getElementText() {
        element.text()
    }
}

class FrameSupportSpecPageWithPassingAtChecker extends Page {
    static at = { span.text() == "footer" }

    static content = {
        span { $("span") }
    }
}

class FrameSupportSpecPageWithFailingAtChecker extends Page {
    static at = { false }
}

