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

import geb.junit.GebTestUtil
import geb.test.GebTestManager
import org.junit.jupiter.api.extension.*

class GebTestManagerExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    @Override
    void beforeAll(ExtensionContext context) throws Exception {
        getTestManager(context).beforeTestClass(context.requiredTestClass)
    }

    @Override
    void afterAll(ExtensionContext context) throws Exception {
        getTestManager(context).afterTestClass()
    }

    @Override
    void beforeEach(ExtensionContext context) throws Exception {
        getTestManager(context).beforeTest(context.requiredTestClass, context.requiredTestMethod.name)
    }

    @Override
    void afterEach(ExtensionContext context) throws Exception {
        def testManager = getTestManager(context)

        if (testManager.reportingEnabled && context.executionException.present) {
            testManager.reportFailure()
        }

        testManager.afterTest()
    }

    private GebTestManager getTestManager(ExtensionContext context) {
        GebTestUtil.getTestManager(context.requiredTestClass)
    }
}
