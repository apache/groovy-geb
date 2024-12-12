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

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier

class TestExecutionResult implements TestExecutionListener {

    private final List<TestIdentifier> skipped = []
    private final List<TestIdentifier> unsuccessful = []
    final List<Optional<Throwable>> throwables = []
    private final List<TestIdentifier> successful = []

    @Override
    void executionSkipped(TestIdentifier testIdentifier, String reason) {
        skipped << testIdentifier
    }

    @Override
    void executionFinished(TestIdentifier testIdentifier, org.junit.platform.engine.TestExecutionResult testExecutionResult) {
        if (testExecutionResult.status == org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL) {
            successful << testIdentifier
        } else {
            testExecutionResult.throwable.ifPresent {
                it.printStackTrace()
            }
            unsuccessful << testIdentifier
            throwables << testExecutionResult.throwable
        }
    }

    boolean wasExecutionSuccessful() {
        !skipped && !unsuccessful && successful
    }

    boolean oneFailureOccurred() {
        !skipped && unsuccessful.size() == 1
    }

    Throwable getSingleThrowable() {
        assert throwables.size() == 1
        throwables.first().get()
    }
}