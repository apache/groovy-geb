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

import geb.test.GebTestManager
import geb.test.ManagedGebTest
import org.testng.IClassListener
import org.testng.ITestClass
import org.testng.ITestContext
import org.testng.ITestListener
import org.testng.ITestResult

import java.util.function.Consumer

class GebTestListener implements IClassListener, ITestListener {

    @Override
    void onBeforeClass(ITestClass testClass) {
        withTestManager(testClass) {
            it.beforeTestClass(testClass.realClass)
        }
    }

    @Override
    void onAfterClass(ITestClass testClass) {
        withTestManager(testClass) {
            it.afterTestClass()
        }
    }

    @Override
    void onTestStart(ITestResult result) {
        withTestManager(result) {
            it.beforeTest(result.testClass.realClass, result.name)
        }
    }

    @Override
    void onTestSuccess(ITestResult result) {
        onTestEnd(result)
    }

    @Override
    void onTestFailure(ITestResult result) {
        withTestManager(result) {
            if (it.reportingEnabled) {
                it.reportFailure()
            }
            it.afterTest()
        }
    }

    @Override
    void onTestSkipped(ITestResult result) {
        onTestEnd(result)
    }

    @Override
    void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestEnd(result)
    }

    @Override
    void onStart(ITestContext context) {
    }

    @Override
    void onFinish(ITestContext context) {
    }

    private void onTestEnd(ITestResult result) {
        withTestManager(result) {
            it.afterTest()
        }
    }

    private void withTestManager(ITestResult result, Consumer<GebTestManager> managerHandler) {
        withTestManager(testManager(result), managerHandler)
    }

    private void withTestManager(ITestClass testClass, Consumer<GebTestManager> managerHandler) {
        withTestManager(testManager(testClass), managerHandler)
    }

    private void withTestManager(GebTestManager testManager, Consumer<GebTestManager> managerHandler) {
        if (testManager) {
            managerHandler.accept(testManager)
        }
    }

    private GebTestManager testManager(ITestResult result) {
        def instance = result.instance
        if (instance instanceof ManagedGebTest) {
            instance.testManager
        }
    }

    private GebTestManager testManager(ITestClass testClass) {
        def instances = testClass.getInstances(false)
        if (instances) {
            def instance = instances.first()
            if (instance instanceof ManagedGebTest) {
                instance.testManager
            }
        }
    }

}
