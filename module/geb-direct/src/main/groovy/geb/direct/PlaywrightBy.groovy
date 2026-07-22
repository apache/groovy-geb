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

/**
 * Maps Selenium {@link By} locators to Playwright selector strings.
 * <p>
 * Selenium's {@code By.toString()} format is {@code By.<strategy>: <value>}
 * (for example {@code By.cssSelector: .notice}).
 */
final class PlaywrightBy {

    private PlaywrightBy() {
    }

    static String selector(By by) {
        if (by == null) {
            throw new IllegalArgumentException('Locator must not be null')
        }

        String description = by
        def matcher = (description =~ /^By\.([A-Za-z]+):\s*(.*)$/)
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported locator: $by")
        }

        String strategy = matcher.group(1).toLowerCase(Locale.ROOT)
        String value = matcher.group(2)

        switch (strategy) {
            case 'cssselector':
            case 'css selector':
                return value
            case 'xpath':
                return "xpath=${value}"
            case 'id':
                return value ==~ /[A-Za-z_][A-Za-z0-9_-]*/ ? "#${value}" : "[id=\"${cssString(value)}\"]"
            case 'name':
                return "[name=\"${cssString(value)}\"]"
            case 'classname':
            case 'class name':
                // CSS class selectors reject identifiers starting with a digit; attribute
                // matching preserves Selenium By.className semantics for those values.
                return value ==~ /[A-Za-z_-][A-Za-z0-9_-]*/ ? ".${value}" : "[class~=\"${cssString(value)}\"]"
            case 'tagname':
            case 'tag name':
                return value
            case 'linktext':
            case 'link text':
                return "a:text-is(\"${cssString(value)}\")"
            case 'partiallinktext':
            case 'partial link text':
                return "a:has-text(\"${cssString(value)}\")"
            default:
                throw new IllegalArgumentException("Unsupported locator strategy '${matcher.group(1)}'")
        }
    }

    private static String cssString(String value) {
        value.replace('\\', '\\\\').replace('"', '\\"')
    }
}
