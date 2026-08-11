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
import groovy.transform.CompileStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@CompileStatic
class ReportingFailureWatcher extends TestWatcher {

    private final GebReportingTest test

    Browser browser

    ReportingFailureWatcher(GebReportingTest test) {
        this.test = test
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (browser?.config?.reportOnTestFailureOnly) {
            // TODO: The effectiveReportLabel method was removed from GebReportingTest in
            //  https://github.com/apache/groovy-geb/commit/9da4360db04f846558dd720e4cd878f5296eaf42#diff-9adad49cb4ab674064363bcce3d7a0bfb3777b98a6964b58932965c2b8842848
            // browser.report(test.effectiveReportLabel('failure'))
            browser.report(test.getClass().simpleName + "." + description.methodName + "-failure")
        }
    }
}