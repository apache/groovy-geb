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
package geb.report

/**
 * Delegates to the backing reporter for each of the currently opened windows.
 */
class MultiWindowReporter implements Reporter {

    @Delegate
    Reporter backing

    MultiWindowReporter(Reporter backing) {
        this.backing = backing
    }

    @Override
    void writeReport(ReportState reportState) {
        def windows = reportState.browser.availableWindows
        if (windows.size() > 1) {
            windows.each {
                writeWindowReport(reportState, it)
            }
        } else {
            backing.writeReport(reportState)
        }
    }

    void writeWindowReport(ReportState reportState, String windowId) {
        reportState.browser.withWindow(windowId) {
            def windowLabel = "${reportState.label}-window $windowId"
            def windowState = new ReportState(reportState.browser, windowLabel, reportState.outputDir)
            backing.writeReport(windowState)
        }
    }

}
