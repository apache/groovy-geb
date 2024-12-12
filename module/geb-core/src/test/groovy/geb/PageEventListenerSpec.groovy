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
package geb

import geb.error.UnexpectedPageException
import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

@SuppressWarnings("ClosureAsLastMethodParameter")
@Unroll
class PageEventListenerSpec extends GebSpecWithCallbackServer {

    PageEventListener listener = Mock(PageEventListener)

    static List<Closure> atMethods() {
        [{ it.&at }, { it.&isAt }]
    }

    def setup() {
        browser.config.pageEventListener = listener
    }

    def "page event listener is called before at checking is performed"() {
        when:
        atMethod(browser)(PageEventListenerSpecPageWithPassingAtChecker)

        then:
        1 * listener.beforeAtCheck(browser, { it instanceof PageEventListenerSpecPageWithPassingAtChecker }) >> { browser, page ->
            assert browser.page != page
        }

        where:
        atMethod << atMethods()
    }

    def "page event listener is called after a successful at check"() {
        when:
        atMethod(browser)(PageEventListenerSpecPageWithPassingAtChecker)

        then:
        1 * listener.onAtCheckSuccess(browser, { it instanceof PageEventListenerSpecPageWithPassingAtChecker }) >> { browser, page ->
            assert browser.page == page
        }
        0 * listener.onAtCheckFailure(*_)

        where:
        atMethod << atMethods()
    }

    def "page event listener is called after an unsuccessful at check for non-implicitly asserted at checker"() {
        when:
        atMethod(browser)(PageEventListenerSpecWithFalseReturningAtChecker)

        then:
        1 * listener.onAtCheckFailure(browser, { it instanceof PageEventListenerSpecWithFalseReturningAtChecker }) >> { browser, page ->
            assert browser.page != page
        }
        0 * listener.onAtCheckSuccess(*_)

        where:
        atMethod << atMethods()
    }

    def "page event listener is called after an unsuccessful at check for implicitly asserted at checker"() {
        when:
        at PageEventListenerSpecWithFailingAtChecker

        then:
        thrown(AssertionError)
        1 * listener.onAtCheckFailure(browser, { it instanceof PageEventListenerSpecWithFailingAtChecker }) >> { browser, page ->
            assert browser.page != page
        }
        0 * listener.onAtCheckSuccess(*_)
    }

    def "page event listener is called after an unsuccessful safe at check for implicitly asserted at checker"() {
        when:
        isAt PageEventListenerSpecWithFailingAtChecker

        then:
        1 * listener.onAtCheckFailure(browser, { it instanceof PageEventListenerSpecWithFailingAtChecker }) >> { browser, page ->
            assert browser.page != page
        }
        0 * listener.onAtCheckSuccess(*_)
    }

    def "page event listener is called when page instance is changed"() {
        when:
        page(PageEventListenerSpecPageWithPassingAtChecker)

        then:
        1 * listener.pageWillChange(browser, null, { it instanceof PageEventListenerSpecPageWithPassingAtChecker })

        when:
        page(PageEventListenerSpecWithFalseReturningAtChecker)

        then:
        1 * listener.pageWillChange(browser, { it instanceof PageEventListenerSpecPageWithPassingAtChecker }, { it instanceof PageEventListenerSpecWithFalseReturningAtChecker })
    }

    def "page event listener is called when an unexpected page is detected"() {
        given:
        browser.config.unexpectedPages = [PageEventListenerSpecPageWithPassingAtChecker]

        when:
        at PageEventListenerSpecWithFailingAtChecker

        then:
        thrown(UnexpectedPageException)

        and:
        1 * listener.unexpectedPageEncountered(browser, { it instanceof PageEventListenerSpecPageWithPassingAtChecker })
    }
}

class PageEventListenerSpecPageWithPassingAtChecker extends Page {
    static at = { true }
}

class PageEventListenerSpecWithFalseReturningAtChecker extends Page {
    // this circumvents implicit assertion AST transformation
    static atChecker = { false }
    static at = atChecker
}

class PageEventListenerSpecWithFailingAtChecker extends Page {
    static at = { false }
}
