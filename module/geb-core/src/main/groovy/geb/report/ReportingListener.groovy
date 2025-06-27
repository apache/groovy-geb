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
 * Gets notified whenever a report is taken.
 *
 * Reporting listeners are notified by reporters whenever reports are taken. This can be used to output links
 * to report files which is sometimes utilized by continuous integration servers.
 * <p>
 * They can be registered via the config mechanism...
 * <pre>
 * reportingListener = new ReportingListener() {
 *   void onReport(Reporter reporter, ReportState reportState, List<File> reportFiles) {
 *     reportFiles.each {
 *       println "Report taken: $it.absolutePath"
 *     }
 *   }
 * }
 * </pre>
 */
interface ReportingListener {

    /**
     * Called when a report is taken.
     *
     * @param reporter The reporter instance that created the report.
     * @param reportState Information about what was reported on.
     * @param reportFiles The report files that the reporter created.
     */
    void onReport(Reporter reporter, ReportState reportState, List<File> reportFiles)

}
