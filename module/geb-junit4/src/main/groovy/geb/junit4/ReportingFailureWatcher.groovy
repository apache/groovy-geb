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
package geb.junit4

import geb.Browser
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ReportingFailureWatcher extends TestWatcher {

    private final GebReportingTest test

    Browser browser

    ReportingFailureWatcher(GebReportingTest test) {
        this.test = test
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (browser?.config?.reportOnTestFailureOnly) {
            browser.report(test.effectiveReportLabel('failure'))
        }
    }

}