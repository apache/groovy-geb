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
 * Writes the source content of each top level frame of the browser's current page as a html files.
 */
class FramesSourceReporter implements Reporter {

    @Delegate
    Reporter backing = new PageSourceReporter()

    @Override
    void writeReport(ReportState reportState) {
        def browser = reportState.browser
        def frames = browser.find('frame') + browser.find('iframe')
        (0..<frames.size()).each { index ->
            reportFrameSource(reportState, index)
        }
    }

    private File reportFrameSource(ReportState reportState, int index) {
        def driver = reportState.browser.driver
        try {
            driver.switchTo().frame(index)
            def frameLabel = "${reportState.label}-frame ${index + 1}"
            def frameState = new ReportState(reportState.browser, frameLabel, reportState.outputDir)
            backing.writeReport(frameState)
        } finally {
            driver.switchTo().defaultContent()
        }
    }

}
