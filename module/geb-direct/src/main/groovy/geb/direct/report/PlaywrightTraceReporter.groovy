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
package geb.direct.report

import geb.direct.PlaywrightWebDriver
import geb.report.ReportState
import geb.report.ReporterSupport
import java.nio.file.Path

/** Writes the active Playwright trace and immediately starts a fresh trace. */
class PlaywrightTraceReporter extends ReporterSupport {
    void writeReport(ReportState reportState) {
        def driver = reportState.browser.driver
        if (!(driver instanceof PlaywrightWebDriver) || !driver.session.options.tracing) {
            return
        }
        Path target = getFile(reportState.outputDir, reportState.label, 'zip').toPath()
        driver.tracing.stop(target)
        driver.tracing.start()
        notifyListeners(reportState, [target.toFile()])
    }
}
