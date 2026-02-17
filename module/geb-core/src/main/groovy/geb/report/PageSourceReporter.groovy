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

import geb.Browser
import groovy.transform.CompileStatic

/**
 * Writes the source content of the browser's current page as a html file.
 */
@CompileStatic
class PageSourceReporter extends ReporterSupport {

    static public final String NO_PAGE_SOURCE_SUBSTITUTE = "-- no page source --"

    @Override
    void writeReport(ReportState reportState) {
        def file = getReportFile(reportState)
        writePageSource(file, reportState.browser)
        notifyListeners(reportState, [file])
    }

    protected File getReportFile(ReportState reportState) {
        getFile(reportState.outputDir, reportState.label, getPageSourceFileExtension(reportState.browser))
    }

    protected void writePageSource(File file, Browser browser) {
        file.write(getPageSource(browser))
    }

    protected String getPageSource(Browser browser) {
        browser.driver.pageSource ?: NO_PAGE_SOURCE_SUBSTITUTE
    }

    /**
     * Here to allow smarter calculation of the extension if necessary
     */
    @SuppressWarnings("UnusedMethodParameter")
    protected String getPageSourceFileExtension(Browser browser) {
        "html"
    }

}