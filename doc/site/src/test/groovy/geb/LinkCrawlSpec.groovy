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

import geb.crawl.Crawler
import geb.crawl.PrettyPrintCollection
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification

import static geb.crawl.Crawler.Link
import static geb.crawl.Crawler.Response

@Slf4j
class LinkCrawlSpec extends Specification {

    def "site has no bad links"() {
        given:
        def aut = ApplicationContext.run(EmbeddedServer)

        def allowBroken = ["https://travis-ci.org", "http://markmail.org", "https://circleci.com", "https://saucelabs.com", "https://wiki.saucelabs.com", "http://ldaley.com"]

        String startingUrl = "http://localhost:${aut.port}"
        Set<String> knowBadHosts = ["markmail.org", "ldaley.com"] as Set<String>
        def crawler = new Crawler(startingUrl, knowBadHosts) {
            boolean shouldUseHeadRequest(Link url) {
                !(url.uri.host in ["blog.proxerd.pl", "search.maven.org"]) && super.shouldUseHeadRequest(url)
            }

            boolean shouldValidateFragment(Link url) {
                url.uri.host != "search.maven.org"
            }

            @Override
            boolean isCrawlable(Link link) {
                def path = link.uri.path
                if (path.startsWith("/manual") && !path.startsWith("/manual/snapshot")) {
                    false
                } else if (path.endsWith("api/index-all.html") || path.endsWith("api/help-doc.html") || path.endsWith("package-summary.html") || path.endsWith("deprecated-list.html")) {
                    false
                } else {
                    super.isCrawlable(link)
                }
            }

            List<String> findPageLinks(Response response) {
                def document = response.document
                document == null ? [] : document.select("body a")*.attr("href").findAll { it }
            }

            @Override
            void addPageErrors(Link link, Response response) {
                if (isCrawlable(link)) {
                    response.document?.text()?.findAll(~/(link:[^\s]+)|([^$]\{[a-z\-]+\})/)?.each {
                        link.errors << new BadLinkSyntax(it)
                    }
                }
                super.addPageErrors(link, response)
            }
        }

        when:
        def visited = crawler.crawl()
        def broken = visited.findAll { it.errors.size() > 0 }
        def brokenByLevel = broken.groupBy { link -> allowBroken.any { link.uri.toString().startsWith(it) } ? "warn" : "error" }
        def errored = new PrettyPrintCollection(brokenByLevel["error"] ?: [])
        def warned = new PrettyPrintCollection(brokenByLevel["warn"] ?: [])
        if (!warned.empty) {
            log.warn "${warned}"
        }

        then:
        errored.empty

        cleanup:
        aut.stop()
    }

    private static class BadLinkSyntax {
        final String link

        BadLinkSyntax(String link) {
            this.link = link
        }

        @Override
        String toString() {
            "Bad link: $link"
        }
    }
}
