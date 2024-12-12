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
package geb.report

import geb.spock.IterationInterceptor
import geb.test.GebSpecWithCallbackServer
import org.jsoup.Jsoup

import java.nio.charset.StandardCharsets

class FramesSourceReporterSpec extends GebSpecWithCallbackServer {

    private final static GROUP_NAME = "frames"

    @IterationInterceptor
    @Delegate
    ReportsFolder reportsFolder = new ReportsFolder(GROUP_NAME)

    def setupSpec() {
        responseHtml { request, response ->
            String pageName = (~'/(.*)').matcher(request.requestURI)[0][1]
            if (pageName == "frames") {
                frameset(rows: "25%,75%") {
                    frame(src: '/header')
                    frame(src: '/footer')
                }
            } else if (pageName == "iframe") {
                body {
                    iframe(src: '/inline')
                }
            } else {
                body { span("$pageName") }
            }
        }
    }

    def setup() {
        browser.config.reporter = new FramesSourceReporter()
        browser.reportGroup(GROUP_NAME)
    }

    def "reports on source of frames"() {
        given:
        go 'frames'

        when:
        report('test')

        then:
        reportSpanText('001-001-reports on source of frames-test-frame 1') == 'header'
        reportSpanText('001-001-reports on source of frames-test-frame 2') == 'footer'
    }

    def "reports on source of iframes"() {
        given:
        go 'iframe'

        when:
        report('test')

        then:
        reportSpanText('002-001-reports on source of iframes-test-frame 1') == 'inline'
    }

    String reportSpanText(String reportName) {
        def document = Jsoup.parse(reportFile(reportName), StandardCharsets.UTF_8.toString())
        document.select('html body span').text()
    }

    File reportFile(String reportName) {
        new File(groupDir, "${reportName}.html")
    }

}
