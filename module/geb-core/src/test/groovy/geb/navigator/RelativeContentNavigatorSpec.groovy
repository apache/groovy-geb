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

import geb.test.browsers.CrossBrowser
import geb.test.GebSpecWithCallbackServer

@CrossBrowser
class RelativeContentNavigatorSpec extends GebSpecWithCallbackServer {

    def children() {
        given:
        html {
            div(id: "a") {
                div('class': 'a', id: "aa", "aa")
                div('class': 'b', id: "ab", "ab")
            }
            div(id: "b") {
                div('class': 'a', id: "ba", "ba")
                div('class': 'b', id: "bb", "bb")
            }
        }

        expect:
        $("#a").children("#ab")*.text() == ["ab"]
        $("#a").children()*.text() == ["aa", "ab"]
        $("div").children(".a")*.text() == ["aa", "ba"]
        $("#a").add("#b").children()*.@id == ["aa", "ab", "ba", "bb"]
        $("#a").children(class: 'b')*.text() == ["ab"]
        $("div").children(class: 'a')*.text() == ["aa", "ba"]
        $("div").children(class: 'non-existent')*.text() == []
        $("#a").children("#ab", class: 'b')*.text() == ["ab"]
        $("div").children(".a", class: 'a')*.text() == ["aa", "ba"]
        $("div").children(".a", class: 'non-existent')*.text() == []
    }

    def siblings() {
        given:
        html {
            div(id: "a") {
                div(id: "aa", class: 'a', "")
                div(id: "ba", class: 'a', "")
                div(id: "ca", class: 'b', "")
                div(id: "da", class: 'b', "")
            }
            div(id: "b") {
                div(id: "ab", class: 'a', "")
                div(id: "bb", class: 'a', "")
                div(id: "cb", class: 'b', "")
                div(id: "db", class: 'b', "")
            }
        }

        expect:
        $("#ca").siblings()*.@id == ["aa", "ba", "da"]
        $("#a div").siblings("#aa").unique()*.@id == ["aa"]
        $("#ca").add("#cb").siblings().unique()*.@id == ["aa", "ba", "da", "ab", "bb", "db"]
        $("#ba").siblings(class: 'a')*.@id == ["aa"]
        $("#aa").add("#ab").siblings(class: 'b')*.@id == ["ca", "da", "cb", "db"]
        $("#ba").siblings(".a", class: 'a')*.@id == ["aa"]
        $("#aa").add("#ab").siblings(".b", class: 'b')*.@id == ["ca", "da", "cb", "db"]
    }

    def parent() {
        given:
        html {
            div(id: "a", 'class': 'parent') {
                div(id: "a-a") {
                    div(id: "a-a-a", "")
                }
                div(id: "a-b") {
                    div(id: "a-b-a", "")
                }
            }
            div(id: "b", 'class': 'parent') {
                div(id: "b-a") {
                    div(id: "b-a-a", "")
                }
                div(id: "b-b") {
                    div(id: "b-b-a", "")
                }
            }
        }

        expect:
        $("#a-b-a").parent()*.@id == ["a-b"]
        $("#b-a").parent()*.@id == ["b"]
        $("#a-b-a").parent("div")*.@id == ["a-b"]
        $("#a-b-a").parent("p")*.@id == []
        $("#a-a").parent(class: "parent")*.@id == ["a"]
        $("#a-a").parent(class: "non-existent")*.@id == []
        $("#a-a").add("#b-a").parent(class: "parent")*.@id == ["a", "b"]
        $("#a-a").parent("#a", class: "parent")*.@id == ["a"]
        $("#a-a").parent("#a", class: "non-existent")*.@id == []
        $("#a-a").add("#b-a").parent("div", class: "parent")*.@id == ["a", "b"]
    }

    def parents() {
        given:
        html {
            div(id: "a", 'class': 'parent') {
                div(id: "a-a") {
                    div(id: "a-a-a", "")
                }
                div(id: "a-b", 'class': 'parent') {
                    div(id: "a-b-a", "")
                }
            }
            div(id: "b", 'class': 'parent') {
                div(id: "b-a") {
                    div(id: "b-a-a", "")
                }
                div(id: "b-b") {
                    div(id: "b-b-a", "")
                }
            }
        }

        expect:
        $("#a-b-a").parents()*.@id == ["a-b", "a", "", ""]
        $("#b-a").parents()*.@id == ["b", "", ""]
        $("#a-b-a").parents("div")*.@id == ["a-b", "a"]
        $("#a-b-a").parents("p")*.@id == []
        $("#a-b-a").parents(class: 'parent')*.@id == ["a-b", "a"]
        $("#a-b-a").add("#b-b-a").parents(class: 'parent')*.@id == ["a-b", "a", "b"]
        $("#a-b-a").parents(class: 'non-existent')*.@id == []
        $("#a-b-a").parents("div", class: 'parent')*.@id == ["a-b", "a"]
        $("#a-b-a").add("#b-b-a").parents("div", class: 'parent')*.@id == ["a-b", "a", "b"]
        $("#a-b-a").parents("#a-b", class: 'non-existent')*.@id == []
    }

    def parentsUntil() {
        given:
        html {
            div(id: "a", 'class': 'parent') {
                div(id: "a-a") {
                    div(id: "a-a-a", "")
                }
                div(id: "a-b") {
                    div(id: "a-b-a", "")
                }
            }
            div(id: "b") {
                div(id: "b-a") {
                    div(id: "b-a-a", "")
                }
                div(id: "b-b") {
                    div(id: "b-b-a", "")
                }
            }
        }

        expect:
        $("#a-b-a").parentsUntil("#a")*.@id == ["a-b"]
        $("#b-a").parentsUntil("html")*.@id == ["b", ""]
        $("foo").parentsUntil("div")*.@id == []
        $("#a-a-a").parentsUntil(class: 'parent')*.@id == ["a-a"]
        $("#a-a-a").parentsUntil(class: 'non-existent')*.@id == ["a-a", "a", "", ""]
        $("#a-a-a").parentsUntil("#a", class: 'parent')*.@id == ["a-a"]
        $("#a-a-a").parentsUntil("#a-a", class: 'non-existent')*.@id == ["a-a", "a", "", ""]
    }

    def closest() {
        given:
        html {
            div(id: "a", 'class': 'closest') {
                div(id: "a-a") {
                    div(id: "a-a-a", "")
                }
                div(id: "a-b") {
                    div(id: "a-b-a", "")
                }
            }
            div(id: "b") {
                div(id: "b-a") {
                    div(id: "b-a-a", "")
                }
                div(id: "b-b") {
                    div(id: "b-b-a", "")
                }
            }
        }

        expect:
        $("#a-a-a").closest("#a")*.@id == ["a"]
        $("#a-a-a").closest("#a-a")*.@id == ["a-a"]
        $("#b-a-a").closest("#b")*.@id == ["b"]
        $("#a-a-a").add("#b-a-a").closest("div")*.@id == ["a-a", "b-a"]
        $("#a-a-a").closest(class: 'closest')*.@id == ["a"]
        $("#a-a-a").closest(class: 'non-existent')*.@id == []
        $("#a-a-a").closest("#a", class: 'closest')*.@id == ["a"]
        $("#a-a-a").closest("#a", class: 'non-existent')*.@id == []
    }

    def next() {
        given:
        html {
            div(id: "a", "")
            div(id: "b", 'class': "div", "")
            div(id: "c", 'class': "div", "")
        }

        expect:
        $("#b").next()*.@id == ["c"]
        $("#b").next("#e")*.@id == []
        $("#a").next()*.@id == ["b"]
        $("#c").next()*.@id == []
        $("#a").next(class: "div")*.@id == ["b"]
        $("#a").add("#b").next(class: "div")*.@id == ["b", "c"]
        $("#a").next("#c", class: "div")*.@id == ["c"]
        $("#a").add("#b").next("div", class: "div")*.@id == ["b", "c"]
    }

    def nextAll() {
        given:
        html {
            div(id: "a", "")
            div(id: "b", "")
            div(id: "c", 'class': "div", "")
            div(id: "d", 'class': "div", "")
        }

        expect:
        $("#b").nextAll()*.@id == ["c", "d"]
        $("#b").nextAll("#d")*.@id == ["d"]
        $("#a").nextAll()*.@id == ["b", "c", "d"]
        $("#d").nextAll()*.@id == []
        $("#a").nextAll(class: "div")*.@id == ["c", "d"]
        $("#a").nextAll("div", class: "div")*.@id == ["c", "d"]
    }

    def nextUntil() {
        given:
        html {
            div(id: "a") {
                div(id: "aa", "")
                div(id: "ba", "")
                div(id: "ca", "")
                div(id: "da", 'class': 'end', "")
            }
            div(id: "b") {
                div(id: "ab", "")
                div(id: "bb", "")
                div(id: "cb", "")
                div(id: "db", 'class': 'end', "")
            }
        }

        expect:
        $("#ba").nextUntil("#da")*.@id == ["ca"]
        $("#ab").nextUntil("#db")*.@id == ["bb", "cb"]
        $("#aa").add("#ab").nextUntil(".end")*.@id == ["ba", "ca", "bb", "cb"]
        $("#aa").nextUntil(class: "end")*.@id == ["ba", "ca"]
        $("#aa").add("#ab").nextUntil(class: "end")*.@id == ["ba", "ca", "bb", "cb"]
        $("#aa").nextUntil(class: "non-existent")*.@id == ["ba", "ca", "da"]
        $("#aa").nextUntil("#da", class: "end")*.@id == ["ba", "ca"]
        $("#aa").nextUntil("#zz", class: "non-existent")*.@id == ["ba", "ca", "da"]
    }

    def previous() {
        given:
        html {
            div(id: "a") {
                div(id: "aa", 'class': 'start', "")
                div(id: "ba", "")
                div(id: "ca", "")
                div(id: "da", 'class': 'end', "")
            }
            div(id: "b") {
                div(id: "ab", class: 'start', "")
                div(id: "bb", "")
                div(id: "cb", "")
                div(id: "db", 'class': 'end', "")
            }
        }

        expect:
        $("#b").previous()*.@id == ["a"]
        $("#aa").previous()*.@id == []
        $("#ba").previous()*.@id == ["aa"]
        $("#db").previous()*.@id == ["cb"]
        $("#db").previous("#cb")*.@id == ["cb"]
        $("foo").previous("#cb")*.@id == []
        $("#db").previous(class: 'start')*.@id == ["ab"]
        $("#db").previous(class: 'non-existent')*.@id == []
        $("#db").add("#da").previous(class: 'start')*.@id == ["ab", "aa"]
        $("#db").previous("#ab", class: 'start')*.@id == ["ab"]
        $("#db").previous("#ab", class: 'non-existent')*.@id == []
        $("#db").add("#da").previous("div", class: 'start')*.@id == ["ab", "aa"]
    }

    def prevAll() {
        given:
        html {
            div(id: "a") {
                div(id: "aa", 'class': 'same', "")
                div(id: "ba", 'class': 'same', "")
                div(id: "ca", "")
                div(id: "da", 'class': 'end', "")
            }
            div(id: "b") {
                div(id: "ab", 'class': 'same', "")
                div(id: "bb", 'class': 'same', "")
                div(id: "cb", "")
                div(id: "db", 'class': 'end', "")
            }
        }

        expect:
        $("#b").prevAll()*.@id == ["a"]
        $("#aa").prevAll()*.@id == []
        $("#ba").prevAll()*.@id == ["aa"]
        $("#db").prevAll()*.@id == ["ab", "bb", "cb"]
        $("#db").prevAll("#bb")*.@id == ["bb"]
        $("foo").prevAll("#bb")*.@id == []
        $("#db").prevAll(class: "same")*.@id == ["bb", "ab"]
        $("#db").add("#da").prevAll(class: "same")*.@id == ["bb", "ab", "ba", "aa"]
        $("#db").prevAll(class: "non-existent")*.@id == []
        $("#db").prevAll("div", class: "same")*.@id == ["bb", "ab"]
        $("#db").add("#da").prevAll("div", class: "same")*.@id == ["bb", "ab", "ba", "aa"]
        $("#db").prevAll("#db", class: "non-existent")*.@id == []
    }

    def prevUntil() {
        given:
        html {
            div(id: "a") {
                div(id: "aa", 'class': 'start', "")
                div(id: "ba", "")
                div(id: "ca", "")
                div(id: "da", 'class': 'end', "")
            }
            div(id: "b") {
                div(id: "ab", 'class': 'start', "")
                div(id: "bb", "")
                div(id: "cb", "")
                div(id: "db", 'class': 'end', "")
            }
        }

        expect:
        $("#db").prevUntil("#ab")*.@id == ["cb", "bb"]
        $("#db").prevUntil("foo")*.@id == ["cb", "bb", "ab"]
        $("foo").prevUntil("foo")*.@id == []
        $("#db").prevUntil(class: "start")*.@id == ["cb", "bb"]
        $("#db").prevUntil(class: "non-existent")*.@id == ["cb", "bb", "ab"]
        $("#db").add("#da").prevUntil(class: "start")*.@id == ["cb", "bb", "ca", "ba"]
        $("#db").prevUntil("div", class: "start")*.@id == ["cb", "bb"]
        $("#db").prevUntil("#ab", class: "non-existent")*.@id == ["cb", "bb", "ab"]
        $("#db").add("#da").prevUntil("div", class: "start")*.@id == ["cb", "bb", "ca", "ba"]
    }
}
