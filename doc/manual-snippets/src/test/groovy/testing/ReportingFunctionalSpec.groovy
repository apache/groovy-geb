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
package testing

import geb.test.CallbackHttpServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.EmbeddedSpecRunner

class ReportingFunctionalSpec extends Specification {

    EmbeddedSpecRunner specRunner = new EmbeddedSpecRunner(throwFailure: false)

    @Shared
    @AutoCleanup("stop")
    def server = new CallbackHttpServer()

    @TempDir
    File temporaryDir

    def setupSpec() {
        server.start()
        server.html {
            head {
                title "Not logged in!"
            }
            body {
                input(type: "text", name: "username")
                input(type: "button", name: "login")
            }
        }
    }

    File getReportsDir() {
        new File(temporaryDir, "reports")
    }

    void reportExists(String filename) {
        assert new File(reportsDir, "apackage/LoginSpec/${filename}.html").exists()
    }

    def "reporting on a failing test which also writes a custom report"() {
        when:
        specRunner.runWithImports """
            //tag::example[]
            import geb.spock.GebReportingSpec
            import geb.spock.SpockGebTestManagerBuilder
            import geb.test.GebTestManager

            class LoginSpec extends GebReportingSpec {
            //end::example[]

                private final static GebTestManager TEST_MANAGER = new SpockGebTestManagerBuilder()
                    .withReportingEnabled(true)
                    .build()

                GebTestManager getTestManager() {
                    TEST_MANAGER
                }

                def setup() {
                    baseUrl = "${server.baseUrl}"
                    config.rawConfig.reportsDir = "${reportsDir.absolutePath.replaceAll("\\\\", "\\\\\\\\")}"
                }

                //tag::example[]
                def "login"() {
                    when:
                    go "/login"
                    username = "me"
                    report "login screen" //<1>
                    login().click()

                    then:
                    title == "Logged in!"
                }
            }
            //end::example[]
        """

        then:
        reportExists("001-001-login-login screen")
        reportExists("001-002-login-failure")
    }
}
