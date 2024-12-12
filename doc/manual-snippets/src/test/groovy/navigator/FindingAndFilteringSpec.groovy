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

import geb.navigator.Navigator
import geb.test.GebSpecWithCallbackServer

class FindingAndFilteringSpec extends GebSpecWithCallbackServer {

    def setup() {
        responseHtml """
            <html>
                // tag::html[]
                <div class="a">
                    <p class="b">geb</p>
                </div>
                <div class="b">
                    <input type="text"/>
                </div>
                // end::html[]
            <html>
        """

        go()
    }

    def "selectors work as expected"() {
        expect:
        bParagraphUsingFind() == $("p.b")
        bParagraphUsingDollar() == $("p.b")
        bDivUsingFilter() == $("div.b")
        bDivUsingNot() == $("div.b")
        aDivUsingHas() == $("div.a")
        bDivUsingHas() == $("div.b")
        bDivUsingHasNot() == $("div.b")
        aDivUsingHasNot() == $("div.a")
        divsUsingHasNot() == $("div")
    }

    private Navigator bParagraphUsingFind() {
        // tag::find[]
        $("div").find(".b")
        // end::find[]
    }

    private Navigator bParagraphUsingDollar() {
        // tag::dollar[]
        $("div").$(".b")
        // end::dollar[]
    }

    private Navigator bDivUsingFilter() {
        // tag::filter[]
        $("div").filter(".b")
        // end::filter[]
    }

    private Navigator bDivUsingNot() {
        // tag::not[]
        $(".b").not("p")
        // end::not[]
    }

    private Navigator aDivUsingHas() {
        // tag::a_has[]
        $("div").has("p")
        // end::a_has[]
    }

    private Navigator bDivUsingHas() {
        // tag::b_has[]
        $("div").has("input", type: "text")
        // end::b_has[]
    }

    private Navigator bDivUsingHasNot() {
        // tag::b_has_not[]
        $("div").hasNot("p")
        // end::b_has_not[]
    }

    private Navigator aDivUsingHasNot() {
        // tag::a_has_not[]
        $("div").hasNot("input", type: "text")
        // end::a_has_not[]
    }

    private Navigator divsUsingHasNot() {
        // tag::has_not[]
        $("div").hasNot("input", type: "submit")
        // end::has_not[]
    }
}