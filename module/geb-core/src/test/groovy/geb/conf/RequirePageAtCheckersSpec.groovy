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
package geb.conf

import geb.PageWithoutAtChecker
import geb.error.UndefinedAtCheckerException
import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class RequirePageAtCheckersSpec extends GebSpecWithCallbackServer {

    def setup() {
        browser.config.requirePageAtCheckers = true
    }

    def pages() {
        [
                [PageWithoutAtChecker, "class"],
                [new PageWithoutAtChecker(), "instance"]
        ]
    }

    @Unroll
    void "exception is thrown when page at checkers are required but page #scenario navigated to does not define one"() {
        when:
        to page

        then:
        thrown(UndefinedAtCheckerException)

        where:
        [page, scenario] << pages()
    }

    @Unroll
    void "exception is thrown when at checkers are required but page #scenario passed to withWindow does not define one"() {
        when:
        withWindow({ true }, page: page) {}

        then:
        thrown(UndefinedAtCheckerException)

        where:
        [page, scenario] << pages()
    }

    @Unroll
    void "exception is thrown when at checkers are required but page #scenario passed to withNewWindow does not define one"() {
        given:
        html {
            a(href: "/", target: "_blank")
        }

        when:
        withNewWindow({ $("a").click() }, page: page) {}

        then:
        thrown(UndefinedAtCheckerException)

        where:
        [page, scenario] << pages()
    }

    @Unroll
    void "exception is thrown when at checkers are required but page #scenario passed to withFrame does not define one"() {
        given:
        html {
            iframe(name: frameName, src: "/")
        }

        when:
        withFrame(frameName, page) {}

        then:
        thrown(UndefinedAtCheckerException)

        where:
        frameName = "test-frame"
        [page, scenario] << pages()
    }

    @Unroll
    void "exception is thrown when at checkers are required but page #scenario passed to click does not define one"() {
        given:
        html {
            button()
        }

        when:
        $("button").click(page)

        then:
        thrown(UndefinedAtCheckerException)

        where:
        [page, scenario] << pages()
    }

}
