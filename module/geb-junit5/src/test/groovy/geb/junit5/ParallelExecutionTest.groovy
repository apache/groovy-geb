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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore

import javax.servlet.http.HttpServletRequest

import geb.driver.CachingDriverFactory
import geb.junit5.fixture.CallbackServerExtension
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static java.util.concurrent.TimeUnit.SECONDS
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT

@Disabled("https://github.com/geb/geb/issues/188")
@Execution(CONCURRENT)
class ParallelExecutionTest extends ConfigModifyingGebTest {
    public static classExecutionExclusivityLock = new Semaphore(1)
    public static classOrderLatch = new CountDownLatch(1)

    private static testParallelismLatch = new CountDownLatch(2)

    @RegisterExtension
    public static CallbackServerExtension callbackServerExtension = new CallbackServerExtension(testManager)

    @BeforeAll
    static void setupClass() {
        classExecutionExclusivityLock.acquire()
        classOrderLatch.countDown()
        CachingDriverFactory.clearCacheAndQuitDriver()
        CachingDriverFactory.clearCacheCache()
        callbackServerExtension.server.html { HttpServletRequest request ->
            body {
                div "${request.requestURI.toURI().path}"
            }
        }
    }

    @AfterAll
    static void tearDownClass() {
        CachingDriverFactory.clearCacheCache()
        classExecutionExclusivityLock.release(2)
    }

    @BeforeEach
    void setup() {
        baseUrl = "${callbackServerExtension.server.baseUrl}"
        config.cacheDriverPerThread = true
        testParallelismLatch.countDown()
        // make sure tests are run in parallel
        assert testParallelismLatch.await(30, SECONDS)
    }

    @AfterEach
    void tearDown() {
        testManager.resetBrowser()
        CachingDriverFactory.clearCacheAndQuitDriver()
    }

    @ParameterizedTest
    @ValueSource(strings = ['a', 'b', 'c', 'd'])
    void testRunningIterationsInParallel(path) {
        go(path)
        assert $('div').text() == "/${path}"
    }
}

@Execution(CONCURRENT)
abstract class AbstractParallelExecutionWithReportingTest extends ConfigModifyingGebReportingTest {

    private static classParallelismLatch = new CountDownLatch(2)
    private static executionOfTestsCompleteLatch = new CountDownLatch(2)
    private static testParallelismLatches = new ConcurrentHashMap<Class<?>, CountDownLatch>()

    @BeforeAll
    static void setupClass() {
        ParallelExecutionTest.classOrderLatch.await()
        ParallelExecutionTest.classExecutionExclusivityLock.acquire()
        CachingDriverFactory.clearCacheAndQuitDriver()
        CachingDriverFactory.clearCacheCache()
        clazz.callbackServerExtension.server.html { HttpServletRequest request ->
            body {
                div "${request.requestURI.toURI().path}"
            }
        }
        classParallelismLatch.countDown()
        // make sure tests are run in parallel
        assert classParallelismLatch.await(30, SECONDS)
    }

    @AfterAll
    static void tearDownClass() {
        executionOfTestsCompleteLatch.countDown()
        // make sure the cleanup does not clear state
        // still used by the other class by synchronizing
        // again using a count down latch
        executionOfTestsCompleteLatch.await()
        CachingDriverFactory.clearCacheCache()
        assert reportFileTestCounterPrefixes(clazz) == '001'..'004'
    }

    @BeforeEach
    void setup() {
        def setupCountDownLatch = testParallelismLatches.computeIfAbsent(getClass()) { new CountDownLatch(2) }
        setupCountDownLatch.countDown()
        // make sure tests are run in parallel
        assert setupCountDownLatch.await(30, SECONDS)
        baseUrl = "${clazz.callbackServerExtension.server.baseUrl}"
        config.cacheDriverPerThread = true
        config.rawConfig.reportsDir = reportDir.absolutePath.replaceAll($/\\/$, $/\\\\/$)
    }

    @AfterEach
    void tearDown() {
        report('end')
        testManager.resetBrowser()
        CachingDriverFactory.clearCacheAndQuitDriver()
    }

    @ParameterizedTest
    @ValueSource(strings = ['a', 'b', 'c', 'd'])
    void testRunningIterationsInParallel(path) {
        go(path)
        assert $('div').text() == "/${path}"
    }

    private static List<String> reportFileTestCounterPrefixes(Class<?> clazz) {
        def reportGroupDir = new File(reportDir, clazz.name.replace('.', '/'))
        reportGroupDir.listFiles().collect {
            it.name.tokenize("-").first()
        }.sort()
    }

    private static File getReportDir() {
        new File(clazz.temporaryDir, "reports")
    }

    private static getClazz() {
        testManager.currentTestClass
    }
}

@Disabled("https://github.com/geb/geb/issues/188")
class ParallelExecutionWithReportingTest1 extends AbstractParallelExecutionWithReportingTest {
    @RegisterExtension
    public static CallbackServerExtension callbackServerExtension = new CallbackServerExtension(testManager)

    @TempDir
    public static File temporaryDir
}

@Disabled("https://github.com/geb/geb/issues/188")
class ParallelExecutionWithReportingTest2 extends AbstractParallelExecutionWithReportingTest {
    @RegisterExtension
    public static CallbackServerExtension callbackServerExtension = new CallbackServerExtension(testManager)

    @TempDir
    public static File temporaryDir
}
