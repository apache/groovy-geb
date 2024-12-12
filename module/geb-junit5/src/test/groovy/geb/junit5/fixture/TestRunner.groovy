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
package geb.junit5.fixture

import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory

class TestRunner {

    static TestExecutionResult runSuccessfully(String testCode) {
        TestExecutionResult listener = execute(testCode)

        assert listener.wasExecutionSuccessful()
        listener
    }

    static TestExecutionResult runAndFail(String testCode) {
        TestExecutionResult listener = execute(testCode)

        assert listener.oneFailureOccurred()
        listener
    }

    private static TestExecutionResult execute(String testCode) {
        def testClass = parseClass(testCode)

        def request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build()

        def listener = new TestExecutionResult()
        LauncherFactory.create().execute(request, listener)
        listener
    }

    private static Class<?> parseClass(String classCode) {
        def classLoader = new GroovyClassLoader(TestRunner.classLoader)
        classLoader.parseClass(classCode)
    }

}