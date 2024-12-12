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

import spock.lang.Specification
import spock.lang.TempDir

class ReporterSupportSpec extends Specification {

    @TempDir
    File reportDir

    def "report filename escaping"() {
        given:
        def reporter = new ReporterSupport() {
            void writeReport(ReportState reportState) {
                getFile(reportState.outputDir, reportState.label, "12 | 34 | zф") << "content"
            }
        }

        when:
        reporter.writeReport(new ReportState(null, "12 | 34 | zф", reportDir))

        then:
        new File(reportDir, "12 _ 34 _ zф.12 _ 34 _ zф").exists()
    }

    def "listener added more than once is not called twice"() {
        given:
        def l1 = Mock(ReportingListener)
        def l2 = Mock(ReportingListener)
        def f = new File("foo")
        def files = [f]
        def state = new ReportState(null, "foo", reportDir)
        def reporter = new ReporterSupport() {
            void writeReport(ReportState reportState) {
                notifyListeners(reportState, files)
            }
        }

        when:
        2.times { reporter.addListener(l1) }
        2.times { reporter.addListener(l2) }
        reporter.writeReport(state)

        then:
        1 * l1.onReport(reporter, state, files)
        1 * l2.onReport(reporter, state, files)
    }
}
