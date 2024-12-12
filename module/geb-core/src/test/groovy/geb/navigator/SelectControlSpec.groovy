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

import geb.test.GebSpecWithCallbackServer
import geb.test.browsers.CrossBrowser

@CrossBrowser
class SelectControlSpec extends GebSpecWithCallbackServer {

    def singleSelect() {
        given:
        html {
            select(name: "s1") {
                option(value: "o1")
                option(value: "o2")
            }
            select(name: "s2") {
                option(value: "o1")
                option(value: "o2", selected: "selected")
            }
        }

        expect:
        $().s1 == "o1"
        $().s1().value() == "o1"
        $().s2 == "o2"
        $().s2().value() == "o2"
    }

    def "single select setting nonexistent value"() {
        given:
        html {
            select(name: "s1") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = "o3"

        then:
        IllegalArgumentException e = thrown()
        e.message == "Couldn't select option with text or value: o3, available texts: [t1, t2], available values: [o1, o2]"
    }

    def "single select setting null value"() {
        html {
            select(name: "s1") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = null

        then:
        IllegalArgumentException e = thrown()
        e.message == "Couldn't select option with text or value: null, available texts: [t1, t2], available values: [o1, o2]"
    }

    def "single select setting empty list value"() {
        html {
            select(name: "s1") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = []

        then:
        IllegalArgumentException e = thrown()
        e.message == "Couldn't select option with text or value: [], available texts: [t1, t2], available values: [o1, o2]"
    }

    def "single select setting by value"() {
        given:
        html {
            select(name: "s1") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = "o2"

        then:
        $().s1 == "o2"
    }

    def "single select setting by text"() {
        given:
        html {
            select(name: "s1") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = "t2"

        then:
        $().s1 == "o2"
    }

    def "multiSelect - read"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "o1")
                option(value: "o2", "o2")
            }
            select(name: "s2", multiple: "multiple") {
                option(value: "o1", "o1")
                option(value: "o2", selected: "selected", "o2")
            }
            select(name: "s3", multiple: "multiple") {
                option(value: "o1", selected: "selected", "o1")
                option(value: "o2", selected: "selected", "o2")
            }
        }

        expect:
        $().s1 == []
        $().s1().value() == []
        $().s2 == ["o2"]
        $().s2().value() == ["o2"]
        $().s3 == ["o1", "o2"]
        $().s3().value() == ["o1", "o2"]
    }

    def "multiselect setting nonexistent value"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "o1")
                option(value: "o2", "o2")
            }
        }

        when:
        $().s1 = "o3"

        then:
        IllegalArgumentException e = thrown()
        e.message == "Couldn't select option with text or value: o3, available texts: [o1, o2], available values: [o1, o2]"

        when:
        $().s1 = ["o1", "o3"]

        then:
        e = thrown()
        e.message == "Couldn't select option with text or value: o3, available texts: [o1, o2], available values: [o1, o2]"
    }

    def "multiSelect - set by value"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = "o3"

        then:
        thrown(IllegalArgumentException)

        when:
        $().s1 = ["o1", "o3"]

        then:
        thrown(IllegalArgumentException)

        when:
        $().s1 = "o2"

        then:
        $().s1().value() == ["o2"]

        when:
        $().s1 = ["o1"]

        then:
        $().s1().value() == ["o1"]
    }

    def "multiSelect - set by text"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = "t3"

        then:
        thrown(IllegalArgumentException)

        when:
        $().s1 = ["t1", "t3"]

        then:
        thrown(IllegalArgumentException)

        when:
        $().s1 = "t2"

        then:
        $().s1().value() == ["o2"]

        when:
        $().s1 = ["t1"]

        then:
        $().s1().value() == ["o1"]

        when:
        $().s1 = ["t1", "o1"]

        then:
        $().s1().value() == ["o1"]
    }
}
