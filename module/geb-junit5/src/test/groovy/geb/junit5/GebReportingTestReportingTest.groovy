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
package geb.junit5

import geb.junit5.fixture.TestRunner
import geb.test.CallbackHttpServer
import org.jsoup.Jsoup
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GebReportingTestReportingTest {

    final static String REPORTING_SPEC_TEST = "GebReportingExtendingTest"

    static CallbackHttpServer server = new CallbackHttpServer()

    @TempDir
    public File tempDir

    @BeforeAll
    static void setup() {
        server.start()
        server.html {
            body {
                div "Test page"
            }
        }
    }

    @AfterAll
    static void tearDown() {
        server.stop()
    }

    @Test
    void reportsAreWrittenAtTheEndOfEachTestWhenConfigured() {
        runSuccessfully("""
            @BeforeEach
            void setupReportingAfterEachTest() {
                config.reportOnTestFailureOnly = false
            }

            @Test
            void successfulTest() {
                go()
            }
        """)

        def doc = Jsoup.parse(reportFile("001-001-successfulTest-end.html").text)
        assert doc.select("div")*.text("Test page")
    }

    @Test
    void canWriteReportsWithTestDerivedName() {
        runSuccessfully("""
            @Test
            void successfulTest() {
                go()
                report("custom")
            }
        """)

        assert reportFile("001-001-successfulTest-custom.html").exists()
    }

    @Test
    void reportsAreWrittenOnFailure() {
        runAndFail("""
            @Test
            void failingTest() {
                go()
                assert false
            }
        """)

        assert reportFile("001-001-failingTest-failure.html").exists()
    }

    File getReportDir() {
        new File(tempDir, "reports")
    }

    File reportFile(String name) {
        def reportGroupDir = new File(reportDir, REPORTING_SPEC_TEST)
        new File(reportGroupDir, name)
    }

    void runSuccessfully(String testCode) {
        TestRunner.runSuccessfully(wrapIntoTestClass(testCode))
    }

    void runAndFail(String testCode) {
        TestRunner.runAndFail(wrapIntoTestClass(testCode))
    }

    private String wrapIntoTestClass(String testCode) {
        """
            import geb.junit5.ConfigModifyingGebReportingTest
            import org.junit.jupiter.api.*

            class ${REPORTING_SPEC_TEST} extends ConfigModifyingGebReportingTest {

                @BeforeEach
                void setup() {
                    baseUrl = "${server.baseUrl}"
                    config.rawConfig.reportsDir = "${reportDir.absolutePath.replaceAll("\\\\", "\\\\\\\\")}"
                }

                ${testCode}

            }
        """
    }
}
