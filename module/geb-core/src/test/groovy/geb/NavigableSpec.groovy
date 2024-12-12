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

import geb.error.UnableToSetElementException
import geb.navigator.Navigator
import geb.navigator.DefaultNavigator
import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.WebElement
import spock.lang.Issue

class NavigableSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml {
            body {
                ['a', 'b', 'c', 'd'].each {
                    p(it, 'class': it, style: "display: ${it == 'd' ? 'none' : 'inline'};")
                }
                input(type: "text", name: "e", value: "val")
                img(name: "notAControl")
                span(name: "alsoNotAControl")
                img(name: "alsoNotAControl")
            }
        }
    }

    def setup() {
        go()
    }

    def "no args"() {
        expect:
        $().tag() == "html"
        //find().tag() == "html" // doesn't work due to Groovy not dispatching to our method
    }

    def "just index"() {
        expect:
        $(0).tag() == "html"
        find(0).tag() == "html"
    }

    def "just selector"() {
        expect:
        $("p").size() == 4
        find("p").size() == 4
    }

    def "just attributes"() {
        expect:
        $(class: "a").text() == "a"
        find(class: "a").text() == "a"
    }

    def "just attributes - but text"() {
        expect:
        $(text: "a").text() == "a"
        find(text: "a").text() == "a"
    }

    def "just attributes - displayed"() {
        expect:
        $(displayed: false)*.classes().contains(["d"])
        find(displayed: false)*.classes().contains(["d"])
    }

    def "selector and index"() {
        expect:
        $("p", 1).text() == "b"
        find("p", 1).text() == "b"
    }

    def "selector and attributes"() {
        expect:
        $("p", class: "c").text() == "c"
        find("p", class: "c").text() == "c"
    }

    @Issue("https://github.com/geb/issues/issues/339")
    def "predicates map passed to find is not modified"() {
        given:
        def predicates = [class: "c"]

        when:
        def text = find(predicates, "p").text()

        then:
        text == "c"
        predicates == [class: "c"]
    }

    def "attributes and index"() {
        expect:
        $(1, class: ~/\w/).text() == "b"
        find(1, class: ~/\w/).text() == "b"
    }

    def "selector, attributes and index"() {
        expect:
        $("p", 1, class: ~/\w/).text() == "b"
        find("p", 1, class: ~/\w/).text() == "b"
    }

    def "delegating missing properties to the navigator"() {
        expect:
        e == "val"
        when:
        e = "changed"
        then:
        e == "changed"
    }

    def "delegating missing methods to the navigator"() {
        expect:
        e().tag() == "input"
    }

    def "invalid property access throws unresolvable exception"() {
        when:
        SomePage
        then:
        MissingPropertyException e = thrown()
        e.message.endsWith('Is SomePage a class you forgot to import?')
    }

    def "invalid property assignment throws unresolvable exception"() {
        when:
        z = 3
        then:
        thrown(MissingPropertyException)
    }

    def "dynamic method call that resolves to an empty navigator throws missing method exception"() {
        when:
        z()
        then:
        thrown(MissingMethodException)
    }

    def "trying to set a value on an element that does not support it throws unable to set element exception"() {
        when:
        notAControl = 'testValue'
        then:
        UnableToSetElementException ex = thrown(UnableToSetElementException)
        ex.message == 'Unable to set the value of element img as it is not a valid form element'
    }

    def "trying to set a value on multiple elements that do not support it throws unable to set element exception"() {
        when:
        alsoNotAControl = 'testValue'
        then:
        UnableToSetElementException ex = thrown(UnableToSetElementException)
        ex.message == 'Unable to set the value of elements span, img as they are not valid form elements'
    }

    def "uppercase input element can be set"() {
        given:
        WebElement element = Mock(WebElement) {
            getTagName() >> "INPUT"
        }
        Navigator navigator = new DefaultNavigator(Stub(Browser), [element])

        when:
        navigator.value("test")

        then:
        notThrown(UnableToSetElementException)

        and:
        1 * element.sendKeys("test")
    }

    def "composition with navigators"() {
        expect:
        $($(".a")).size() == 1
        $($(".a"), $(".c")).size() == 2
        $($("p"), $("input")).size() == 5
    }

    def "composition with content"() {
        when:
        to(NavigableSupportSpecPage)

        then:
        $(input).size() == 1
        $(input, pElem('a')).size() == 2
        $(input, pElems).size() == 5
    }

    def "composition with web elements"() {
        expect:
        $($(".a").firstElement()).size() == 1
        $($(".a").firstElement(), $(".c").firstElement()).size() == 2
        $(*($("p").allElements()), $("input").firstElement()).size() == 5
    }

    def "attribute access notation"() {
        expect:
        $(".a").@class == 'a'
        $("p")*.@class == ['a', 'b', 'c', 'd']
    }

    def "obtaining a navigator for the focused element"() {
        given:
        def input = $("input")

        when:
        input.click()

        then:
        focused() == input
    }

}

class NavigableSupportSpecPage extends Page {
    static content = {
        pElem { elemClass -> $('p', 'class': elemClass) }
        pElems { $('p') }
        input { $('input') }
    }
}