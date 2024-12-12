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
import geb.test.browsers.FirefoxLinux
import geb.test.browsers.RequiresRealBrowser

import javax.servlet.http.HttpServletResponse

import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE

@RequiresRealBrowser
@FirefoxLinux
class ExceptionOnReportScreenshotSpec extends GebSpecWithCallbackServer {

    private final static GROUP_NAME = "exceptions"

    @IterationInterceptor
    @Delegate
    ReportsFolder reportsFolder = new ReportsFolder(GROUP_NAME)

    def setup() {
        callbackServer.get = { req, HttpServletResponse res ->
            res.addHeader(CONTENT_TYPE.asString(), 'application/xml')
            res.outputStream << '''<?xml version="1.0"?>
            <test></test>
            '''
        }
        browser.config.reporter = new ScreenshotReporter()
        browser.reportGroup(GROUP_NAME)
    }

    def "a stacktrace screenshot is taken when an exception is thrown while taking a screenshot"() {
        given:
        go()

        when:
        report("test")

        then:
        reportFileNames == ["001-001-a stacktrace screenshot is taken when an exception is thrown while taking a screenshot-test.png"].toSet()
    }

}
