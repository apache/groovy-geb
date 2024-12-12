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
import geb.test.browsers.Chrome
import geb.test.browsers.Firefox
import geb.test.browsers.InternetExplorer
import geb.test.browsers.Safari

//Some functionality around multiselect seems not to work in Edge
@Chrome
@Firefox
@InternetExplorer
@Safari
class EdgeIncompatibleSelectControlSpec extends GebSpecWithCallbackServer {

    def "multiselect deselecting"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "o1", selected: "selected")
                option(value: "o2", "o2")
            }
            select(name: "s2", multiple: "multiple") {
                option(value: "o1", "o1")
                option(value: "o2", "o2", selected: "selected")
            }
        }

        when:
        $().s1 = null
        $().s2 = []

        then:
        $().s1 == []
        $().s2 == []
    }

    def "multiSelect - set multiple by value"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = ["o1", "o2"]

        then:
        $().s1().value() == ["o1", "o2"]
    }

    def "multiSelect - set multiple by text"() {
        given:
        html {
            select(name: "s1", multiple: "multiple") {
                option(value: "o1", "t1")
                option(value: "o2", "t2")
            }
        }

        when:
        $().s1 = ["t1", "t2"]

        then:
        $().s1().value() == ["o1", "o2"]
    }
}
