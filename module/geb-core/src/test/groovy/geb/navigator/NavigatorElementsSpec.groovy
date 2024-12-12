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

import geb.test.browsers.Android
import geb.test.browsers.CrossBrowser
import geb.test.GebSpecWithCallbackServer

@CrossBrowser
@Android
class NavigatorElementsSpec extends GebSpecWithCallbackServer {

    def getElement() {
        given:
        html {
            div(id: "a", "a")
            div(id: "b", "b")
            div(id: "c", "c")
        }

        expect:
        $("div").getElement(0).getAttribute("id") == "a"
        $("div").getElement(1).getAttribute("id") == "b"
        $("foo").getElement(0) == null
        $("foo").getElement(10) == null
    }

    def firstElement() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3", "a")
            div(id: "b", 'class': "b1", "b")
        }

        expect:
        $("div").firstElement().getAttribute("id") == "a"
        $("foo").firstElement() == null
    }

    def lastElement() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3", "a")
            div(id: "b", 'class': "b1", "b")
        }

        expect:
        $("div").lastElement().getAttribute("id") == "b"
        $("foo").lastElement() == null
    }

    def allElements() {
        when:
        html {
            div(id: "a", "a")
            div(id: "b", "b")
            div(id: "c", "c")
        }

        then:
        $("div").allElements()*.getAttribute("id") == ["a", "b", "c"]
    }
}
