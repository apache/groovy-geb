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
package geb

import geb.report.CompositeReporter
import geb.report.Reporter
import geb.report.ReportingListener
import spock.lang.Specification

class ConfigurationSpec extends Specification {

    def "creates default reporter"() {
        when:
        def conf = conf()

        then:
        conf.reporter
        conf.reporter instanceof CompositeReporter
    }

    def "uses no listener by default"() {
        expect:
        conf().reportingListener == null
    }

    def "specified reporting listener is added to reporter"() {
        given:
        def listener = Mock(ReportingListener)
        def reporter = Mock(Reporter)
        def conf = conf(reportingListener: listener, reporter: reporter)

        when:
        conf.reporter

        then:
        1 * reporter.addListener(listener)
    }

    protected Configuration conf(Map<String, ?> props = [:]) {
        new Configuration(props)
    }

}
