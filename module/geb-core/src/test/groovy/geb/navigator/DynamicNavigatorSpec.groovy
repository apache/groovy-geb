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

import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.By
import spock.lang.Unroll

class DynamicNavigatorSpec extends GebSpecWithCallbackServer {

    @Unroll("#scenario selector based dynamic navigator")
    def "selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
        }

        and:
        def nonDynamic = $(selector)
        def dynamic = $(selector, dynamic: true)

        when:
        $("body").jquery.append("<div>div</div>")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2

        where:
        scenario | selector
        "string" | "div"
        "By"     | By.cssSelector("div")
    }

    def "dynamic attribute is interpreted using Groovy truth regardless of type"() {
        given:
        bodyWithJquery {
            div("div")
        }

        and:
        def dynamic = $("div", dynamic: "true")

        when:
        $("body").jquery.append("<div>div</div>")

        then:
        dynamic.size() == 2
    }

    def "attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text")
        }

        and:
        def nonDynamic = $(type: "text")
        def dynamic = $(type: "text", dynamic: true)

        when:
        $("body").jquery.append('<input type="text">')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "attribute and index based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text", value: "first")
            input(type: "password", value: "password")
            input(type: "text", value: "second")
        }

        and:
        def nonDynamic = $(1, type: "text")
        def dynamic = $(1, type: "text", dynamic: true)

        when:
        $(type: "password").jquery.after('<input type="text" value="inserted">')

        then:
        nonDynamic.value() == "second"
        dynamic.value() == "inserted"
    }

    def "attribute and range based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text", value: "first")
            input(type: "password", value: "password")
            input(type: "text", value: "second")
            input(type: "text", value: "third")
        }

        and:
        def nonDynamic = $(1..2, type: "text")
        def dynamic = $(1..2, type: "text", dynamic: true)

        when:
        $(type: "password").jquery.after('<input type="text" value="inserted">')

        then:
        nonDynamic*.value() == ["second", "third"]
        dynamic*.value() == ["inserted", "second"]
    }

    @Unroll("#scenario selector and index based dynamic navigator")
    def "selector and index based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("first")
            div("second")
        }

        and:
        def nonDynamic = $(selector, 1)
        def dynamic = $(selector, 1, dynamic: true)

        when:
        $("div", 0).jquery.after('<div>inserted</div>')

        then:
        nonDynamic.text() == "second"
        dynamic.text() == "inserted"

        where:
        scenario | selector
        "string" | "div"
        "By"     | By.cssSelector("div")
    }

    @Unroll("#scenario selector and range based dynamic navigator")
    def "selector and range based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("first")
            div("second")
            div("third")
            div("fourth")
        }

        and:
        def nonDynamic = $(selector, 1..2)
        def dynamic = $(selector, 1..2, dynamic: true)

        when:
        $("div", 1).jquery.after('<div>inserted</div>')

        then:
        nonDynamic*.text() == ["second", "third"]
        dynamic*.text() == ["second", "inserted"]

        where:
        scenario | selector
        "string" | "div"
        "By"     | By.cssSelector("div")
    }

    @Unroll("#scenario selector, attribute and index based dynamic navigator")
    def "selector, attribute and index based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text", value: "first")
            input(type: "password", value: "password")
            input(type: "text", value: "second")
        }

        and:
        def nonDynamic = $(selector, 1, type: "text")
        def dynamic = $(selector, 1, type: "text", dynamic: true)

        when:
        $(type: "password").jquery.after('<input type="text" value="inserted">')

        then:
        nonDynamic.value() == "second"
        dynamic.value() == "inserted"

        where:
        scenario | selector
        "string" | "input"
        "By"     | By.cssSelector("input")
    }

    @Unroll("#scenario selector, attribute and range based dynamic navigator")
    def "selector, attribute and range based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text", value: "first")
            input(type: "password", value: "password")
            input(type: "text", value: "second")
            input(type: "text", value: "third")
        }

        and:
        def nonDynamic = $(selector, 1..2, type: "text")
        def dynamic = $(selector, 1..2, type: "text", dynamic: true)

        when:
        $(type: "password").jquery.after('<input type="text" value="inserted">')

        then:
        nonDynamic*.value() == ["second", "third"]
        dynamic*.value() == ["inserted", "second"]

        where:
        scenario | selector
        "string" | "input"
        "By"     | By.cssSelector("input")
    }

    def "string selector, non-translatable attribute and index based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(id: "first", "matching text")
            div(id: "second", "not matching text")
            div(id: "third", "matching text")
        }

        and:
        def nonDynamic = $("div", 1, text: "matching text")
        def dynamic = $("div", 1, text: "matching text", dynamic: true)

        when:
        $(id: "second").jquery.after('<div id="inserted">matching text</div>')

        then:
        nonDynamic.@id == "third"
        dynamic.@id == "inserted"
    }

    def "string selector, non-translatable attribute and range based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(id: "first", "matching text")
            div(id: "second", "not matching text")
            div(id: "third", "matching text")
            div(id: "fourth", "matching text")
        }

        and:
        def nonDynamic = $("div", 1..2, text: "matching text")
        def dynamic = $("div", 1..2, text: "matching text", dynamic: true)

        when:
        $(id: "second").jquery.after('<div id="inserted">matching text</div>')

        then:
        nonDynamic*.attr("id") == ["third", "fourth"]
        dynamic*.attr("id") == ["inserted", "third"]
    }

    def "attributes filter based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text")
            input(type: "password")
        }

        and:
        def base = $("input", dynamic: true)
        def nonDynamic = base.filter(type: "text")
        def dynamic = base.filter(type: "text", dynamic: true)

        when:
        $("body").jquery.append('<input type="text">')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "selector filter based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text")
        }

        and:
        def base = $("input", dynamic: true)
        def nonDynamic = base.filter("input")
        def dynamic = base.filter("input", dynamic: true)

        when:
        $("body").jquery.append('<input type="text">')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "children selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
        }

        and:
        def body = $("body")
        def nonDynamic = body.children("div")
        def dynamic = body.children("div", dynamic: true)

        when:
        body.jquery.append("<div>div</div>")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "children attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            input(type: "text")
        }

        and:
        def body = $("body")
        def nonDynamic = body.children(type: "text")
        def dynamic = body.children(type: "text", dynamic: true)

        when:
        body.jquery.append('<input type="text">')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "closest attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(id: "grandparent", class: "to-be-selected") {
                div(id: "parent") {
                    div(id: "child", "div")
                }
            }
        }

        and:
        def child = $("#child")
        def nonDynamic = child.closest(class: "to-be-selected")
        def dynamic = child.closest(class: "to-be-selected", dynamic: true)

        when:
        $("#parent").jquery.addClass("to-be-selected")

        then:
        nonDynamic.@id == "grandparent"
        dynamic.@id == "parent"
    }

    def "closest selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(id: "grandparent", class: "to-be-selected") {
                div(id: "parent") {
                    div(id: "child", "div")
                }
            }
        }

        and:
        def child = $("#child")
        def nonDynamic = child.closest(".to-be-selected")
        def dynamic = child.closest(".to-be-selected", dynamic: true)

        when:
        $("#parent").jquery.addClass("to-be-selected")

        then:
        nonDynamic.@id == "grandparent"
        dynamic.@id == "parent"
    }

    def "has attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "top-level") {
                div(class: "included", "div")
            }
            div(class: "top-level") {
                div("div")
            }
        }

        and:
        def parentDivs = $(".top-level")
        def nonDynamic = parentDivs.has(class: "included")
        def dynamic = parentDivs.has(class: "included", dynamic: true)

        when:
        $(".top-level:nth-of-type(2) div").jquery.addClass("included")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    @Unroll("has #scenario selector based dynamic navigator")
    def "has selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "top-level") {
                div(class: "included", "div")
            }
            div(class: "top-level") {
                div("div")
            }
        }

        and:
        def parentDivs = $(".top-level")
        def nonDynamic = parentDivs.has(selector)
        def dynamic = parentDivs.has(selector, dynamic: true)

        when:
        $(".top-level:nth-of-type(2) div").jquery.addClass("included")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2

        where:
        scenario | selector
        "string" | ".included"
        "By"     | By.className("included")
    }

    def "hasNot attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "top-level") {
                div(class: "excluded", "div")
            }
            div(class: "top-level") {
                div("div")
            }
        }

        and:
        def parentDivs = $(".top-level")
        def nonDynamic = parentDivs.hasNot(class: "excluded")
        def dynamic = parentDivs.hasNot(class: "excluded", dynamic: true)

        when:
        $(".top-level:nth-of-type(2) div").jquery.addClass("excluded")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 0
    }

    @Unroll("#scenario hasNot selector based dynamic navigator")
    def "hasNot selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "top-level") {
                div(class: "excluded", "div")
            }
            div(class: "top-level") {
                div("div")
            }
        }

        and:
        def parentDivs = $(".top-level")
        def nonDynamic = parentDivs.hasNot(selector)
        def dynamic = parentDivs.hasNot(selector, dynamic: true)

        when:
        $(".top-level:nth-of-type(2) div").jquery.addClass("excluded")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 0

        where:
        scenario | selector
        "string" | ".excluded"
        "By"     | By.className("excluded")
    }

    def "next attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("first")
            div("second")
            div(class: "match", "third")
        }

        and:
        def firstDiv = $("div::first-child")
        def nonDynamic = firstDiv.next(class: "match")
        def dynamic = firstDiv.next(class: "match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.text() == "third"
        dynamic.text() == "second"
    }

    def "next selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("first")
            div("second")
            div(class: "match", "third")
        }

        and:
        def firstDiv = $("div::first-child")
        def nonDynamic = firstDiv.next(".match")
        def dynamic = firstDiv.next(".match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.text() == "third"
        dynamic.text() == "second"
    }

    def "nextAll attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
            div("div")
            div(class: "match", "div")
        }

        and:
        def firstDiv = $("div::first-child")
        def nonDynamic = firstDiv.nextAll(class: "match")
        def dynamic = firstDiv.nextAll(class: "match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "nextAll selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
            div("div")
            div(class: "match", "div")
        }

        and:
        def firstDiv = $("div::first-child")
        def nonDynamic = firstDiv.nextAll(".match")
        def dynamic = firstDiv.nextAll(".match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "nextUntil attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
            div("div")
            div("div")
            div(class: "match", "div")
        }

        and:
        def firstDiv = $("div::first-child")
        def nonDynamic = firstDiv.nextUntil(class: "match")
        def dynamic = firstDiv.nextUntil(class: "match", dynamic: true)

        when:
        $("div::nth-of-type(3)").jquery.addClass("match")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "nextUntil selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
            div("div")
            div("div")
            div(class: "match", "div")
        }

        and:
        def firstDiv = $("div::first-child")
        def nonDynamic = firstDiv.nextUntil(".match")
        def dynamic = firstDiv.nextUntil(".match", dynamic: true)

        when:
        $("div::nth-of-type(3)").jquery.addClass("match")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "previous attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "match", "first")
            div("second")
            div("third")
        }

        and:
        def lastDiv = $("div::last-child")
        def nonDynamic = lastDiv.previous(class: "match")
        def dynamic = lastDiv.previous(class: "match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.text() == "first"
        dynamic.text() == "second"
    }

    def "previous selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "match", "first")
            div("second")
            div("third")
        }

        and:
        def lastDiv = $("div::last-child")
        def nonDynamic = lastDiv.previous(".match")
        def dynamic = lastDiv.previous(".match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.text() == "first"
        dynamic.text() == "second"
    }

    def "prevAll attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "match", "div")
            div("div")
            div("div")
        }

        and:
        def lastDiv = $("div::last-child")
        def nonDynamic = lastDiv.prevAll(class: "match")
        def dynamic = lastDiv.prevAll(class: "match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "prevAll selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "match", "div")
            div("div")
            div("div")
        }

        and:
        def lastDiv = $("div::last-child")
        def nonDynamic = lastDiv.prevAll(".match")
        def dynamic = lastDiv.prevAll(".match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "prevUntil attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "match", "div")
            div("div")
            div("div")
            div("div")
        }

        and:
        def lastDiv = $("div::last-child")
        def nonDynamic = lastDiv.prevUntil(class: "match")
        def dynamic = lastDiv.prevUntil(class: "match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "prevUntil selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "match", "div")
            div("div")
            div("div")
            div("div")
        }

        and:
        def lastDiv = $("div::last-child")
        def nonDynamic = lastDiv.prevUntil(".match")
        def dynamic = lastDiv.prevUntil(".match", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("match")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "not attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "excluded", "div")
            div("div")
            div("div")
        }

        and:
        def divs = $("div")
        def nonDynamic = divs.not(class: "excluded")
        def dynamic = divs.not(class: "excluded", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("excluded")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "not selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "excluded", "div")
            div("div")
            div("div")
        }

        and:
        def divs = $("div")
        def nonDynamic = divs.not(".excluded")
        def dynamic = divs.not(".excluded", dynamic: true)

        when:
        $("div::nth-of-type(2)").jquery.addClass("excluded")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "parent attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "included") {
                div("div")
            }
            div {
                div("div")
            }
        }

        and:
        def childrenDivs = $("div div", dynamic: true)
        def nonDynamic = childrenDivs.parent(class: "included")
        def dynamic = childrenDivs.parent(class: "included", dynamic: true)

        when:
        $("body > div").jquery.addClass("included")
        $("body").jquery.append('<div class="included"><div>div</div></div>')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 3
    }

    def "parent selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "included") {
                div("div")
            }
            div {
                div("div")
            }
        }

        and:
        def childrenDivs = $("div div", dynamic: true)
        def nonDynamic = childrenDivs.parent(".included")
        def dynamic = childrenDivs.parent(".included", dynamic: true)

        when:
        $("body > div").jquery.addClass("included")
        $("body").jquery.append('<div class="included"><div>div</div></div>')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 3
    }

    def "parents attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "to-be-selected") {
                div(id: "parent") {
                    div(id: "child", "div")
                }
            }
        }

        and:
        def child = $("#child")
        def nonDynamic = child.parents(class: "to-be-selected")
        def dynamic = child.parents(class: "to-be-selected", dynamic: true)

        when:
        $("#parent").jquery.addClass("to-be-selected")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "parents selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "to-be-selected") {
                div(id: "parent") {
                    div(id: "child", "div")
                }
            }
        }

        and:
        def child = $("#child")
        def nonDynamic = child.parents(".to-be-selected")
        def dynamic = child.parents(".to-be-selected", dynamic: true)

        when:
        $("#parent").jquery.addClass("to-be-selected")

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "parentsUntil attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "until") {
                div(id: "grandparent") {
                    div {
                        div(id: "child", "div")
                    }
                }
            }
        }

        and:
        def child = $("#child")
        def nonDynamic = child.parentsUntil(class: "until")
        def dynamic = child.parentsUntil(class: "until", dynamic: true)

        when:
        $("#grandparent").jquery.addClass("until")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "parentsUntil selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div(class: "until") {
                div(id: "grandparent") {
                    div {
                        div(id: "child", "div")
                    }
                }
            }
        }

        and:
        def child = $("#child")
        def nonDynamic = child.parentsUntil(".until")
        def dynamic = child.parentsUntil(".until", dynamic: true)

        when:
        $("#grandparent").jquery.addClass("until")

        then:
        nonDynamic.size() == 2
        dynamic.size() == 1
    }

    def "siblings attribute based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
            div("div")
            div(class: "match", "div")
        }

        and:
        def secondDiv = $("div::nth-of-type(2)")
        def nonDynamic = secondDiv.siblings(class: "match")
        def dynamic = secondDiv.siblings(class: "match", dynamic: true)

        when:
        secondDiv.jquery.before('<div class="match">div</div>')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "siblings selector based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
            div("div")
            div(class: "match", "div")
        }

        and:
        def secondDiv = $("div::nth-of-type(2)")
        def nonDynamic = secondDiv.siblings(".match")
        def dynamic = secondDiv.siblings(".match", dynamic: true)

        when:
        secondDiv.jquery.before('<div class="match">div</div>')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }

    def "web elements supplier based dynamic navigator"() {
        given:
        bodyWithJquery {
            div("div")
        }

        and:
        def nonDynamic = $("div")
        def dynamic = navigatorFactory.createDynamic { $("div").allElements() }

        when:
        $("body").jquery.append('<div></div>')

        then:
        nonDynamic.size() == 1
        dynamic.size() == 2
    }
}
