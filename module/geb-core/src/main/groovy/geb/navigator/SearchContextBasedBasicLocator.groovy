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

import geb.navigator.factory.NavigatorFactory
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

import java.util.function.Supplier

import static geb.navigator.Locator.MATCH_ALL_SELECTOR
import static geb.navigator.BasicLocator.DYNAMIC_ATTRIBUTE_NAME
import static geb.navigator.WebElementPredicates.matches

class SearchContextBasedBasicLocator implements BasicLocator {

    private static final Map<String, Closure> BY_SELECTING_ATTRIBUTES = [
            id   : By.&id,
            class: By.&className,
            name : By.&name
    ]

    public static final List<String> NON_SELECTOR_TRANSLATABLE_ATTRIBUTES = ["text", DYNAMIC_ATTRIBUTE_NAME]

    private final Iterable<? extends SearchContext> searchContexts
    private final NavigatorFactory navigatorFactory

    SearchContextBasedBasicLocator(SearchContext searchContext, NavigatorFactory navigatorFactory) {
        this([searchContext], navigatorFactory)
    }

    SearchContextBasedBasicLocator(Iterable<? extends SearchContext> searchContexts, NavigatorFactory navigatorFactory) {
        this.searchContexts = searchContexts
        this.navigatorFactory = navigatorFactory
    }

    @Override
    Navigator find(By bySelector) {
        find(false, bySelector)
    }

    @Override
    Navigator find(By bySelector, int index) {
        find(false, bySelector, index)
    }

    @Override
    Navigator find(Map<String, Object> attributes, int index) {
        find(attributes) { dynamic, bySelector, filteredAttributes ->
            find(dynamic, bySelector, filteredAttributes, index)
        }
    }

    @Override
    Navigator find(Map<String, Object> attributes, Range<Integer> range) {
        find(attributes) { dynamic, bySelector, filteredAttributes ->
            find(dynamic, bySelector, filteredAttributes, range)
        }
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector, int index) {
        find(dynamic(attributes), bySelector, attributes, index)
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector, Range<Integer> range) {
        find(dynamic(attributes), bySelector, attributes, range)
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector) {
        find(dynamic(attributes), bySelector, attributes)
    }

    @Override
    Navigator find(Map<String, Object> attributes, String selector) {
        find(attributes, selector) { dynamic, bySelector, filteredAttributes ->
            find(dynamic, bySelector, filteredAttributes)
        }
    }

    @Override
    Navigator find(Map<String, Object> attributes, String selector, int index) {
        find(attributes, selector) { dynamic, bySelector, filteredAttributes ->
            find(dynamic, bySelector, filteredAttributes, index)
        }
    }

    @Override
    Navigator find(Map<String, Object> attributes, String selector, Range<Integer> range) {
        find(attributes, selector) { dynamic, bySelector, filteredAttributes ->
            find(dynamic, bySelector, filteredAttributes, range)
        }
    }

    protected Navigator find(
            Map<String, Object> attributes,
            String selector = MATCH_ALL_SELECTOR,
            @ClosureParams(value = FromString, options = "Boolean,org.openqa.selenium.By,Map<String, Object>") Closure<Navigator> navigatorFromBy
    ) {
        def attributesCopy = attributes.clone()
        def selectedUsingBy = findUsingByIfPossible(attributesCopy, selector, navigatorFromBy)
        if (selectedUsingBy != null) {
            return selectedUsingBy
        }

        def optimizedSelector = optimizeSelector(selector, attributesCopy)
        if (optimizedSelector) {
            navigatorFromBy.call(dynamic(attributes), By.cssSelector(optimizedSelector), attributesCopy)
        } else {
            find(attributes, MATCH_ALL_SELECTOR)
        }
    }

    protected Navigator navigatorFor(Iterable<WebElement> contextElements) {
        navigatorFactory.createFromWebElements(contextElements)
    }

    protected Navigator findUsingByIfPossible(
            Map<String, Object> attributes,
            String selector,
            @ClosureParams(value = FromString, options = "Boolean,org.openqa.selenium.By,Map<String, Object>") Closure<Navigator> navigatorFromBy
    ) {
        if (attributes.size() == 1 && selector == MATCH_ALL_SELECTOR) {
            BY_SELECTING_ATTRIBUTES.findResult { String attributeName, Closure<By> byFactory ->
                if (hasStringValueForKey(attributes, attributeName)) {
                    navigatorFromBy.call(false, byFactory.call(attributes[attributeName]), [:])
                }
            }
        }
    }

    protected boolean hasStringValueForKey(Map<String, Object> attributes, String key) {
        attributes.containsKey(key) && attributes[key] instanceof CharSequence
    }

    /**
     * Optimizes the selector by translating attributes map into a css attribute selector if possible.
     * Note this method has a side-effect in that it _removes_ those keys from the predicates map.
     */
    protected String optimizeSelector(String selector, Map<String, Object> attributes) {
        if (!selector) {
            return selector
        }

        def buffer = new StringBuilder(selector)
        for (def it = attributes.entrySet().iterator(); it.hasNext();) {
            def attribute = it.next()
            if (!(attribute.key in NON_SELECTOR_TRANSLATABLE_ATTRIBUTES) && attribute.value instanceof CharSequence) {
                def attributeValue = attribute.value.toString()
                if (attribute.key == "class") {
                    attributeValue.split(/\s+/).each { className ->
                        buffer << "." << CssSelector.escape(className)
                    }
                } else {
                    buffer << """[${attribute.key}="${CssSelector.escape(attributeValue)}"]"""
                }
                it.remove()
            }
        }

        if (buffer[0] == MATCH_ALL_SELECTOR && buffer.length() > 1) {
            buffer.deleteCharAt(0)
        }
        buffer.toString()
    }

    protected Navigator find(boolean dynamic, By bySelector, Map<String, Object> attributes = [:]) {
        toNavigator(dynamic, elementsSupplier(bySelector, attributes))
    }

    protected Navigator find(boolean dynamic, By bySelector, Map<String, Object> attributes = [:], int index) {
        toNavigator(dynamic, elementsSupplier(bySelector, attributes, index))
    }

    protected Navigator find(boolean dynamic, By bySelector, Map<String, Object> attributes, Range<Integer> range) {
        toNavigator(dynamic, elementsSupplier(bySelector, attributes, range))
    }

    protected Supplier<Collection<WebElement>> elementsSupplier(By bySelector, Map<String, Object> attributes) {
        { ->
            searchContexts.collectMany { it.findElements(bySelector) }.findAll { matches(it, attributes) }
        }
    }

    protected Supplier<Collection<WebElement>> elementsSupplier(By bySelector, Map<String, Object> attributes, int index) {
        { ->
            [elementsSupplier(bySelector, attributes).get()[index]]
        }
    }

    protected Supplier<Collection<WebElement>> elementsSupplier(By bySelector, Map<String, Object> attributes, Range<Integer> range) {
        { ->
            def elements = elementsSupplier(bySelector, attributes).get()
            if (elements) {
                elements.toList()[range]
            }
        }
    }

    protected Iterable<WebElement> toDynamicIterable(Supplier<Collection<WebElement>> contextElementsSupplier) {
        { -> contextElementsSupplier.get().iterator() } as Iterable<WebElement>
    }

    protected Navigator toNavigator(boolean dynamic, Supplier<Collection<WebElement>> contextElementsSupplier) {
        navigatorFor(dynamic ? toDynamicIterable(contextElementsSupplier) : contextElementsSupplier.get())
    }

    protected boolean dynamic(Map<String, Object> attributes) {
        attributes[DYNAMIC_ATTRIBUTE_NAME]
    }
}
