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
package pages

import geb.Page
import geb.test.GebSpecWithCallbackServer

class AtSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            a(id: "to-page-with-at-checker", "link")
            h1("Example")
        }
    }

    def "verify at"() {
        expect:
        // tag::verify_at[]
        to PageLeadingToPageWithAtChecker
        link.click()
        page PageWithAtChecker
        verifyAt()
        // end::verify_at[]
    }

    def "at"() {
        expect:
        // tag::at[]
        to PageLeadingToPageWithAtChecker
        link.click()
        at PageWithAtChecker
        // end::at[]
    }

    def "at using content"() {
        expect:
        at PageWithAtCheckerUsingContent
    }
}

// tag::introduction[]
class PageWithAtChecker extends Page {
    static at = { $("h1").text() == "Example" }
}
// end::introduction[]

// tag::leading_to_page[]
class PageLeadingToPageWithAtChecker extends Page {
    static content = {
        link { $("a#to-page-with-at-checker") }
    }
}
// end::leading_to_page[]

// tag::at_checker_using_content_definition[]
class PageWithAtCheckerUsingContent extends Page {
    static at = { heading == "Example" }

    static content = {
        heading { $("h1").text() }
    }
}
// end::at_checker_using_content_definition[]