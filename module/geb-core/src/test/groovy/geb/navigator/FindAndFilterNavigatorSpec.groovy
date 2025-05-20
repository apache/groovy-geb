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

import geb.test.browsers.CrossBrowser
import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.By
import spock.lang.Issue
import spock.lang.Unroll

@CrossBrowser
class FindAndFilterNavigatorSpec extends GebSpecWithCallbackServer {

    def "find by selector"() {
        given:
        html {
            div(id: "idA", 'class': 'classA', "a")
            div(id: "idB", 'class': 'classB', "b")
        }

        expect:
        $("#idA").text() == "a"
        $(".classA").text() == "a"
        $("#idB").text() == "b"
        $(".classB").text() == "b"
        $(".dontexist").empty
    }

    def "find by a By object selector"() {
        given:
        html {
            div(id: "idA", 'class': 'classA', "a")
            div(id: "idB", 'class': 'classB', "b")
            a(id: "link", 'href': 'groovy.apache.org/geb/', "Geb testing")
        }

        expect:
        $(By.id("idA")).text() == "a"
        $(By.className("classA")).text() == "a"
        $(By.xpath("//div[@id='idA']")).text() == "a"
        $(By.tagName("a")).text() == "Geb testing"
        $(By.id("idB")).text() == "b"
        $(By.className("classB")).text() == "b"
        $(By.className("dontexist")).empty
        $(By.partialLinkText("Geb")).text() == "Geb testing"
        $(By.linkText("Geb testing")).getAttribute("id") == "link"
        $(By.cssSelector("div#idB.classB")).text() == "b"
    }

    @Unroll
    def "nested find using #findMethod"() {
        given:
        html {
            div(id: "a") {
                div(id: "b", class: "nested", "b")
                div(id: "c", class: "nested", "c")
                div(id: "hidden", class: "nested", style: "display: none;", "d")
            }
        }

        expect:
        $("#a")."$findMethod"("#b").text() == "b"
        $("#a")."$findMethod"(By.id("b")).text() == "b"
        $("#a")."$findMethod"("#d").empty
        $("#a")."$findMethod"(By.id("d")).empty
        $("#a")."$findMethod"(text: "b").text() == "b"
        $("#a")."$findMethod"(text: "b", 0).text() == "b"
        $("#a")."$findMethod"(displayed: false).@id == "hidden"
        $("#a")."$findMethod"(displayed: true, 1).text() == "c"
        $("#a")."$findMethod"(class: "nested", 0..1)*.@id == ["b", "c"]
        $("#a")."$findMethod"(class: "nested", "#c").text() == "c"
        $("#a")."$findMethod"(class: "nested", By.id("c")).text() == "c"
        $("#a")."$findMethod"(class: "nested", "div", 1).text() == "c"
        $("#a")."$findMethod"(class: "nested", By.tagName("div"), 1).text() == "c"
        $("#a")."$findMethod"(class: "nested", "div", 0..1)*.@id == ["b", "c"]
        $("#a")."$findMethod"(class: "nested", By.tagName("div"), 0..1)*.@id == ["b", "c"]
        $("#a")."$findMethod"("div", 0).text() == "b"
        $("#a")."$findMethod"(By.tagName("div"), 0).text() == "b"
        $("#a")."$findMethod"("div", 0..1)*.@id == ["b", "c"]
        $("#a")."$findMethod"(By.tagName("div"), 0..1)*.@id == ["b", "c"]

        where:
        findMethod << ['find', '$']
    }

    def "find by attributes"() {
        given:
        html {
            div(a: "1", id: "id1")
            div(a: "2", id: "id2")
        }

        expect:
        $(a: "1")*.@id == ["id1"]
        $(a: "2")*.@id == ["id2"]
        $(a: ~/\d/)*.@id == ["id1", "id2"]
        $(a: ~/\d/, id: "id1")*.@id == ["id1"]
    }

    @Issue("https://github.com/geb/issues/issues/525")
    def "find by GString href attribute"() {
        given:
        def accountId = "1234"

        and:
        html {
            a(href: "/account/$accountId/profile")
        }

        expect:
        $("a", href: "/account/$accountId/profile")
    }

    def "find by selector and class attribute"() {
        given:
        html {
            div(class: "a b c")
        }

        expect:
        $("div", class: "a")
        $("div", class: "a c")
    }

    @Issue("https://github.com/geb/issues/issues/14")
    def "find by attributes passing a class pattern should match any of the classes on an element"() {
        given:
        html {
            (1..3).each {
                div('class': "col-$it", it)
            }
        }

        expect:
        $(class: ~/col-\d/)*.text() == ["1", "2", "3"]
    }

    def "find by node text"() {
        given:
        html {
            p "a"
            p "b"
            p "c"
        }

        expect:
        $(text: "a")*.text() == ["a"]
        $(text: "b")*.text() == ["b"]
        $(text: ~/\w/)*.text() == ["a", "b", "c"]
    }

    def "find by visibility"() {
        given:
        html {
            p(class: "displayed")
            p(class: "hidden", style: "display: none;")
        }

        expect:
        $(displayed: true)*.classes().contains(["displayed"])
        $(displayed: false)*.classes().contains(["hidden"])
    }

    def "selecting with index"() {
        given:
        html {
            div('class': 'a', id: 'a')
            div('class': 'b', id: 'b')
            div('class': 'b', id: 'c')
        }

        expect:
        $("div", 0)*.@id == ["a"]
        $("div", 1)*.@id == ["b"]
        $("div", -1)*.@id == ["c"]
        $(By.tagName("div"), 0)*.@id == ["a"]
        $(By.tagName("div"), 1)*.@id == ["b"]
        $(By.tagName("div"), -1)*.@id == ["c"]
    }

    def "find by selector and attribute"() {
        given:
        html {
            div('class': 'a', name: 'a1', id: 'a1', "")
            div('class': 'a', name: 'a2', id: 'a2', "")
            div('class': 'b', name: 'b1', id: 'b1', "")
            div('class': 'b', name: 'b2', id: 'b2', "")
            div('class': 'c', name: 'c1', id: 'c1', "")
            div('class': 'c', name: 'c2', id: 'c2', "")
        }

        expect:
        $(".a", name: "a1")*.@id == ["a1"]
        $(".b", name: "b2")*.@id == ["b2"]
        $(".c", name: ~/c\d/)*.@id == ["c1", "c2"]
        $(".c", name: "d")*.@id == []
        $(By.className("a"), name: "a1")*.@id == ["a1"]
        $(By.className("b"), name: "b2")*.@id == ["b2"]
        $(By.className("c"), name: ~/c\d/)*.@id == ["c1", "c2"]
        $(By.className("c"), name: "d")*.@id == []
    }

    def "find by selector and text"() {
        given:
        html {
            div('class': 'a', id: 'a1', 'a1')
            div('class': 'a', id: 'a2', 'a2')
            div('class': 'b', id: 'b1', 'b1')
            div('class': 'b', id: 'b2', 'b2')
            div('class': 'c', id: 'c1', 'c1')
            div('class': 'c', id: 'c2', 'c2')
        }

        expect:
        $(".a", text: "a1")*.@id == ["a1"]
        $(".b", text: "b2")*.@id == ["b2"]
        $(".c", text: ~/c\d/)*.@id == ["c1", "c2"]
        $(".c", text: "d")*.@id == []
        $(By.className("a"), text: "a1")*.@id == ["a1"]
        $(By.className("b"), text: "b2")*.@id == ["b2"]
        $(By.className("c"), text: ~/c\d/)*.@id == ["c1", "c2"]
        $(By.className("c"), text: "d")*.@id == []
    }

    def "find by selector and visibility"() {
        given:
        html {
            div('class': 'a', id: 'a1', 'a1')
            div('class': 'a', id: 'a2', 'a2', style: "display: none;")
            div('class': 'b', id: 'b1', 'b1')
            div('class': 'b', id: 'b2', 'b2', style: "display: none;")
        }

        expect:
        $(".a", displayed: true).@id == "a1"
        $("div", displayed: false)*.@id == ["a2", "b2"]
    }

    def filter() {
        given:
        html {
            div(id: "a", "a")
            div(id: "b", "b")
            div(id: "c", "c")
            div(id: "d", "d", style: "display: none;")
        }

        expect:
        $("div").filter("#a")*.@id == ["a"]
        $("div").filter("#a,#b")*.@id == ["a", "b"]
        $("div").filter(id: "a")*.@id == ["a"]
        $("div").filter(text: "a")*.@id == ["a"]
        $("div").filter(text: ~/\w/)*.@id in [
            ["a", "b", "c"],
            ["a", "b", "c", "d"] //most browsers do not return not visible text from WebElement.text() but some do
        ]
        $("div").filter(displayed: false)*.@id == ["d"]
    }

}
