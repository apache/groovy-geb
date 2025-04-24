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
package geb.test

import geb.Browser
import geb.Configuration
import geb.ConfigurationLoader

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

import static geb.report.ReporterSupport.toTestReportLabel

/**
 * This implementation assumes that a thread is reserved for one test execution at least from
 * {@code beforeTest} until {@code afterTest}, and that {@code beforeTestClass} and {@code afterTestClass}
 * are called on the same thread, even if the thread executed some test inbetween.
 * There should also only be one instance for all tests of a class even if run in parallel.
 */
class GebTestManager {

    private final Map<Class<?>, AtomicInteger> testCounters = new ConcurrentHashMap<>()
    private final Map<Class<?>, Consumer<Browser>> browserConfigurers = new ConcurrentHashMap<>()

    private final Supplier<Browser> browserCreator
    private final Predicate<Class<?>> resetBrowserAfterEachTestPredicate
    final boolean reportingEnabled

    protected final ThreadLocal<Browser> browser = new ThreadLocal<>()
    private final ThreadLocal<Configuration> configuration = ThreadLocal.withInitial { new ConfigurationLoader().conf }
    private final ThreadLocal<Deque<Class<?>>> testClass = ThreadLocal.withInitial { new ArrayDeque() }
    private final ThreadLocal<Deque<Integer>> perTestReportCounter = ThreadLocal.withInitial { new ArrayDeque() }
    private final ThreadLocal<Deque<Integer>> testCounter = ThreadLocal.withInitial { new ArrayDeque() }
    private final ThreadLocal<String> currentTestName = new ThreadLocal<>()

    GebTestManager(
            Supplier<Browser> browserCreator, Predicate<Class<?>> resetBrowserAfterEachTestPredicate,
            boolean reportingEnabled
    ) {
        this.browserCreator = browserCreator ?: { new Browser(configuration.get()) } as Supplier<Browser>
        this.resetBrowserAfterEachTestPredicate = resetBrowserAfterEachTestPredicate
        this.reportingEnabled = reportingEnabled
    }

    Browser getBrowser() {
        if (browser.get() == null) {
            browser.set(createBrowser())
        }
        browser.get()
    }

    void report(String label = "") {
        if (!reportingEnabled) {
            throw new IllegalStateException("Reporting has not been enabled on this GebTestManager yet report() was called")
        }
        getBrowser().report(createReportLabel(label))
        perTestReportCounter.get().push(perTestReportCounter.get().pop() + 1)
    }

    void reportFailure() {
        report("failure")
    }

    void beforeTestClass(Class<?> testClass) {
        this.testClass.get().push(testClass)
        if (reportingEnabled) {
            getBrowser().reportGroup(testClass)
            getBrowser().cleanReportGroupDir()
            browserConfigurers.put(testClass, { Browser browser ->
                browser.reportGroup(testClass)
            } as Consumer<Browser>)
            testCounter.get().push(nextTestCounter(testClass))
            perTestReportCounter.get().push(1)
        }
    }

    void beforeTest(Class<?> testClass, String testName) {
        this.testClass.get().push(testClass)
        currentTestName.set(testName)
        if (reportingEnabled) {
            // it happens that beforeTestClass for test class A is run on a thread
            // and next beforeTest (and thus test execution) for tests from class B are run on that thread
            // as the browser is thread-local reused, we need to set the report group here or the report
            // eventually is written to the wrong report group which is confusing and makes some tests flaky
            getBrowser().reportGroup(testClass)
            testCounter.get().push(nextTestCounter(testClass))
            perTestReportCounter.get().push(1)
        }
    }

    void afterTest() {
        if (reportingEnabled) {
            if (browser.get() && !getBrowser().config.reportOnTestFailureOnly) {
                report("end")
            }
            perTestReportCounter.get().pop()
            testCounter.get().pop()
        }

        if (resetBrowserAfterEachTest) {
            resetBrowser()
        }
        currentTestName.remove()
        testClass.get().pop()
    }

    void afterTestClass() {
        if (reportingEnabled) {
            perTestReportCounter.get().pop()
            testCounter.get().pop()
            browserConfigurers.remove(currentTestClass)
        }

        if (!resetBrowserAfterEachTest) {
            resetBrowser()
        }

        testClass.get().pop()
    }

    String createReportLabel(String label) {
        def methodName = currentTestName.get() ?: 'fixture'
        toTestReportLabel(currentTestCounter, currentPerTestReportCounter, methodName, label)
    }

    void resetBrowser() {
        def config = browser.get()?.config
        if (config?.autoClearCookies) {
            getBrowser().clearCookiesQuietly()
        }
        if (config?.autoClearWebStorage) {
            getBrowser().clearWebStorageQuietly()
        }
        if (config?.quitDriverOnBrowserReset) {
            getBrowser().driver.quit()
        }
        browser.remove()
    }

    private int nextTestCounter(Class<?> testClass) {
        testCounters.putIfAbsent(testClass, new AtomicInteger(0))
        testCounters[testClass].getAndIncrement()
    }

    private Browser createBrowser() {
        def browser = browserCreator.get()
        currentTestClass?.with(browserConfigurers.&get)?.accept(browser)
        browser
    }

    private boolean getResetBrowserAfterEachTest() {
        resetBrowserAfterEachTestPredicate.test(currentTestClass)
    }

    private int getCurrentTestCounter() {
        testCounter.get().peek()
    }

    private int getCurrentPerTestReportCounter() {
        perTestReportCounter.get().peek()
    }

    private Class<?> getCurrentTestClass() {
        testClass.get().peek()
    }

}
