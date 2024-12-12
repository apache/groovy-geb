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
package geb.navigator

import geb.Page
import geb.test.GebSpecWithCallbackServer
import geb.waiting.Wait
import spock.lang.Unroll

class EmptyNavigatorSpec extends GebSpecWithCallbackServer {

    Navigator navigator

    def setup() {
        html {}
        navigator = $("#not-existing")
    }

    def find() {
        expect:
        navigator.find("foo").empty
        navigator.find("foo", 0).empty
        navigator.find("foo", 0..1).empty
        navigator.find("foo", 0, a: "b").empty
        navigator.find("foo", 0..1, a: "b").empty
        navigator.find("foo", 0..<0, a: "b").empty
        navigator.find("foo", a: "b").empty
        navigator.find(0, a: "b").empty
        navigator.find(0..1, a: "b").empty
        navigator.find(a: "b").empty
    }

    def getAt() {
        expect:
        navigator[1].empty
        navigator[1..10].empty
        navigator[0..<0].empty
    }

    def head() {
        expect:
        navigator.head().empty
    }

    def first() {
        expect:
        navigator.first().empty
    }

    def hasClass() {
        expect:
        !navigator.hasClass("any-class")
    }

    def is() {
        expect:
        !navigator.is("div")
    }

    def last() {
        expect:
        navigator.last().empty
    }

    def tail() {
        expect:
        navigator.tail().empty
    }

    def remove() {
        expect:
        navigator.remove(0).empty
        navigator.remove(1).empty
    }

    def isDisplayed() {
        expect:
        !navigator.displayed
    }

    def tag() {
        expect:
        navigator.tag() == null
    }

    def text() {
        expect:
        navigator.text() == null
    }

    def getAttribute() {
        expect:
        navigator.getAttribute("href") == null
    }

    def classes() {
        expect:
        navigator.classes() == []
    }

    def value() {
        expect:
        navigator.value() == null
    }

    def verifyNotEmpty() {
        when:
        navigator.verifyNotEmpty()

        then:
        thrown(EmptyNavigatorException)
    }

    def methodMissing() {
        expect:
        navigator.username().empty
    }

    @Unroll('click() for args: #args')
    def click() {
        when:
        navigator.click(*args)

        then:
        UnsupportedOperationException e = thrown()
        e.message == "not supported on empty navigator objects"

        where:
        args << [
                [],
                [Page],
                [Page, new Wait()],
                [new Page()],
                [new Page(), new Wait()],
                [[Page]],
                [[Page], new Wait()]
        ]
    }

}
