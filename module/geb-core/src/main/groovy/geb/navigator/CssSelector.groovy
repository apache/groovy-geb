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

import geb.error.InvalidCssSelectorException
import geb.error.UnsupportedFilteringCssSelectorException
import jodd.csselly.CSSelly
import jodd.csselly.CSSellyException
import jodd.csselly.CssSelector as JoddCssSelector
import jodd.csselly.selector.AttributeSelector
import org.openqa.selenium.WebElement

class CssSelector {

    private static final String CSS_SELECTOR_SPECIAL_CHARS_PATTERN = '[ !"#$%&\'\\(\\)*+,./:;<=>?@\\[\\]^`\\{|\\}~\\\\]'

    private CssSelector() {
    }

    static String escape(String value) {
        value.replaceAll("($CSS_SELECTOR_SPECIAL_CHARS_PATTERN)", '\\\\$1')
    }

    static boolean matches(WebElement element, String selectorString) {
        def selectors = compile(selectorString)
        selectors.any { JoddCssSelector selector ->
            matchesTagName(element, selector) && matchesAttributes(element, selector)
        }
    }

    private static List<JoddCssSelector> compile(String groupSelector) {
        List<List<JoddCssSelector>> cssSelectors
        try {
            cssSelectors = CSSelly.parse(groupSelector)
        } catch (CSSellyException e) {
            throw new InvalidCssSelectorException(groupSelector, e)
        }
        validate(cssSelectors)
        cssSelectors*.first()
    }

    private static void validate(List<List<JoddCssSelector>> cssSelectors) {
        cssSelectors.each {
            if (it.size() > 1) {
                def selectorAsString = it*.toString().join("")
                throw new UnsupportedFilteringCssSelectorException(selectorAsString, "Only single level selectors are supported.")
            }
            def cssSelector = it.first()
            for (int i = 0; i < cssSelector.selectorsCount(); i++) {
                if (!AttributeSelector.isInstance(cssSelector.getSelector(i))) {
                    throw new UnsupportedFilteringCssSelectorException(cssSelector.toString(), "Only element name, class, id and attribute selectors are supported.")
                }
            }
        }
    }

    private static boolean matchesTagName(WebElement element, JoddCssSelector selector) {
        selector.element == "*" || selector.element.equalsIgnoreCase(element.tagName)
    }

    private static boolean matchesAttributes(WebElement element, JoddCssSelector selector) {
        List<AttributeSelector> attributeSelectors = (0..<selector.selectorsCount()).collect { selector.getSelector(it) }
        attributeSelectors.every { attributeSelector ->
            def attributeValue = element.getAttribute(attributeSelector.name)

            attributeValue != null && (attributeSelector.value == null || attributeSelector.match.compare(attributeValue, attributeSelector.value))
        }
    }

}