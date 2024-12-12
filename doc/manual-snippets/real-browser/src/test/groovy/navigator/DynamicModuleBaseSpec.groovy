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
package navigator

import geb.Module
import geb.Page
import geb.test.GebSpecWithCallbackServer
import org.apache.http.entity.ContentType
import org.openqa.selenium.StaleElementReferenceException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DynamicModuleBaseSpec extends GebSpecWithCallbackServer {

    @SuppressWarnings("GStringExpressionWithinString")
    def setup() {
        callbackServer.get = { HttpServletRequest request, HttpServletResponse response ->
            if (request.requestURI.endsWith("/vue-2.6.10.min.js")) {
                response.contentType = "text/javascript"
                response.writer << getClass().getResource("/vue-2.6.10.min.js").text
            } else {
                response.contentType = ContentType.TEXT_HTML.toString()
                response.characterEncoding = "utf8"
                response.writer << '''
                    <html>
                        <head>
                            <script type="text/javascript" src="/vue-2.6.10.min.js"></script>
                        </head>
                        <body>
                            // tag::template[]
                            <div id="app">
                                <ul>
                                    <li v-for="(berry,index) in berries" :key="`${berry}-${index}`">
                                        <button v-if="index != 0" v-on:click="swapWithNext(index - 1)">⇑</button>
                                        <button v-if="index != berries.length - 1" v-on:click="swapWithNext(index)">⇓</button>
                                        <span>{{ berry }}</span>
                                    </li>
                                </ul>
                            </div>
                            // end::template[]
                            <script type="text/javascript">
                                // tag::vue_app[]
                                var app = new Vue({
                                    el: '#app',
                                    data: {
                                        berries: [
                                            'strawberry',
                                            'raspberry',
                                            'blueberry',
                                            'cranberry'
                                        ]
                                    },
                                    methods: {
                                        swapWithNext: function(index) {
                                            this.berries.splice(index, 2, this.berries[index + 1], this.berries[index])
                                        }
                                    }
                                });
                                // end::vue_app[]
                            </script>
                        </body>
                    </html>
                '''
            }
        }
    }

    def "using a static navigator as the base for a module which is re-rendered"() {
        given:
        to PageWithList

        when:
        // tag::exception[]
        item("blueberry").moveUpBy(2)
        // end::exception[]

        then:
        thrown(StaleElementReferenceException)
    }

    def "using a dynamic navigator as the base for a module which is re-rendered"() {
        given:
        // tag::no_exception[]
        to PageWithListHandlingRerenders
        // end::no_exception[]

        when:
        // tag::no_exception[]
        item("blueberry").moveUpBy(2)

        // end::no_exception[]

        then:
        // tag::no_exception[]
        assert items*.text() == ["blueberry", "strawberry", "raspberry", "cranberry"]
        // end::no_exception[]
    }
}

class PageWithList extends Page {
    // tag::static_navigator[]
    static content = {
        // end::static_navigator[]
        items { $("li").moduleList(ListItem) }
        // tag::static_navigator[]
        item { text -> $("li", text: endsWith(text)).module(ListItem) }
    }
    // end::static_navigator[]
}

// tag::dynamic_navigator[]
class PageWithListHandlingRerenders extends Page {
    static content = {
        items { $("li").moduleList(ListItem) }
        item { text -> $("li", text: endsWith(text), dynamic: true).module(ListItem) }
    }
}
// end::dynamic_navigator[]

// tag::module[]
class ListItem extends Module {
    static content = {
        textElement { $("span") }
        upButton { $("button", text: "⇑") }
    }

    void moveUpBy(int count) {
        count.times { upButton.click() }
    }

    @Override
    String text() {
        textElement.text()
    }
}
// end::module[]
