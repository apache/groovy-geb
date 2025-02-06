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

import geb.pages.ApiPage
import geb.pages.MainPage
import geb.pages.ManualPage
import geb.spock.GebSpec
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

@Stepwise
class SiteSmokeSpec extends GebSpec {

    @Shared
    EmbeddedServer app = ApplicationContext.run(EmbeddedServer)

    def setup() {
        browser.baseUrl = "http://localhost:${app.port}/".toString()
    }

    def cleanupSpec() {
        app.stop()
    }

    void 'index'() {
        when:
        go()

        then:
        at MainPage
        firstHeaderText == 'What is it?'
    }

    void 'manual and api links are available'() {
        when:
        go()

        then:
        at MainPage
        menu.manuals.text() == 'Manual'
        menu.manuals.links
        menu.apis.text() == 'API'
        menu.apis.links
    }

    @Unroll
    void 'manual - #manualVersion'() {
        when:
        println("Version: $link")
        go(link)

        then:
        at ManualPage
        version == manualVersion

        where:
        [manualVersion, link] << manualLinksData()
    }

    @Unroll
    void 'api - #link'() {
        given:
        browser.driver.javascriptEnabled = true

        when:
        go(link)

        then:
        at ApiPage

        where:
        link << apiLinksData()
    }

    private Document parseMainPage() {
        BlockingHttpClient client = app.applicationContext.createBean(HttpClient, app.URL).toBlocking()
        String html = client.retrieve("", String)
        Jsoup.parse(html)
    }

    private List<List<String>> manualLinksData() {
        def links = parseMainPage().select("#manuals-menu a")
        links.collect {
            [it.text(), it.attr('href')]
        }.findAll { !it[0].contains("snapshot") && !it[0].contains("current") }
    }

    private List<String> apiLinksData() {
        List<String> links = parseMainPage().select("#apis-menu a")*.attr('href')
        links
    }
}
