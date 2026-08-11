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

import geb.textmatching.TextMatcher
import groovy.transform.CompileStatic
import org.openqa.selenium.WebElement

import java.util.regex.Pattern

import static geb.navigator.BasicLocator.DYNAMIC_ATTRIBUTE_NAME

@CompileStatic
class WebElementPredicates {

    static boolean matches(WebElement element, Map<String, Object> predicates) {
        predicates
            .findAll { entry -> entry.key != DYNAMIC_ATTRIBUTE_NAME }
            .every { entry ->
                def name = entry.key
                def requiredValue = entry.value
                switch (name) {
                    case 'text': return matchesStringValue(element.text, requiredValue)
                    case 'class': return matchesCollectionValue(element.getAttribute('class')?.tokenize(), requiredValue)
                    case 'displayed': return matches(element.displayed, requiredValue)
                    default: return matchesStringValue(element.getAttribute(name), requiredValue)
                }
            }
    }

    private static boolean matchesStringValue(String actualValue, Object requiredValue) {
        if (actualValue == null) {
            return requiredValue == null
        }
        matchesRequiredValue(requiredValue,
            { Pattern value -> matches(actualValue, value) },
            { TextMatcher value -> matches(actualValue, value) },
            { String value -> matches(actualValue, value) }
        )
    }

    private static boolean matchesCollectionValue(Collection<String> actualValue, Object requiredValue) {
        if (actualValue == null) {
            return requiredValue == null
        }
        matchesRequiredValue(requiredValue,
            { Pattern value -> matches(actualValue, value) },
            { TextMatcher value -> matches(actualValue, value) },
            { String value -> matches(actualValue, value) }
        )
    }

    private static boolean matchesRequiredValue(
        Object requiredValue,
        Closure<Boolean> whenPattern,
        Closure<Boolean> whenTextMatcher,
        Closure<Boolean> whenString
    ) {
        switch (requiredValue) {
            case null: return false
            case Pattern: return whenPattern.call((Pattern) requiredValue)
            case TextMatcher: return whenTextMatcher.call((TextMatcher) requiredValue)
            case String: return whenString.call((String) requiredValue)
            default: return false
        }
    }

    protected static boolean matches(String actualValue, String requiredValue) {
        actualValue == requiredValue
    }

    protected static boolean matches(String actualValue, Pattern requiredValue) {
        actualValue ==~ requiredValue
    }

    protected static boolean matches(String actualValue, TextMatcher matcher) {
        matcher.matches(actualValue)
    }

    protected static boolean matches(Collection<String> actualValue, String requiredValue) {
        requiredValue in actualValue
    }

    protected static boolean matches(Collection<String> actualValue, Pattern requiredValue) {
        actualValue.any { it ==~ requiredValue }
    }

    protected static boolean matches(Collection<String> actualValue, TextMatcher matcher) {
        actualValue.any { matcher.matches(it) }
    }

    protected static boolean matches(boolean actualValue, Object requiredValue) {
        actualValue == requiredValue as Boolean
    }
}
