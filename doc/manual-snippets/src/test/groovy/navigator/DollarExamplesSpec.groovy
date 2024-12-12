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
package navigator

import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.By

class DollarExamplesSpec extends GebSpecWithCallbackServer {

    def "examples of all dollar method signatures"() {
        given:
        html {
            h1("first heading", class: "heading")
            h1("second heading", class: "heading")
            h1("third heading", class: "heading")
            div {
                p(title: "something")
            }
        }

        expect:
        // tag::concrete_example[]
        $("h1", 2, class: "heading")
        // end::concrete_example[]
        // tag::other_examples[]
        $("div p", 0)
        $("div p", title: "something")
        $(0)
        $(title: "something")
        // end::other_examples[]
    }

    def "support for CSS3 selectors"() {
        given:
        html {
            div(class: "some-class") {
                p(title: "something")
            }
        }

        expect:
        // tag::css3_selectors[]
        $('div.some-class p:first-child[title^="someth"]')
        // end::css3_selectors[]
    }

    def "support for By selectors"() {
        given:
        html {
            div(class: "some-class") {
                p(id: "some-id", class: "xpath")
            }
        }
        expect:
        // tag::by_selectors[]
        $(By.id("some-id"))
        $(By.className("some-class"))
        $(By.xpath('//p[@class="xpath"]'))
        // end::by_selectors[]
    }

    def "indexes and ranges"() {
        given:
        responseHtml """
            <html>
                // tag::indexes_and_ranges_html[]
                <p>a</p>
                <p>b</p>
                <p>c</p>
                // end::indexes_and_ranges_html[]
            </html>
        """

        when:
        go()

        then:
        // tag::indexes_and_ranges[]
        assert $("p", 0).text() == "a"
        assert $("p", 2).text() == "c"
        assert $("p", 0..1)*.text() == ["a", "b"]
        assert $("p", 1..2)*.text() == ["b", "c"]
        // end::indexes_and_ranges[]
    }

    def "attributes and text matching"() {
        given:
        attributesAndTextMatchingHtml()

        when:
        go()

        then:
        // tag::attributes[]
        assert $("p", attr1: "a").size() == 2
        assert $("p", attr2: "c").size() == 1
        // end::attributes[]
        // tag::multiple_attributes[]
        assert $("p", attr1: "a", attr2: "b").size() == 1
        // end::multiple_attributes[]
        // tag::text_matchers[]
        assert $("p", text: "p1").size() == 1
        // end::text_matchers[]
        // tag::matchers_and_attributes[]
        assert $("p", text: "p1", attr1: "a").size() == 1
        // end::matchers_and_attributes[]
    }

    def "patterns"() {
        given:
        attributesAndTextMatchingHtml()

        when:
        go()

        then:
        // tag::pattern[]
        assert $("p", text: ~/p./).size() == 2
        // end::pattern[]
        // tag::pattern_methods[]
        assert $("p", text: startsWith("p")).size() == 2
        assert $("p", text: endsWith("2")).size() == 1
        // end::pattern_methods[]
        // tag::pattern_methods_using_pattern[]
        assert $("p", text: contains(~/\d/)).size() == 2
        // end::pattern_methods_using_pattern[]
        // tag::pattern_composite[]
        assert $("p", text: allOf(contains("p1"), contains("p2"))).size() == 0
        assert $("p", text: anyOf(contains("p1"), contains("p2"))).size() == 2
        // end::pattern_composite[]
    }

    def "visibility"() {
        given:
        responseHtml """
            <html>
                // tag::visibility_html[]
                <p>p1</p>
                <p style="display: none;">p2</p>
                // end::visibility_html[]
            </html>
        """

        when:
        go()

        then:
        // tag::visibility[]
        assert $("p", displayed: true).size() == 1
        // end::visibility[]
    }

    private void attributesAndTextMatchingHtml() {
        responseHtml """
            <html>
                // tag::attributes_html[]
                <p attr1="a" attr2="b">p1</p>
                <p attr1="a" attr2="c">p2</p>
                // end::attributes_html[]
            </html>
        """
    }
}
