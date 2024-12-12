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
package intro

// tag::imports[]
import geb.Browser
// end::imports[]
import intro.page.GebHomePage
import intro.page.TheBookOfGebPage
import spock.lang.Specification

class ScriptingSpec extends Specification {

    def "inline"() {
        expect:
        // tag::inline[]
        Browser.drive {
            go "http://gebish.org"

            assert title == "Geb - Very Groovy Browser Automation" // <1>

            $("div.menu a.manuals").click() //<2>
            waitFor { !$("#manuals-menu").hasClass("animating") } //<3>

            $("#manuals-menu a")[0].click() //<4>

            assert title.startsWith("The Book Of Geb") // <5>
        }
        // end::inline[]
    }

    def "using page objects"() {
        expect:
        // tag::using_page_objects[]
        Browser.drive {
            to GebHomePage //<1>

            manualsMenu.open()

            manualsMenu.links[0].click()

            at TheBookOfGebPage
        }
        // end::using_page_objects[]
    }
}