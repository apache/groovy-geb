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

import geb.test.GebTestManager
import geb.test.ManagedGebTest
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

class GebExtension implements IGlobalExtension {

    @Override
    void start() {
    }

    @Override
    void stop() {
    }

    void visitSpec(SpecInfo spec) {
        if (ManagedGebTest.isAssignableFrom(spec.reflection)) {
            addOnFailureReporter(spec)
            addManagerCalls(spec)
        }
    }

    protected GebTestManager getManager(IMethodInvocation invocation) {
        def spec = invocation.instance as ManagedGebTest
        spec.testManager
    }

    private void addOnFailureReporter(SpecInfo spec) {
        (spec.allFeatures*.featureMethod + spec.allFixtureMethods)
                *.addInterceptor(new OnFailureReporter())
    }

    private void addManagerCalls(SpecInfo spec) {
        // do it in shared initializer interceptor instead of
        // specification interceptor, so that it is run already
        // before shared field initializers are executed
        spec.addSharedInitializerInterceptor { invocation ->
            getManager(invocation).beforeTestClass(invocation.spec.reflection)
            invocation.proceed()
        }

        spec.addInterceptor { invocation ->
            try {
                invocation.proceed()
            } finally {
                getManager(invocation).afterTestClass()
            }
        }

        spec.allFeatures*.addIterationInterceptor { invocation ->
            GebTestManager testManager = getManager(invocation)
            testManager.beforeTest(invocation.instance.getClass(), invocation.iteration.displayName)
            try {
                invocation.proceed()
            } finally {
                testManager.afterTest()
            }
        }
    }
}
