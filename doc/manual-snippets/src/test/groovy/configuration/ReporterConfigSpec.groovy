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
package configuration

import geb.report.ReportState
import geb.report.Reporter
import geb.report.ReportingListener
import spock.lang.Specification

class ReporterConfigSpec extends Specification implements InlineConfigurationLoader {

    def "configuring custom reporter"() {
        when:
        configScript """
            import configuration.CustomReporter

            // tag::config[]
            reporter = new CustomReporter()
            // end::config[]
        """

        then:
        config.reporter instanceof CustomReporter
    }

    def "configuring reporter to also write frames source"() {
        when:
        configScript """
            // tag::frames_source_reporter_config[]
            import geb.report.*

            reporter = new CompositeReporter(new PageSourceReporter(), new ScreenshotReporter(), new FramesSourceReporter())
            // end::frames_source_reporter_config[]
        """

        then:
        noExceptionThrown()
    }

    def "configuring reporter to write reports for each open window"() {
        when:
        configScript """
            // tag::multi_window_reporter_config[]
            import geb.report.*

            reporter = new MultiWindowReporter(new CompositeReporter(new PageSourceReporter(), new ScreenshotReporter()))
            // end::multi_window_reporter_config[]
        """

        then:
        noExceptionThrown()
    }
}

class CustomReporter implements Reporter {

    @Override
    void writeReport(ReportState reportState) {
    }

    @Override
    void addListener(ReportingListener listener) {
    }
}