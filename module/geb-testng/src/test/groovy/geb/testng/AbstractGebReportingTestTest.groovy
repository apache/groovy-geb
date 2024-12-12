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
package geb.testng

import geb.report.ReporterSupport
import geb.test.CallbackHttpServer
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod

abstract class AbstractGebReportingTestTest extends GebReportingTest {

    protected static final String END_OF_METHOD_REPORT_LABEL = "end"

    private CallbackHttpServer server

    protected int methodNumber = 0
    protected int reportNumberInTest = 1
    protected int methodNumberOfInitTest = 0

    @BeforeClass
    void setUpClass() {
        server = new CallbackHttpServer(browser.config)
        server.start()
        server.get = { req, res ->
            res.outputStream << """
                <html>
                <body>
                    <div class="d1" id="d1">d1</div>
                </body>
                </html>
            """
        }
    }

    @BeforeMethod
    void setUp() {
        ++methodNumber
        reportNumberInTest = 1

        browser.baseUrl = server.baseUrl
        go()
    }

    @AfterClass
    void tearDown() {
        server.stop()
    }

    def doTestReport(methodName = "", label = "", methodNumber = this.methodNumber, reportCounter = reportNumberInTest, File reportDir = reportGroupDir) {
        def report = tryToFindReport(methodName, label, methodNumber, reportCounter, reportDir)

        assert report != null, "${ReporterSupport.toTestReportLabel(methodNumber, reportCounter, methodName, label)} not found in ${reportGroupDir.listFiles()}"
        assert report.exists()
        reportNumberInTest++

        assert report.text.contains('<div class="d1" id="d1">')
    }

    def tryToFindReport(methodName = "", label = "", methodNumber = this.methodNumber, reportCounter = reportNumberInTest, File reportDir = reportGroupDir) {
        def reportName = ReporterSupport.toTestReportLabel(methodNumber, reportCounter, methodName, label)
        reportDir.listFiles().find { it.name.startsWith reportName }
    }
}
