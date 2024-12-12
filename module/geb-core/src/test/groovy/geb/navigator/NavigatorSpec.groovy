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
import geb.test.browsers.CrossBrowser
import org.openqa.selenium.By
import spock.lang.Unroll

@Unroll
@CrossBrowser
class NavigatorSpec extends GebSpecWithCallbackServer {

    def truthiness() {
        given:
        html { div() }

        expect:
        $("div")
        !$("foo")
    }

    def "reading attributes"() {
        given:
        html {
            select(id: "single", "")
            select(id: "multi", multiple: "multiple", "")
        }

        expect:
        $("#single").getAttribute("multiple") == ""
        $("#single").attr("multiple") == ""
        $("#single").@multiple == ""
        $("#single").attr('multiple') == ""
        $("#multi").getAttribute("multiple") == "true"
        $("#multi").attr("multiple") == "true"
        $("#multi").@multiple == "true"
        $("#multi").attr('multiple') == "true"
    }

    def add() {
        when:
        html {
            div(id: "a")
            div(id: "b")
            div(id: "c")
        }

        then:
        $("#a").add("#c")*.@id == ["a", "c"]
        $("#c").add("#a")*.@id == ["c", "a"]
        $("foo").add("#a")*.@id == ["a"]
    }

    def remove() {
        when:
        html {
            div(id: "a")
            div(id: "b")
            div(id: "c")
        }

        then:
        $("div").remove(1)*.@id == ["a", "c"]
        $("div").remove(5)*.@id == ["a", "b", "c"]
        $("foo").remove(5)*.@id == []
    }

    def has() {
        when:
        html {
            div(id: "a") {
                div("class": "a-1 z-1", "a")
                input(type: "text")
            }
            div(id: "b") {
                div("class": "b-1 z-1", "b")
                input(name: "someName", type: "checkbox")
            }
            div(id: "c") {
                div("class": "c-1 z-1", "c")
                input(type: "text")
            }
            div(id: "d") {
                div(style: "display: none;", "d")
            }
            div(id: "e") {
                p(style: "display: none;", "e")
            }
        }

        then:
        $("div").has(".z-1")*.@id == ["a", "b", "c"]
        $("div").has(".b-1")*.@id == ["b"]
        $("div").has("input", name: "someName")*.@id == ["b"]
        $("div").has("div", text: "b")*.@id == ["b"]
        $("div").has("input", type: "text")*.@id == ["a", "c"]
        $("div").has(text: ~/[abc]/)*.@id == ["a", "b", "c"]
        $("div").has(displayed: false)*.@id == ["d", "e"]
        $("div").has("div", displayed: false)*.@id == ["d"]
    }

    def hasNot() {
        when:
        html {
            div(id: "a") {
                input(class: "a-1 z-1", type: "text")
            }
            div(id: "b") {
                input(class: "b-1 z-1", name: "someName", type: "checkbox")
            }
            div(id: "c") {
                input(class: "c-1 z-1", type: "text")
            }
        }

        then:
        $("div").hasNot(".z-1").size() == 0
        $("div").hasNot(".b-1")*.@id == ["a", "c"]
        $("div").hasNot("input", name: "someName")*.@id == ["a", "c"]
        $("div").hasNot("input", type: "text")*.@id == ["b"]
        $("div").hasNot(type: "text")*.@id == ["b"]
        $("div").hasNot(By.className("b-1"))*.@id == ["a", "c"]
        $("div").hasNot(By.tagName("input")).size() == 0
        $("div").hasNot(By.tagName("input"), name: "someName")*.@id == ["a", "c"]
    }

    def not() {
        when:
        html {
            div(id: "a", 'class': 'z', 'hello')
            div(id: "b", 'class': 'x', 'hello')
            div(id: "c", 'class': 'z', style: 'display: none;')
        }

        then:
        $("div").not("#b")*.@id == ["a", "c"]
        $("div").not("foo")*.@id == ["a", "b", "c"]
        $("foo").not("foo")*.@id == []
        $("div").not(text: ~/.+/)*.@id == ["c"]
        $("div").not(".z", text: 'hello')*.@id == ["b", "c"]
        $("div").not(displayed: true)*.@id == ["c"]
        $("div").not(".z", displayed: false)*.@id == ["a", "b"]
    }

    def plus() {
        when:
        html {
            div(id: "a")
            div(id: "b")
            div(id: "c")
        }

        then:
        ($("#a") + $("#b"))*.@id == ["a", "b"]
        ($("#a") + $("foo"))*.@id == ["a"]
        ($("foo") + $("#a"))*.@id == ["a"]
    }

    def text() {
        given:
        html {
            a "a"
            b "b"
            div {
                c "c"
                d "d"
            }
        }

        expect:
        $("a").text() == "a"
        $("a").add("b")*.text() == ["a", "b"]
        $("div").text() =~ /(?s)c.*d/ // this is not consistent across drivers
        $("div")*.text().size() == 1 && $("div")*.text().first() =~ /(?s)c.*d/ // this is not consistent across drivers
        $("foo")*.text() == []
    }

    def tag() {
        given:
        html {
            a "a"
            b "b"
            div {
                c "c"
                d "d"
            }
        }

        expect:
        $("a").tag() == "a"
        $("a").add("b")*.tag() == ["a", "b"]
        $("div").tag() == "div"
        $("div")*.tag() == ["div"]
        $("foo")*.tag() == []
    }

    def classes() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3")
            div(id: "b", 'class': "b1")
        }

        expect:
        $("#a").classes() == ["a1", "a2", "a3"]
        $("#b").classes() == ["b1"]
        $("foo").classes() == []
    }

    def hasClass() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3")
            div(id: "b", 'class': "b1")
        }

        expect:
        $("#a").hasClass("a2")
        !$("#a").hasClass("a4")
        $("#b").hasClass("b1")
    }

    def is() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3")
            div(id: "b", 'class': "b1")
        }

        expect:
        $("#a").is("div")
        !$("#a").is("foo")
        !$("foo").is("foo")
    }

    def first() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3")
            div(id: "b", 'class': "b1")
        }

        expect:
        $("div").first()*.@id == ["a"]
        $("foo").first()*.@id == []
    }

    def last() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3")
            div(id: "b", 'class': "b1")
        }

        expect:
        $("div").last()*.@id == ["b"]
        $("foo").last()*.@id == []
    }

    def verifyNotEmpty() {
        given:
        html {
            div(id: "a", 'class': "a1 a2 a3")
            div(id: "b", 'class': "b1")
        }

        expect:
        $("div").verifyNotEmpty()

        when:
        $("foo").verifyNotEmpty()

        then:
        thrown(EmptyNavigatorException)
    }

    def displayed() {
        given:
        html {
            div(id: "a", "a")
            div(id: "b", style: "display: none;", "b")
        }

        expect:
        $("#a").displayed
        !$("#b").displayed
        !$("foo").displayed
    }

    def eq() {
        when:
        html {
            div(id: "a")
            div(id: "b")
            div(id: "c")
        }

        then:
        $("div").allElements()*.getAttribute("id") == ["a", "b", "c"]
    }

    def "leftShift returns the navigator so appends can be chained"() {
        given:
        html {
            input(type: "text")
        }

        when:
        $("input") << "a" << "b" << "c"

        then:
        $("input").value() == "abc"
    }

    def size() {
        given:
        html {
            div(id: "a")
            div(id: "b")
            div(id: "c")
        }

        expect:
        $("#a").size() == 1
        $("#a").add("#b").size() == 2
        $("#d").size() == 0
    }

    def "cannot read or set value of non existent control"() {
        given:
        html {
        }

        when:
        $().someFieldThatDoesNotExist

        then:
        thrown(MissingPropertyException)

        when:
        $().someFieldThatDoesNotExist = "foo"

        then:
        thrown(MissingPropertyException)
    }

    def ranges() {
        when:
        html {
            p(a: 1, "1")
            p(a: 2, "2")
            p(a: 3, "3")
            p(a: 4, "4")
        }

        then:
        $("p", 1..2)*.text() == ["2", "3"]
        $(a: ~/\d+/, 1..2)*.text() == ["2", "3"]
        $("p")[1..2]*.text() == ["2", "3"]
        $("p", a: ~/[234]/, 1..2)*.text() == ["3", "4"]
    }

    def focused() {
        when:
        html {
            input(class: "focused")
            input(class: "notFocused")
        }

        and:
        $(".focused").click()

        then:
        !$(".notExisting").focused
        $(".focused").focused
        !$(".notFocused").focused
    }

    def "filtering with only dynamic attribute set to true is a noop"() {
        given:
        html {
            div("div")
        }

        when:
        def unfiltered = $("div")

        then:
        unfiltered.filter(dynamic: true).is(unfiltered)
    }

}

class PageWithoutAtChecker extends Page {
}

class PageWithAtChecker extends Page {
    static at = { true }
}

class PageInstanceWithParametrizedAtChecker extends Page {
    static at = { condition }
    boolean condition
}
