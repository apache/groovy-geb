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
import geb.UnexpectedPage
import geb.error.UnexpectedPageException
import geb.test.GebSpecWithCallbackServer

class UnexpectedPagesSpec extends GebSpecWithCallbackServer {

    void setupConfigurationForPageNotFoundPageUnexpectedPage() {
        updateConfiguration("""
            import pages.PageNotFoundPage

            // tag::config[]
            unexpectedPages = [PageNotFoundPage]
            // end::config[]
        """)
    }

    void updateConfiguration(String unexpectedPagesConfig) {
        browser.config.rawConfig.merge(new ConfigSlurper().parse(unexpectedPagesConfig))
    }

    def setup() {
        html {
            p(id: "error-message", "Sorry but we could not find that page")
        }
    }

    @SuppressWarnings("ConstantAssertExpression")
    def "using unexpected page"() {
        given:
        setupConfigurationForPageNotFoundPageUnexpectedPage()

        expect:
        // tag::usage[]
        try {
            at ExpectedPage
            assert false //should not get here
        } catch (UnexpectedPageException e) {
            assert e.message.startsWith("An unexpected page ${PageNotFoundPage.name} was encountered")
        }
        // end::usage[]
    }

    def "checking for unexpected page"() {
        given:
        setupConfigurationForPageNotFoundPageUnexpectedPage()

        when:
        // tag::checking_for_unexpected_page[]
        at PageNotFoundPage
        // end::checking_for_unexpected_page[]

        then:
        noExceptionThrown()
    }

    @SuppressWarnings("ConstantAssertExpression")
    def "using unexpected page with custom message"() {
        given:
        updateConfiguration("unexpectedPages = [pages.PageNotFoundPageWithCustomMessage]")

        expect:
        // tag::usage_with_custom_message[]
        try {
            at ExpectedPage
            assert false //should not get here
        } catch (UnexpectedPageException e) {
            assert e.message.contains("Additional UnexpectedPageException message text")
        }
        // end::usage_with_custom_message[]
    }
}

// tag::unexpected_page[]
class PageNotFoundPage extends Page {
    static at = { $("#error-message").text() == "Sorry but we could not find that page" }
}
// end::unexpected_page[]

// tag::unexpected_page_with_custom_message[]
class PageNotFoundPageWithCustomMessage extends Page implements UnexpectedPage {
    static at = { $("#error-message").text() == "Sorry but we could not find that page" }

    @Override
    String getUnexpectedPageMessage() {
        "Additional UnexpectedPageException message text"
    }
}
// end::unexpected_page_with_custom_message[]

class ExpectedPage extends Page {
    static at = { }
}

