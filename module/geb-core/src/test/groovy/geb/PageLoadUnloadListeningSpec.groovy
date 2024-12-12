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

import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class PageLoadUnloadListeningSpec extends GebSpecWithCallbackServer {

    @Unroll
    def "change callbacks via #method"(String method, Closure toPage) {
        given:
        html {
            button()
        }

        when:
        to PageLoadUnloadListeningSpecPage1

        then:
        page instanceof PageLoadUnloadListeningSpecPage1

        when:
        def previousPage = page
        fire(toPage, PageLoadUnloadListeningSpecPage2)

        then:
        previousPage.arg.class == PageLoadUnloadListeningSpecPage2
        previousPage.method == 'onUnload'

        and:
        page instanceof PageLoadUnloadListeningSpecPage2
        page.arg.class == PageLoadUnloadListeningSpecPage1
        page.method == 'onLoad'

        where:
        method                     | toPage
        "page method"              | { page(it) }
        "content click"            | { link.click() }
        "to method"                | { to(it) }
        "click with explicit page" | { button.click(it) }
        "click with page list"     | { button.click([PageLoadUnloadListeningSpecPage3, it]) }
    }

    def "there is only one page instance created when passing a page class to to() method"() {
        given:
        html {
            h1("test")
        }

        when:
        to PageLoadUnloadListeningSpecContextPage1

        and:
        context.text = "test"

        then:
        to PageLoadUnloadListeningSpecContextPage2
    }

    private fire(Closure closure, Class<? extends Page> pageClass) {
        closure.delegate = this
        closure.call(pageClass)
    }
}

class PageLoadUnloadListeningSpecPage1 extends Page {
    static content = {
        link(to: PageLoadUnloadListeningSpecPage2) { $("button") }
        button { $("button") }
    }

    def arg
    def method

    void onLoad(Page previousPage) {
        method = 'onLoad'
        arg = previousPage
    }

    void onUnload(Page nextPage) {
        method = 'onUnload'
        arg = nextPage
    }
}

class PageLoadUnloadListeningSpecPage2 extends PageLoadUnloadListeningSpecPage1 {
    static at = { true }
    static content = {
        link(to: PageLoadUnloadListeningSpecPage1) { $("button") }
        button { $("button") }
    }
}

class PageLoadUnloadListeningSpecPage3 extends Page {
    static at = { false }
}

class PageLoadUnloadListeningSpecContextPage extends Page {
    def context = [:]

    void onUnload(Page newPage) {
        newPage.context << context
    }
}

class PageLoadUnloadListeningSpecContextPage1 extends PageLoadUnloadListeningSpecContextPage {
}

class PageLoadUnloadListeningSpecContextPage2 extends PageLoadUnloadListeningSpecContextPage {
    static at = {
        context.text == headerText
    }
    static content = {
        headerText { $("h1").text() }
    }
}
