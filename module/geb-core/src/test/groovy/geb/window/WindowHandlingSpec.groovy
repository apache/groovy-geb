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
package geb.window

import geb.Page
import geb.error.NoNewWindowException
import geb.error.UndefinedAtCheckerException
import geb.error.UnexpectedPageException
import spock.lang.Unroll

import static geb.window.BaseWindowHandlingSpec.MAIN_PAGE_URL

class WindowHandlingSpec extends BaseWindowHandlingSpec {

    @Unroll
    def "ensure withWindow block closure parameter called for all windows for which specification closure returns true"() {
        given:
        openAllWindows()

        when:
        def results = withWindow(specification) { title }

        then:
        results.sort() == returnValues

        where:
        returnValues                                    | specification
        [windowTitle(), windowTitle(1), windowTitle(2)] | { true }
        [windowTitle()]                                 | { title == windowTitle() }
        [windowTitle(1), windowTitle(2)]                | { title in [windowTitle(1), windowTitle(2)] }
    }

    @Unroll
    def "withWindow block closure is called in the context of the page passed as the 'page' option"() {
        given:
        openAllWindows()
        page WindowHandlingSpecMainPage

        when:
        withWindow(page: WindowHandlingSpecNewWindowPage, specification) {
            assert page.getClass() == WindowHandlingSpecNewWindowPage
        }

        then:
        page.getClass() == WindowHandlingSpecMainPage

        where:
        specification << [
            { true },
            { title in [windowTitle(1), windowTitle(2)] },
            windowName(1)
        ]
    }

    def "withWindow block closure is called in the context of the page instance passed as the 'page' option"() {
        given:
        def parametrizedPage = new WindowHandlingSpecParametrizedPage(index: 1)
        openAllWindows()
        page WindowHandlingSpecMainPage

        when:
        withWindow(page: parametrizedPage, windowName(1)) {
            assert page == parametrizedPage
        }

        then:
        page.getClass() == WindowHandlingSpecMainPage
    }

    @Unroll
    def "withWindow by default does not close the matching windows"() {
        given:
        openAllWindows()

        when:
        withWindow(specification) {
        }

        then:
        availableWindows.size() == 3

        where:
        specification << [
            { true },
            windowName(1)
        ]
    }

    @Unroll
    def "withWindow can be configured to close the matching windows"() {
        given:
        openAllWindows()

        and:
        browser.config.withWindowCloseOption = true

        when:
        withWindow(specification) {
        }

        then:
        availableWindows.size() == 2

        where:
        specification << [
                { title == windowTitle(1) },
                windowName(1)
        ]
    }

    @Unroll
    def "close option passed to withWindow overrides the one specified in configuration"() {
        given:
        openAllWindows()

        and:
        browser.config.withWindowCloseOption = true

        when:
        withWindow(specification, close: false) {
        }

        then:
        availableWindows.size() == 3

        where:
        specification << [
                { title == windowTitle(1) },
                windowName(1)
        ]
    }

    @Unroll
    def "withWindow closes matching windows if 'close' option is passed and block closure throws an exception"() {
        given:
        openAllWindows()

        when:
        withWindow(specification, close: true) { throw new Exception() }

        then:
        thrown(Exception)
        availableWindows.size() == 2

        where:
        specification << [
            { title == windowTitle(1) },
            windowName(1),
            { title in [windowTitle(1), windowTitle(2)] }
        ]
    }

    @Unroll("ensure withNewWindow throws an exception when: '#message'")
    def "ensure withNewWindow throws exception if there was none or more than one windows opened"() {
        when:
        withNewWindow(newWindowBlock) {
        }

        then:
        NoNewWindowException e = thrown()
        e.message.startsWith(message)

        where:
        message                                      | newWindowBlock
        'No new window has been opened'              | { }
        'There has been more than one window opened' | { openAllWindows() }
    }

    def "withNewWindow closes the new window even if closure throws an exception"() {
        given:
        go MAIN_PAGE_URL

        when:
        withNewWindow({ openWindow(1) }) { throw new Exception() }

        then:
        thrown(Exception)
        availableWindows.size() == 1
    }

    def "withNewWindow does not close the new window if close option is set to false"() {
        given:
        go MAIN_PAGE_URL

        when:
        withNewWindow({ openWindow(1) }, close: false) {
        }

        then:
        availableWindows.size() == 2
        inContextOfMainWindow
    }

    def "can configure withNewWindow not to close the newly opened window by default"() {
        given:
        go MAIN_PAGE_URL

        and:
        browser.config.withNewWindowCloseOption = false

        when:
        withNewWindow({ openWindow(1) }) {}

        then:
        availableWindows.size() == 2
    }

    def "close option passed to withNewWindow overrides the one specified in configuration"() {
        given:
        go MAIN_PAGE_URL

        and:
        browser.config.withNewWindowCloseOption = false

        when:
        withNewWindow({ openWindow(1) }, close: true) {}

        then:
        availableWindows.size() == 1
    }

    def "withNewWindow block closure is called in the context of the page passed as the 'page' option"() {
        given:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(1) }, page: WindowHandlingSpecNewWindowPage) {
            assert page.getClass() == WindowHandlingSpecNewWindowPage
        }

        then:
        page.getClass() == WindowHandlingSpecMainPage
    }

    def "withNewWindow block closure is called in the context of the page instance passed as the 'page' option"() {
        given:
        def parametrizedPage = new WindowHandlingSpecParametrizedPage(index: 1)
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(1) }, page: parametrizedPage) {
            assert page == parametrizedPage
        }

        then:
        page.getClass() == WindowHandlingSpecMainPage
    }

    def "page context is reverted after a withNewWindow call where block closure throws an exception and 'page' option is present"() {
        given:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(1) }, page: WindowHandlingSpecNewWindowPage) {
            throw new Exception()
        }

        then:
        thrown(Exception)
        page.getClass() == WindowHandlingSpecMainPage
    }

    def "'wait' option can be used in withNewWindow call if the new window opens asynchronously"() {
        given:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({
            js.exec """
                setTimeout(function() {
                    document.getElementById('main-1').click();
                }, 200);
            """
        }, wait: true) {
        }

        then:
        notThrown(NoNewWindowException)
    }

    def "withNewWindow can be configured to wait for the new window to be opened by default"() {
        given:
        browser.config.withNewWindowWaitOption = true

        and:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({
            js.exec """
                setTimeout(function() {
                    document.getElementById('main-1').click();
                }, 75);
            """
        }) {
        }

        then:
        notThrown(NoNewWindowException)
    }

    def "'wait' option passed to withNewWindow overrides the default value from configuration"() {
        given:
        browser.config.withNewWindowWaitOption = false

        and:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({
            js.exec """
                setTimeout(function() {
                    document.getElementById('main-1').click();
                }, 75);
            """
        }, wait: true) {
        }

        then:
        notThrown(NoNewWindowException)
    }

    def "withWindow methods can be nested"() {
        given:
        openAllWindows()

        when: // can't put this in an expect block, some spock bug
        withWindow(windowName(1)) {
            assert title == windowTitle(1)
            openWindow(2)
            withWindow(windowName(1, 2)) {
                assert title == windowTitle(1, 2)
                openWindow(1)
                withWindow(windowName(1, 2, 1)) {
                    assert title == windowTitle(1, 2, 1)
                }
                assert title == windowTitle(1, 2)
            }
            assert title == windowTitle(1)
        }

        then:
        true
    }

    def "withNewWindow methods can be nested"() {
        given:
        openAllWindows()

        when: // can't put this in an expect block, some spock bug
        withWindow(windowName(1)) {
            assert title == windowTitle(1)
            withNewWindow({ openWindow(2) }) {
                assert title == windowTitle(1, 2)
                withNewWindow({ openWindow(1) }) {
                    assert title == windowTitle(1, 2, 1)
                }
                assert title == windowTitle(1, 2)
            }
            assert title == windowTitle(1)
        }

        then:
        true
    }

    @Unroll
    def "withWindow successfully verifies at checker"() {
        given:
        openAllWindows()

        when:
        withWindow(page: WindowHandlingSpecNewWindowWithAtCheckPage, specification) {
        }

        then:
        notThrown(Exception)

        where:
        specification << [
            { title == windowTitle(1) },
            windowName(1)
        ]
    }

    @Unroll
    def "withWindow does not fail if there is no at checker"() {
        given:
        openAllWindows()

        when:
        withWindow(page: WindowHandlingSpecNewWindowPage, specification) {
            assert page.getClass() == WindowHandlingSpecNewWindowPage
        }

        then:
        notThrown(UndefinedAtCheckerException)

        where:
        specification << [
            { title == windowTitle(1) },
            windowName(1)
        ]
    }

    @Unroll
    def "withWindow verifies at checker"() {
        given:
        openAllWindows()

        when:
        withWindow(page: WindowHandlingSpecNewWindowWithAtCheckPage, specification) {
        }

        then:
        thrown(AssertionError)

        where:
        specification << [
            { title == windowTitle(2) },
            windowName(2)
        ]
    }

    @Unroll
    def "withWindow verifies truthy at checker when implicit assertions are disabled"() {
        given:
        openAllWindows()

        when:
        withWindow(page: page, specification) {
        }

        then:
        UnexpectedPageException e = thrown()
        e.message == "At checker page verification failed for page geb.window.WindowHandlingSpecNewWindowWithTruthyAtCheckPage"

        where:
        page                                                   | specification
        WindowHandlingSpecNewWindowWithTruthyAtCheckPage       | { title == windowTitle(2) }
        WindowHandlingSpecNewWindowWithTruthyAtCheckPage       | windowName(2)
        new WindowHandlingSpecNewWindowWithTruthyAtCheckPage() | { title == windowTitle(2) }
        new WindowHandlingSpecNewWindowWithTruthyAtCheckPage() | windowName(2)
    }

    @Unroll
    def "withWindow verifies at checker for a parametrized page instance"() {
        given:
        def parametrizedPage = new WindowHandlingSpecParametrizedPage(index: 1)
        openAllWindows()

        when:
        withWindow(page: parametrizedPage, specification) {
        }

        then:
        thrown(AssertionError)

        where:
        specification << [
            { title == windowTitle(2) },
            windowName(2)
        ]
    }

    def "withNewWindow successfully verifies at checker"() {
        given:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(1) }, page: WindowHandlingSpecNewWindowWithAtCheckPage) {
        }

        then:
        notThrown(Exception)
    }

    def "withNewWindow does not fail if there is no at checker"() {
        to WindowHandlingSpecMainPage

        when:
        def newWindowPage = withNewWindow({ openWindow(1) }, page: WindowHandlingSpecNewWindowPage) {
            page
        }

        then:
        newWindowPage instanceof WindowHandlingSpecNewWindowPage
        notThrown(UndefinedAtCheckerException)
    }

    def "withNewWindow verifies at checker"() {
        given:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(2) }, page: WindowHandlingSpecNewWindowWithAtCheckPage) {
        }

        then:
        thrown(AssertionError)
    }

    @Unroll
    def "withNewWindow verifies truthy at checker when implicit assertions are disabled"() {
        given:
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(2) }, page: page) {
        }

        then:
        UnexpectedPageException e = thrown()
        e.message == "At checker page verification failed for page geb.window.WindowHandlingSpecNewWindowWithTruthyAtCheckPage"

        where:
        page << [WindowHandlingSpecNewWindowWithTruthyAtCheckPage, new WindowHandlingSpecNewWindowWithTruthyAtCheckPage()]
    }

    def "withNewWindow verifies at checker for a parametrized page instance"() {
        given:
        def parametrizedPage = new WindowHandlingSpecParametrizedPage(index: 1)
        to WindowHandlingSpecMainPage

        when:
        withNewWindow({ openWindow(2) }, page: parametrizedPage) {
        }

        then:
        thrown(AssertionError)
    }
}

class WindowHandlingSpecMainPage extends Page {
    static url = MAIN_PAGE_URL
}

class WindowHandlingSpecNewWindowPage extends Page {
}

class WindowHandlingSpecNewWindowWithAtCheckPage extends Page {
    static at = { title == "Window main-1" }
}

class WindowHandlingSpecNewWindowWithTruthyAtCheckPage extends Page {
    // this circumvents implicit assertion AST transformation
    static atChecker = { false }
    static at = atChecker
}

class WindowHandlingSpecParametrizedPage extends Page {
    static at = { title == "Window main-${index}" }

    int index
}
