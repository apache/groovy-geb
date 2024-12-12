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

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Page

class ContentDslSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html """
            <html>
                // tag::html[]
                <div id="a">a</div>
                // end::html[]
            </html>
        """
    }

    def "introduction"() {
        expect:
        // tag::page_with_div_usage[]
        Browser.drive {
            to PageWithDiv
            assert theDiv.text() == "a"
        }
        // end::page_with_div_usage[]
    }

    def "verbose"() {
        expect:
        // tag::verbose_page_with_div_usage[]
        Browser.drive {
            to PageWithDiv
            assert page.theDiv.text() == "a"
        }
        // end::verbose_page_with_div_usage[]
    }

    def "accessing content using a method"() {
        expect:
        // tag::accessing_content_using_a_method[]
        Browser.drive {
            to PageWithDiv

            // Following two lines are equivalent
            assert theDiv.text() == "a"
            assert theDiv().text() == "a"
        }
        // end::accessing_content_using_a_method[]
    }

    def "templated definitions"() {
        expect:
        // tag::templated_page_with_div_usage[]
        Browser.drive {
            to TemplatedPageWithDiv
            assert theDiv("a").text() == "a"
        }
        // end::templated_page_with_div_usage[]
    }

    def "content not returning navigator"() {
        expect:
        // tag::page_with_string_content_usage[]
        Browser.drive {
            to PageWithStringContent
            assert theDivText == "a"
        }
        // end::page_with_string_content_usage[]
    }

    def "content reuse"() {
        expect:
        Browser.drive {
            to PageWithContentReuse
            assert theDivText == "a"
        }
    }

    def "content using a field"() {
        expect:
        Browser.drive {
            to PageWithContentUsingAField
            assert theDiv.text() == "a"
        }
    }

    def "content using a method"() {
        expect:
        Browser.drive {
            to PageWithContentUsingAMethod
            assert theDiv.text() == "a"
        }
    }
}

// tag::page_with_div[]
class PageWithDiv extends Page {
    static content = {
        theDiv { $('div', id: 'a') }
    }
}
// end::page_with_div[]

// tag::templated_page_with_div[]
class TemplatedPageWithDiv extends Page {
    static content = {
        theDiv { id -> $('div', id: id) }
    }
}
// end::templated_page_with_div[]

// tag::page_with_string_content[]
class PageWithStringContent extends Page {
    static content = {
        theDivText { $('div#a').text() }
    }
}
// end::page_with_string_content[]

// tag::page_with_content_reuse[]
class PageWithContentReuse extends Page {
    static content = {
        theDiv { $("div#a") }
        theDivText { theDiv.text() }
    }
}
// end::page_with_content_reuse[]

// tag::page_with_content_using_a_field[]
class PageWithContentUsingAField extends Page {
    static content = {
        theDiv { $('div', id: divId) }
    }

    def divId = "a"
}
// end::page_with_content_using_a_field[]

// tag::page_with_content_using_a_method[]
class PageWithContentUsingAMethod extends Page {
    static content = {
        theDiv { $('div', id: divId()) }
    }

    def divId() {
        "a"
    }
}
// end::page_with_content_using_a_method[]