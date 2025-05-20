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
import geb.Page
import geb.url.UrlFragment
import spock.lang.Specification

class AdvancedNavigationSpec extends Specification {

    def "using to with page specifying an url"() {
        expect:
        // tag::to[]
        Browser.drive(baseUrl: "https://groovy.apache.org/geb/") {
            // end::to[]
            driver.javascriptEnabled = false
            // tag::to[]
            to PageObjectsPage
            assert currentUrl == "https://groovy.apache.org/geb/pages"
        }
        // end::to[]
    }

    def "using to with additional arguments"() {
        expect:
        // tag::to_with_args[]
        Browser.drive(baseUrl: "https://groovy.apache.org/geb/") {
            // end::to_with_args[]
            driver.javascriptEnabled = false
            // tag::to_with_args[]
            to ManualsPage, "7.0.0", "index.html"
            assert currentUrl == "https://groovy.apache.org/geb/manual/7.0.0/index.html"
        }
        // end::to_with_args[]
    }

    def "using to with custom convert to path"() {
        when:
        // tag::convert_to_path[]
        def someManualVersion = new Manual(version: "7.0.0")

        // end::convert_to_path[]
        then:
        // tag::convert_to_path[]
        Browser.drive(baseUrl: "https://groovy.apache.org/geb/") {
            // end::convert_to_path[]
            driver.javascriptEnabled = false
            // tag::convert_to_path[]
            to ManualsPage, someManualVersion
            assert currentUrl == "https://groovy.apache.org/geb/manual/7.0.0/index.html"
        }
        // end::convert_to_path[]
    }

    def "using to with named params"() {
        when:
        // tag::to_with_named_params[]
        def someManualVersion = new Manual(version: "7.0.0")

        // end::to_with_named_params[]
        then:
        // tag::to_with_named_params[]
        Browser.drive(baseUrl: "https://groovy.apache.org/geb/") {
            // end::to_with_named_params[]
            driver.javascriptEnabled = false
            // tag::to_with_named_params[]
            to ManualsPage, someManualVersion, flag: true
            assert currentUrl == "https://groovy.apache.org/geb/manual/7.0.0/index.html?flag=true"
        }
        // end::to_with_named_params[]
    }

    def "using to with fragments"() {
        expect:
        // tag::to_with_fragment[]
        Browser.drive(baseUrl: "https://groovy.apache.org/geb/") {
            // end::to_with_fragment[]
            driver.javascriptEnabled = false
            // tag::to_with_fragment[]
            to ManualsPage, UrlFragment.of("advanced-page-navigation"), "7.0.0", "index.html"
            assert currentUrl == "https://groovy.apache.org/geb/manual/7.0.0/index.html#advanced-page-navigation"
        }
        // end::to_with_fragment[]
    }

    def "using to with parameterized page"() {
        expect:
        // tag::to_with_parameterized_page[]
        Browser.drive(baseUrl: "https://groovy.apache.org/geb/") {
            // end::to_with_parameterized_page[]
            driver.javascriptEnabled = false
            // tag::to_with_parameterized_page[]
            to new ParameterizedManualsPage(version: "7.0.0", section: "advanced-page-navigation")
            assert currentUrl == "https://groovy.apache.org/geb/manual/7.0.0/index.html#advanced-page-navigation"
        }
        // end::to_with_parameterized_page[]
    }
}

// tag::pages_page[]
class PageObjectsPage extends Page {
    static url = "pages"
}
// end::pages_page[]

// tag::manuals_page[]
// tag::manuals_page_with_convert_to_path[]
class ManualsPage extends Page {
    static url = "manual"
    // end::manuals_page[]
    String convertToPath(Manual manual) {
        "/${manual.version}/index.html"
    }
    // tag::manuals_page[]
}
// end::manuals_page_with_convert_to_path[]
// end::manuals_page[]

// tag::parameterized_manuals_page[]
class ParameterizedManualsPage extends Page {
    String version
    String section

    @Override
    String convertToPath(Object[] args) {
        "manual/$version/index.html"
    }

    @Override
    UrlFragment getPageFragment() {
        UrlFragment.of(section)
    }
}
// end::parameterized_manuals_page[]

// tag::manual_class[]
class Manual {
    String version
}
// end::manual_class[]