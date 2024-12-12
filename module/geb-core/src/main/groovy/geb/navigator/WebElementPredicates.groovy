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
import org.openqa.selenium.WebElement

import java.util.regex.Pattern

import static geb.navigator.BasicLocator.DYNAMIC_ATTRIBUTE_NAME

class WebElementPredicates {

    static boolean matches(WebElement element, Map<String, Object> predicates) {
        predicates.findAll { it.key != DYNAMIC_ATTRIBUTE_NAME }.every { name, requiredValue ->
            def actualValue
            switch (name) {
                case "text": actualValue = element.text; break
                case "class": actualValue = element.getAttribute("class")?.tokenize(); break
                case "displayed": actualValue = element.displayed; break
                default: actualValue = element.getAttribute(name)
            }
            matches(actualValue, requiredValue)
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
        actualValue == requiredValue.asBoolean()
    }
}
