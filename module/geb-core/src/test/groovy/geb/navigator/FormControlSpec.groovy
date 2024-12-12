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
import spock.lang.Issue

@CrossBrowser
class FormControlSpec extends GebSpecWithCallbackServer {

    def textInputs() {
        when:
        html {
            input(name: "i1", type: "text", id: "i1", "")
            input(name: "i2", type: "text", id: "i2", value: "value", "")
        }

        def i1 = $().i1()
        def i2 = $().i2()

        then:
        i1*.@id == ["i1"]
        i1.value() == ""
        i1.value("foo")
        i1.value() == "foo"

        and:
        i2*.@id == ["i2"]
        i2.value() == "value"

        when:
        $().i1 = "bar"

        then:
        $().i1 == "bar"
    }

    def textarea() {
        when:
        html {
            textarea(name: "i1", id: "i1", "")
            textarea(name: "i2", id: "i2", "value")
        }

        def i1 = $().i1()
        def i2 = $().i2()

        then:
        i1*.@id == ["i1"]
        i1.value() == ""
        i1.value("foo")
        i1.value() == "foo"

        and:
        i2*.@id == ["i2"]
        i2.value() == "value"
        i2 << "-add"
        i2.value() in ["value-add", "-addvalue"] //not consistent across drivers

        when:
        $().i1 = "bar"

        then:
        $().i1 == "bar"
    }

    def checkbox() {
        when:
        html {
            input(name: "i1", type: "checkbox", id: "i1", value: "i1", "")
            input(name: "i2", type: "checkbox", id: "i2", value: "i2", checked: "checked", "")
        }

        def i1 = $().i1()
        def i2 = $().i2()

        then:
        i1*.@id == ["i1"]
        !i1.value()
        i1.value(true)
        i1.value() == "i1"

        and:
        i2*.@id == ["i2"]
        i2.value() == "i2"
        i2.value(false)
        !i2.value()

        when:
        $().i1 = false

        then:
        !$().i1

        when:
        $().i2 = "i2"

        then:
        $().i2 == "i2"

        when:
        $().i1 = "not-the-value"

        then:
        !$().i1
    }

    def "multiple checkboxes"() {
        html {
            input(name: "c", type: "checkbox", value: "i1", id: "i1")
            label(for: "i1", "i1 label")
            input(name: "c", type: "checkbox", value: "i2", id: "i2")
            label(for: "i2", "i2 label")
            input(name: "c", type: "checkbox", value: "i3", id: "i3")
            label(for: "i3", "i3 label")
        }

        when:
        $().c = true

        then:
        $().c == ["i1", "i2", "i3"]

        when:
        $().c = false

        then:
        !$().c

        when:
        $().c = true
        $().c = null

        then:
        !$().c

        when:
        $().c = true
        $().c = []

        then:
        !$().c

        when:
        $().c = ["i1", "i3"]

        then:
        $().c == ["i1", "i3"]

        when:
        $().c = "i1"

        then:
        $().c == "i1"

        when:
        $().c = ["i2 label", "i3 label"]

        then:
        $().c == ["i2", "i3"]

        when:
        $().c = "i1 label"

        then:
        $().c == "i1"
    }

    def "radio - by value"() {
        when:
        html {
            input(type: "radio", name: "r", value: "r1", checked: "checked")
            input(type: "radio", name: "r", value: "r2")
            input(type: "radio", name: "r", value: "r3")
        }

        then:
        $().r == "r1"

        when:
        $().r = "r2"

        then:
        $().r == "r2"

        when:
        $().r = "foo" // TODO: should throw exception

        then:
        $().r == "r2"
    }

    @Issue("https://github.com/geb/issues/issues/37")
    def "radio - by label text"() {
        when:
        html {
            input(type: "radio", name: "r", value: "r1", id: "r1", "")
            label(for: "r1", "r1 label")
            input(type: "radio", name: "r", value: "r2", id: "r2")
            label(for: "r2", "r2 label")
            input(type: "radio", name: "r", value: "r3", id: "r3")
            label(for: "r3", "r3 label")
        }

        then:
        $().r == null

        when:
        $().r = "r1 label"

        then:
        $().r == "r1"

        when:
        $().r = "r3 label"

        then:
        $().r == "r3"
    }

    def "appending"() {
        given:
        html {
            input(name: "i", "")
            textarea(name: "t", "")
        }

        when:
        $().i() << "a"

        then:
        $().i == "a"

        when:
        $().t() << "a"

        then:
        $().t == "a"

        when:
        $("input,textarea") << "b"

        then:
        $().i == "ab"
        $().t == "ab"
    }
}
