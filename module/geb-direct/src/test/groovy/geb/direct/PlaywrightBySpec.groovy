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
package geb.direct

import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import spock.lang.Specification
import spock.lang.Unroll

class PlaywrightBySpec extends Specification {

    @Unroll
    def "converts #description locator to a Playwright selector"() {
        expect:
        PlaywrightBy.selector(locator) == selector

        where:
        description          | locator                         | selector
        'CSS'                | By.cssSelector('.notice')       | '.notice'
        'XPath'              | By.xpath('//main/article')      | 'xpath=//main/article'
        'id'                 | By.id('account')                | '#account'
        'numeric id'         | By.id('123')                    | '[id="123"]'
        'name'               | By.name('email')                | '[name="email"]'
        'class name'         | By.className('menu-item')       | '.menu-item'
        'numeric class name' | By.className('123item')         | '[class~="123item"]'
        'tag name'           | By.tagName('button')            | 'button'
        'exact link text'    | By.linkText('Continue')         | 'a:text-is("Continue")'
        'partial link text'  | By.partialLinkText('Contin')    | 'a:has-text("Contin")'
    }

    def 'escapes CSS identifiers and selector string values'() {
        expect:
        PlaywrightBy.selector(By.id('a.b')) == '[id="a.b"]'
        PlaywrightBy.selector(By.name('a"b')) == '[name="a\\"b"]'
        PlaywrightBy.selector(By.linkText('a\\b')) == 'a:text-is("a\\\\b")'
    }

    def 'rejects null and unsupported locators'() {
        when:
        PlaywrightBy.selector(locator)

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains(message)

        where:
        locator                                      | message
        null                                         | 'Locator must not be null'
        new By() {
            @SuppressWarnings('UnusedMethodParameter')
            List<WebElement> findElements(SearchContext context) { [] }
            String toString() { 'unsupported locator' }
        }                                            | 'Unsupported locator'
    }
}
