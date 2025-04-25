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
package geb.spock

import geb.report.ReportState
import geb.report.Reporter
import geb.report.ReportingListener
import geb.test.CallbackHttpServer
import org.opentest4j.TestSkippedException
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.*
import spock.util.EmbeddedSpecRunner
import spock.util.EmbeddedSpecRunner.SummarizedEngineExecutionResults

class GebReportingSpecSpec extends Specification {

    final static String REPORTING_SPEC_NAME = "GebReportingExtendingSpec"

    EmbeddedSpecRunner specRunner = new EmbeddedSpecRunner(throwFailure: false)

    @TempDir
    File temporaryDir

    @Shared
    @AutoCleanup("stop")
    def server = new CallbackHttpServer()

    def setupSpec() {
        server.start()
        server.html {
            body {
                div "Test page"
            }
        }
    }

    def setup() {
        specRunner.addClassImport(ConfigModifyingGebReportingSpec)
        specRunner.addClassImport(Unroll)
        specRunner.addClassImport(Reporter)
        specRunner.addClassImport(ReportingListener)
        specRunner.addClassImport(ReportState)
        specRunner.addClassImport(Shared)
        specRunner.addClassImport(PendingFeature)
        specRunner.addClassImport(TestSkippedException)
    }

    File getReportDir() {
        new File(temporaryDir, "reports")
    }

    File getReportGroupDir() {
        new File(reportDir, "apackage/${REPORTING_SPEC_NAME}")
    }

    File reportFile(String name) {
        new File(reportGroupDir, name)
    }

    def "report named after the test and which contains response text is made at the end of a test"() {
        when:
        runReportingSpec """
            def "a request is made"() {
                given:
                config.reportOnTestFailureOnly = false
                go "/"
            }
        """

        then:
        reportFile("001-001-a request is made-end.html").text.startsWith("<?xml")
    }

    def "report is written after each test if reporting on failure only is disabled"() {
        when:
        runReportingSpec "config.reportOnTestFailureOnly = false", """
            def "first test"() {
                given:
                go "/"
            }

            def "second test"() {
                given:
                go "/"
            }
        """

        then:
        reportFile("001-001-first test-end.html").exists()
        reportFile("002-001-second test-end.html").exists()
    }

    def "report is not written after a successful test when reporting on failure only is enabled"() {
        when:
        runReportingSpec """
            def "passing test"() {
                given:
                go "/"

                expect:
                true
            }
        """

        then:
        !reportGroupDir.listFiles()
    }

    def "report is written after a failing test when reporting on failure only is enabled"() {
        when:
        runReportingSpec """
            def "failing test"() {
                given:
                go "/"

                expect:
                false
            }
        """

        then:
        reportFile("001-001-failing test-failure.html").exists()
    }

    def "report is written after a failing unrolled test when reporting on failure only is enabled"() {
        when:
        runReportingSpec """
            @Unroll
            def "failing test"() {
                given:
                go "/"

                expect:
                false

                where:
                parameter << [0]
            }
        """

        then:
        reportFile("001-001-failing test _parameter_ 0_ _0_-failure.html").exists()
    }

    def "report is not written after a skipped or aborted test when reporting on failure only is enabled"() {
        when:
        def result = runReportingSpec """
            @PendingFeature
            def "aborted test"() {
                expect:
                false
            }

            def "skipped test"() {
                given:
                throw new TestSkippedException()

                expect:
                false
            }
        """

        then:
        !reportGroupDir.listFiles()
        result.testsStartedCount == 2
        result.testsSucceededCount == 0
    }

    def "failure when writing a report does not overwrite the original test failure"() {
        when:
        def result = runReportingSpec """
            def "failing test"() {
                given:
                config.reporter = new Reporter() {
                    void writeReport(ReportState reportState) {
                        throw new Exception("Foo")
                    }

                    void addListener(ReportingListener listener) {
                    }
                }

                go "/"

                expect:
                false
            }
        """

        then:
        result.failures.first().exception instanceof ConditionNotSatisfiedError
        result.failures.first().exception.suppressed*.message == ['Foo']
    }

    def "report called from fixture method should create report with default name"() {
        when:
        specRunner.runWithImports """
            class $REPORTING_SPEC_NAME extends ConfigModifyingGebReportingSpec {

                def setupSpec() {
                    ${configuration}
                    config.reportOnTestFailureOnly = false

                    go "/"
                    report('Report in setupSpec')
                }

                def "passing test"() {
                    expect:
                    true
                }
            }
        """

        then:
        reportFile("000-001-fixture-Report in setupSpec.html").text.startsWith("<?xml")
        reportFile("001-001-passing test-end.html").text.startsWith("<?xml")
    }

    def "report called from shared initializer should create report with default name"() {
        when:
        specRunner.runWithImports """
            class $REPORTING_SPEC_NAME extends ConfigModifyingGebReportingSpec {
                @Shared
                def sharedField = helper()

                def helper() {
                    ${configuration}
                    config.reportOnTestFailureOnly = false

                    go "/"
                    report('Report in shared field initializer')

                    "foo"
                }

                def "passing test"() {
                    expect:
                    true
                }
            }
        """

        then:
        reportFile("000-001-fixture-Report in shared field initializer.html").text.startsWith("<?xml")
        reportFile("001-001-passing test-end.html").text.startsWith("<?xml")
    }

    def "failures in setup methods are reported on"() {
        when:
        specRunner.runWithImports """
            class $REPORTING_SPEC_NAME extends ConfigModifyingGebReportingSpec {

                def setup() {
                    ${configuration}
                    go "/"
                    throw new Exception()
                }

                def "test with failure in setup"() {
                    expect:
                    true
                }
            }
        """

        then:
        reportFile("001-001-test with failure in setup-failure.html").exists()
    }

    def "failures in cleanup methods are reported on"() {
        when:
        runReportingSpec """
            def cleanup() {
                throw new Exception()
            }

            def "test with failure in cleanup"() {
                expect:
                browser.go "/"
            }
        """

        then:
        reportFile("001-001-test with failure in cleanup-failure.html").exists()
    }

    def "failures in setupSpec methods are reported on"() {
        when:
        specRunner.runWithImports """
            class $REPORTING_SPEC_NAME extends ConfigModifyingGebReportingSpec {

                def setupSpec() {
                    ${configuration}
                    go "/"
                    throw new Exception()
                }

                def "test with failure in setupSpec"() {
                    expect:
                    true
                }
            }
        """

        then:
        reportFile("000-001-fixture-failure.html").exists()
    }

    def "failures in cleanupSpec methods are reported on"() {
        when:
        runReportingSpec """
            def cleanupSpec() {
                ${configuration}
                go "/"
                throw new Exception()
            }

            def "test with failure in cleanupSpec"() {
                expect:
                true
            }
        """

        then:
        reportFile("000-001-fixture-failure.html").exists()
    }

    SummarizedEngineExecutionResults runReportingSpec(String additionalConfiguration = "", String body) {
        specRunner.runWithImports """
            class $REPORTING_SPEC_NAME extends ConfigModifyingGebReportingSpec {

                def setup() {
                    ${configuration}
                    ${additionalConfiguration}
                }

                $body
            }
        """
    }

    private String getConfiguration() {
        """
        baseUrl = "${server.baseUrl}"
        config.rawConfig.reportsDir = "${reportDir.absolutePath.replaceAll("\\\\", "\\\\\\\\")}"
        """
    }
}
