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
package geb

import geb.test.GebSpecWithCallbackServer
import geb.url.UrlFragment

class UrlFragmentSpec extends GebSpecWithCallbackServer {

    def setup() {
        def requests = 0
        html {
            requests++
            div requests
        }
    }

    def "can go to a specific fragment"() {
        when:
        go UrlFragment.of("fragment")

        then:
        currentUrl == "${server.baseUrl}#fragment"
    }

    def "can go to a specific path and fragment"() {
        when:
        go "path", UrlFragment.of("fragment")

        then:
        currentUrl == "${server.baseUrl}path#fragment"
    }

    def "can go to query parameters and fragment"() {
        when:
        go UrlFragment.of("fragment"), foo: "bar"

        then:
        currentUrl == "${server.baseUrl}?foo=bar#fragment"
    }

    def "can add additional query params and override fragment for a url"() {
        when:
        go "?foo=bar#original", UrlFragment.of("overridden"), fizz: "buzz"

        then:
        currentUrl == "${server.baseUrl}?foo=bar&fizz=buzz#overridden"
    }

    def "fragment from the url is used if an explicit one is not provided"() {
        when:
        go "#escaped%23fragment"

        then:
        currentUrl == "${server.baseUrl}#escaped%23fragment"
    }

    def "can go to a specific page fragment"() {
        when:
        to Page, UrlFragment.of("fragment"), "path"

        then:
        currentUrl == "${server.baseUrl}path#fragment"
    }

    def "can go via a specific page fragment"() {
        when:
        via Page, UrlFragment.of("fragment"), "path"

        then:
        currentUrl == "${server.baseUrl}path#fragment"
    }

    def "can go to a specific page fragment with query params"() {
        when:
        to Page, UrlFragment.of("fragment"), "path", foo: "bar"

        then:
        currentUrl == "${server.baseUrl}path?foo=bar#fragment"
    }

    def "can go via a specific page fragment with query params"() {
        when:
        via Page, UrlFragment.of("fragment"), "path", foo: "bar"

        then:
        currentUrl == "${server.baseUrl}path?foo=bar#fragment"
    }

    def "can go to a specific initialized page fragment"() {
        when:
        to new Page(), UrlFragment.of("fragment"), "path"

        then:
        currentUrl == "${server.baseUrl}path#fragment"
    }

    def "can go via a specific initialized page fragment"() {
        when:
        via new Page(), UrlFragment.of("fragment"), "path"

        then:
        currentUrl == "${server.baseUrl}path#fragment"
    }

    def "can go to a specific initialized page fragment with query params"() {
        when:
        to new Page(), UrlFragment.of("fragment"), "path", foo: "bar"

        then:
        currentUrl == "${server.baseUrl}path?foo=bar#fragment"
    }

    def "can go via a specific initialized page fragment with query params"() {
        when:
        via new Page(), UrlFragment.of("fragment"), "path", foo: "bar"

        then:
        currentUrl == "${server.baseUrl}path?foo=bar#fragment"
    }

    def "pages can declare string url fragments"() {
        when:
        to PageWithStringFragment

        then:
        currentUrl == "${server.baseUrl}#unescaped%23fragment"
    }

    def "pages can declare map url fragments"() {
        when:
        to PageWithMapFragment

        then:
        currentUrl == "${server.baseUrl}#foo=bar&fizz=buzz%2526buzz"
    }

    def "pages can use custom fragment logic"() {
        when:
        to PageWithCustomisedFragmentLogic

        then:
        currentUrl == "${server.baseUrl}#custom"
    }

    def "fragment declared in the page can be overridden"() {
        when:
        to PageWithStringFragment, UrlFragment.of("custom")

        then:
        currentUrl == "${server.baseUrl}#custom"
    }

    def "going to the same url with only the fragment differing causes the browser to request the page again"() {
        when:
        page RequestCountPage

        then:
        requestCount == 1

        when:
        to RequestCountPage, UrlFragment.of("initial")

        then:
        requestCount == 2

        when:
        to RequestCountPage, UrlFragment.of("final")

        then:
        requestCount == 3
    }

}

class PageWithStringFragment extends Page {
    static fragment = "unescaped#fragment"
}

class PageWithMapFragment extends Page {
    static fragment = [foo: "bar", fizz: "buzz&buzz"]
}

class PageWithCustomisedFragmentLogic extends Page {
    UrlFragment getPageFragment() {
        UrlFragment.of("custom")
    }
}

class RequestCountPage extends Page {
    static url = "request-count"

    static content = {
        requestCount { $("div").text().toInteger() }
    }
}
