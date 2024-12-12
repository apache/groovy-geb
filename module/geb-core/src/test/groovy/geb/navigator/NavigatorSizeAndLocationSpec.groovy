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

import geb.Module
import geb.Page
import geb.test.GebSpecWithCallbackServer

class NavigatorSizeAndLocationSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            res.outputStream << """
            <html>
            <head>
                <style>
                    .a {
                        position: absolute;
                        top: 10;
                        left: 20;
                        width: 30;
                        height: 40;
                    }
                    .b {
                        position: absolute;
                        top: 50;
                        left: 60;
                        width: 70;
                        height: 80;
                    }
                </style>
            </head>
            <body>
                <div class="a"/>
                <div class="b"/>
            </body>
            </html>"""
        }
    }

    def setup() {
        go()
    }

    def "size"() {
        expect:
        $(".a").height == 40
        $(".b").height == 80
        $("div")*.height == [40, 80]

        $(".a").width == 30
        $(".b").width == 70
        $("div")*.width == [30, 70]
    }

    def "location"() {
        expect:
        $(".a").x == 20
        $(".b").x == 60
        $("div")*.x == [20, 60]

        $(".a").y == 10
        $(".b").y == 50
        $("div")*.y == [10, 50]
    }

    def "available on page content"() {
        when:
        page TestPage

        then:
        div("a").height == 40
        div("a").width == 30

        and:
        div("b").height == 80
        div("b").width == 70
    }

    def "available on module"() {
        when:
        page TestPage

        then:
        module("a").height == 40
        module("a").width == 30

        and:
        module("b").height == 80
        module("b").width == 70
    }

}

class TestModule extends Module {
}

class TestPage extends Page {
    static content = {
        div { $("div.$it") }
        module { div(it).module TestModule }
    }
}

