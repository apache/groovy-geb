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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import geb.driver.CachingDriverFactory
import geb.test.CallbackHttpServer
import spock.lang.*
import spock.util.EmbeddedSpecRunner

import javax.servlet.http.HttpServletRequest

class ParallelExecutionSpec extends Specification {

    EmbeddedSpecRunner specRunner = new EmbeddedSpecRunner(
        throwFailure: false,
        configurationScript: {
            runner {
                parallel {
                    enabled true
                    // default Spock logic uses all except for 2 cores
                    // so on 4 or less cores the test would only use 2 threads
                    // which will not properly test thread-safety during parallel running
                    // as all iterations run on the same thread as their specification
                    if (Runtime.runtime.availableProcessors() <= 4) {
                        fixed(3)
                    }
                }
            }
        }
    )

    @TempDir
    File temporaryDir

    @Shared
    @AutoCleanup("stop")
    CallbackHttpServer server = new CallbackHttpServer()

    def setupSpec() {
        CachingDriverFactory.clearCacheAndQuitDriver()
        CachingDriverFactory.clearCacheCache()
    }

    def cleanupSpec() {
        CachingDriverFactory.clearCacheCache()
    }

    def setup() {
        server.start()
        server.html { HttpServletRequest request ->
            body {
                div "${request.requestURI.toURI().path}"
            }
        }

        specRunner.addClassImport(ConfigModifyingGebSpec)
        specRunner.addClassImport(ConfigModifyingGebReportingSpec)
        specRunner.addClassImport(Unroll)
        specRunner.addClassImport(CachingDriverFactory)
        specRunner.addClassImport(Shared)
        specRunner.addClassImport(CountDownLatch)
        specRunner.addClassImport(TimeUnit)
    }

    def 'GebSpec supports parallel execution at feature level'() {
        when:
        def result = specRunner.runWithImports """
            class SpecRunningIterationsInParallel extends ConfigModifyingGebSpec {

                @Shared
                def featureParallelismLatch = new CountDownLatch(2)

                def setup() {
                    baseUrl = "${server.baseUrl}"
                    config.cacheDriverPerThread = true
                    featureParallelismLatch.countDown()
                    // make sure tests are run in parallel
                    assert featureParallelismLatch.await(30, TimeUnit.SECONDS)
                }

                def cleanup() {
                    testManager.resetBrowser()
                    CachingDriverFactory.clearCacheAndQuitDriver()
                }

                @Unroll
                def 'feature running iterations in parallel'() {
                    when:
                    go path

                    then:
                    \$('div').text() == "/\${path}"

                    where:
                    path << ('a'..'d')
                }
            }
        """

        then:
        !result.failures*.exception
    }

    def 'GebReportingSpec supports parallel execution at feature level'() {
        when:
        def result = specRunner.runWithImports """
            abstract class AbstractSpecRunningIterationsInParallel extends ConfigModifyingGebReportingSpec {
                static specParallelismLatch = new CountDownLatch(2)
                @Shared
                def featureParallelismLatch = new CountDownLatch(2)

                def setupSpec() {
                    specParallelismLatch.countDown()
                    // make sure tests are run in parallel
                    assert specParallelismLatch.await(30, TimeUnit.SECONDS)

                    setupConfiguration()
                    go '/'
                    report('start spec')
                }

                def cleanupSpec() {
                    setupConfiguration()
                    go '/'
                    report('end spec')
                }

                def setup() {
                    featureParallelismLatch.countDown()
                    // make sure tests are run in parallel
                    assert featureParallelismLatch.await(30, TimeUnit.SECONDS)

                    setupConfiguration()
                }

                def setupConfiguration() {
                    baseUrl = "${server.baseUrl}"
                    config.cacheDriverPerThread = true
                    config.rawConfig.reportsDir = "${reportDir.absolutePath.replaceAll("\\\\", "\\\\\\\\")}"
                }

                def cleanup() {
                    report("end")
                    testManager.resetBrowser()
                    CachingDriverFactory.clearCacheAndQuitDriver()
                }

                @Unroll
                def 'feature running iterations in parallel'() {
                    when:
                    go path

                    then:
                    \$('div').text() == "/\${path}"

                    where:
                    path << ('a'..'d')
                }
            }

            class SpecRunningIterationsInParallel1 extends AbstractSpecRunningIterationsInParallel {
            }

            class SpecRunningIterationsInParallel2 extends AbstractSpecRunningIterationsInParallel {
            }
        """

        then:
        !result.failures*.exception
        reportFileTestCounterPrefixes("SpecRunningIterationsInParallel1") == (["000"] * 2) + ('001'..'004')
        reportFileTestCounterPrefixes("SpecRunningIterationsInParallel2") == (["000"] * 2) + ('001'..'004')
    }

    private List<String> reportFileTestCounterPrefixes(String className) {
        def reportGroupDir = new File(reportDir, "apackage/${className}")
        reportGroupDir.listFiles().collect {
            it.name.tokenize("-").first()
        }.sort()
    }

    private File getReportDir() {
        new File(temporaryDir, "reports")
    }

}
