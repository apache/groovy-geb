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
package geb.transform.implicitassertions

import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

class SpockIntegrationSpec extends Specification {

    EmbeddedSpecRunner specRunner = new EmbeddedSpecRunner(throwFailure: false)

    def setup() {
        specRunner.addClassImport(Specification)
    }

    def "transform works in a spec feature method"() {
        when:
        def result = specRunner.runWithImports """
            class ExampleSpec extends Specification {

                def featureMethod() {
                    expect:
                    waitFor { 1 == 2 }
                }

                private waitFor(Closure c) {
                    c()
                }
            }
        """

        then:
        result.totalFailureCount == 1
        def failure = result.failures.first()
        PowerAssertionError error = failure.exception.cause
        error.message == """
            1 == 2
              |
              false
        """.readLines()[1..<-1].join("\n").stripIndent()
    }

    def "transform works in a spec helper method"() {
        when:
        def result = specRunner.runWithImports """
            class ExampleSpec extends Specification {

                def featureMethod() {
                    expect:
                    helperMethod()
                }

                def helperMethod() {
                    waitFor { 3 == 4 }
                }

                private waitFor(Closure c) {
                    c()
                }
            }
        """

        then:
        result.totalFailureCount == 1
        def failure = result.failures.first()
        PowerAssertionError error = failure.exception.cause
        error.message == """
            3 == 4
              |
              false
        """.readLines()[1..<-1].join("\n").stripIndent()
    }

    def "can have wait for methods with explicit asserts"() {
        when:
        def result = specRunner.runWithImports """
            class ExampleSpec extends Specification {

                def featureMethodWithAssertInWaitFor() {
                    expect:
                    waitFor { assert 1 == 1; true }
                }

                private waitFor(Closure c) {
                    try {
                        c()
                    } catch (Throwable e) {
                        throw new Exception("nested", e)
                    }
                }
            }
        """

        then:
        result.totalFailureCount == 0
    }
}

