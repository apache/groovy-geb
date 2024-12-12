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

import geb.Browser
import geb.error.InvalidCssSelectorException
import geb.error.UnsupportedFilteringCssSelectorException
import jodd.csselly.CSSellyException
import org.openqa.selenium.WebDriver
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CssSelectorSpec extends Specification {

    @Shared
    Browser browser

    @Shared
    WebDriver driver

    @Shared
    Navigator onPage

    def setupSpec() {
        browser = new Browser()
        browser.go(getClass().getResource("/test.html") as String)
        onPage = browser.navigatorFactory.base
    }

    @Unroll("matching elements using #selector")
    def "matching elements using css selectors"() {
        expect:
        CssSelector.matches(element, selector) == expectedMatch

        where:
        selector               | element                                       | expectedMatch
        '#article-1'           | onPage.find('#article-1').firstElement()      | true
        '#article-2'           | onPage.find('#article-1').firstElement()      | false
        '.article'             | onPage.find('#article-1').firstElement()      | true
        '.not-article'         | onPage.find('#article-1').firstElement()      | false
        'a, form'              | onPage.find('#sidebar > form').firstElement() | true
        '[method=get]'         | onPage.find('#sidebar > form').firstElement() | true
        '[data-test]'          | onPage.find('#sidebar > form').firstElement() | true
        '[action^="#"]'        | onPage.find('#sidebar > form').firstElement() | true
        'p, form[method=post]' | onPage.find('#sidebar > form').firstElement() | false
        '*.article'            | onPage.find('#article-1').firstElement()      | true
        '*.not-article'        | onPage.find('#article-1').firstElement()      | false
        'form'                 | onPage.find('#sidebar > form').firstElement() | true
        'FORM'                 | onPage.find('#sidebar > form').firstElement() | true
        'form[method=get]'     | onPage.find('#sidebar > form').firstElement() | true
        'FORM[method=get]'     | onPage.find('#sidebar > form').firstElement() | true
    }

    @Unroll
    def "pseudo classes and pseudo functions are not supported when matching elements"() {
        when:
        CssSelector.matches(onPage.firstElement(), selector)

        then:
        UnsupportedFilteringCssSelectorException e = thrown()
        e.message == "$selector is not supported as a selector for filtering. Only element name, class, id and attribute selectors are supported."

        where:
        selector << ["a:first-child", "a:nth-child(1)"]
    }

    def "only single level selectors are supported when matching elements"() {
        when:
        CssSelector.matches(onPage.firstElement(), selector)

        then:
        UnsupportedFilteringCssSelectorException e = thrown()
        e.message == "$selector is not supported as a selector for filtering. Only single level selectors are supported."

        where:
        selector = "p a"
    }

    def "invalid selectors"() {
        when:
        CssSelector.matches(onPage.firstElement(), ".#a")

        then:
        InvalidCssSelectorException e = thrown()
        e.message == ".#a is not a valid CSS selector"
        e.cause in CSSellyException
    }
}
