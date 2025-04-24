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
import geb.window.BaseWindowHandlingSpec
import org.jsoup.Jsoup

import java.nio.charset.StandardCharsets

class MultiWindowReporterSpec extends BaseWindowHandlingSpec {

    private final static GROUP_NAME = "windows"

    @IterationInterceptor
    @Delegate
    ReportsFolder temporaryFolder = new ReportsFolder(GROUP_NAME)

    def setup() {
        browser.config.reporter = new MultiWindowReporter(new PageSourceReporter())
        browser.reportGroup(GROUP_NAME)
    }

    def "writes report for each open window"() {
        given:
        openAllWindows()

        when:
        report("test")

        then:
        reportFileNames == availableWindows.collect { "001-001-writes report for each open window-test-window_${it}.html" }.toSet()

        and:
        linkTextsInReports == [
                ["main-1", "main-2"],
                ["main-1-1", "main-1-2"],
                ["main-2-1", "main-2-2"]
        ].toSet()
    }

    def "does not include window id in report name if there is only a single window open"() {
        given:
        go MAIN_PAGE_URL

        when:
        report("test")

        then:
        reportFileNames == ["002-001-does not include window id in report name if there is only a single window open-test.html"].toSet()
    }

    def "does not read backing from SUT but refers to its field"() {
        given:
        openAllWindows()
        html {
            input(type: "text", name: "backing")
        }

        when:
        report("test")

        then:
        notThrown(MissingMethodException)
    }

    Set<List<String>> getLinkTextsInReports() {
        groupDir.listFiles().collect {
            def document = Jsoup.parse(it, StandardCharsets.UTF_8.toString())
            document.select('html body a')*.text()
        }
    }

}
