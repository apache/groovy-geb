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
package geb.textmatching

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class TextMatchingSupportSpec extends Specification {

    @Shared
    def matchers = new TextMatchingSupport()

    @Unroll("pattern methods: #method - #input - #expected")
    def "t"() {
        expect:
        matchers."$method"(input).pattern.toString() == expected
        where:
        method          | input | expected
        "contains"      | "."   | ".*\\Q.\\E.*"
        "iContains"     | "."   | "(?i).*\\Q.\\E.*"
        "startsWith"    | "."   | "\\Q.\\E.*"
        "iStartsWith"   | "."   | "(?i)\\Q.\\E.*"
        "endsWith"      | "."   | ".*\\Q.\\E"
        "iEndsWith"     | "."   | "(?i).*\\Q.\\E"
        "containsWord"  | "."   | "(^|.+\\s)\\Q.\\E(\$|\\s.+)"
        "iContainsWord" | "."   | "(?i)(^|.+\\s)\\Q.\\E(\$|\\s.+)"
        "contains"      | ~"."  | ".*..*"
        "iContains"     | ~"."  | "(?i).*..*"
        "startsWith"    | ~"."  | "..*"
        "iStartsWith"   | ~"."  | "(?i)..*"
        "endsWith"      | ~"."  | ".*."
        "iEndsWith"     | ~"."  | "(?i).*."
        "containsWord"  | ~"."  | "(^|.+\\s).(\$|\\s.+)"
        "iContainsWord" | ~"."  | "(?i)(^|.+\\s).(\$|\\s.+)"
    }

    @Unroll("negated pattern methods: #method - #input")
    def "negated"() {
        when:
        def matcher = matchers."$method"(input)

        then:
        matcher instanceof NegatedTextMatcher

        and:
        matcher.matcher instanceof PatternTextMatcher

        and:
        matcher.matcher.pattern.toString() == expected

        where:
        method             | input | expected
        "notContains"      | "."   | ".*\\Q.\\E.*"
        "iNotContains"     | "."   | "(?i).*\\Q.\\E.*"
        "notStartsWith"    | "."   | "\\Q.\\E.*"
        "iNotStartsWith"   | "."   | "(?i)\\Q.\\E.*"
        "notEndsWith"      | "."   | ".*\\Q.\\E"
        "iNotEndsWith"     | "."   | "(?i).*\\Q.\\E"
        "notContainsWord"  | "."   | "(^|.+\\s)\\Q.\\E(\$|\\s.+)"
        "iNotContainsWord" | "."   | "(?i)(^|.+\\s)\\Q.\\E(\$|\\s.+)"
        "notContains"      | ~"."  | ".*..*"
        "iNotContains"     | ~"."  | "(?i).*..*"
        "notStartsWith"    | ~"."  | "..*"
        "iNotStartsWith"   | ~"."  | "(?i)..*"
        "notEndsWith"      | ~"."  | ".*."
        "iNotEndsWith"     | ~"."  | "(?i).*."
        "notContainsWord"  | ~"."  | "(^|.+\\s).(\$|\\s.+)"
        "iNotContainsWord" | ~"."  | "(?i)(^|.+\\s).(\$|\\s.+)"
    }

    @Unroll("#method matches \"#input\" as expected in text:\"#text\"")
    def "patterns"() {
        expect:
        matchers."$method"(input).matches(text) == expected

        where:
        method          | input     | expected | text
        "contains"      | "Verbum"  | true     | "Geb'sVerbumEstLegis"
        "contains"      | ~"Verbum" | true     | "Geb'sVerbumEstLegis"
        "iContains"     | "WorD"    | true     | "blasdf \t\n\nwORdsdfd"
        "iContains"     | ~"WorD"   | true     | "blasdf \t\n\nwORdsdfd"
        "iContainsWord" | "Button"  | true     | "    \n\n\nbutton"
        "iContainsWord" | ~"Button" | true     | "    \n\n\nbutton"
        "iContainsWord" | "verbum"  | true     | "Geb's Verbum Est Legis"
        "iContainsWord" | ~"verbum" | true     | "Geb's Verbum Est Legis"
        "iContainsWord" | ~"verbum" | false    | "i dont know latin"
        "containsWord"  | "noun"    | true     | "adjective noun verb"
        "containsWord"  | "noun"    | true     | "adjective noun"
        "containsWord"  | "noun"    | true     | "noun verb"
        "containsWord"  | ~"noun"   | true     | "noun verb"
        "containsWord"  | ~"noun"   | false    | "non-noun verb"
        "containsWord"  | "noun"    | false    | "noune verb"
    }

    @Unroll
    def "allOf matches input"(List<TextMatcher> input, String text, boolean expected) {
        expect:
        matchers.allOf(*input).matches(text) == expected

        where:
        input                                               | text   | expected
        [matchers.contains('A')]                            | 'ABBA' | true
        [matchers.contains('C')]                            | 'ABBA' | false
        [matchers.contains('AB'), matchers.contains('BBA')] | 'ABBA' | true
        [matchers.contains('AB'), matchers.contains('C')]   | 'ABBA' | false
    }

    @Unroll
    def "anyOf matches input"(List<TextMatcher> input, String text, boolean expected) {
        expect:
        matchers.anyOf(*input).matches(text) == expected

        where:
        input                                              | text   | expected
        [matchers.contains('A')]                           | 'ABBA' | true
        [matchers.contains('C')]                           | 'ABBA' | false
        [matchers.contains('AB'), matchers.contains('C')]  | 'ABBA' | true
        [matchers.contains('ABA'), matchers.contains('C')] | 'ABBA' | false
    }
}
