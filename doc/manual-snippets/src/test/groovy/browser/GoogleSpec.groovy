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
package browser

// tag::class[]
import geb.Page
import geb.spock.GebSpec

class GoogleHomePage extends Page {
    static url = "https://www.google.com"
}

class GoogleSpec extends GebSpec {
    // end::class[]
    def setup() {
        driver.javascriptEnabled = false
    }
    // tag::class[]

    def "go method does NOT set the page"() {
        when:
        go "https://www.google.com"

        then:
        page == old(page)
        currentUrl.contains "google"
    }

    def "to method does set the page and change the current url"() {
        when:
        to GoogleHomePage

        then:
        page != old(page)
        currentUrl.contains "google"
    }

}
// end::class[]
