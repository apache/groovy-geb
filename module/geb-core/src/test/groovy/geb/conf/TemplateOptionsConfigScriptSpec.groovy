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
package geb.conf

import configuration.InlineConfigurationLoader
import geb.error.InvalidGebConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class TemplateOptionsConfigScriptSpec extends Specification implements InlineConfigurationLoader {

    def "invalid waitCondition default template option configuration value"() {
        given:
        configScript """
            templateOptions {
                waitCondition = "foo"
            }
        """

        when:
        config.templateOptions

        then:
        InvalidGebConfiguration e = thrown()
        e.message == "Configuration for waitCondition template option should be a closure but found \"foo\""
    }

    @Unroll("invalid #option default template option configuration value")
    def "invalid default template option configuration value"() {
        given:
        configScript """
            templateOptions {
                $option = $value
            }
        """

        when:
        config.templateOptions

        then:
        InvalidGebConfiguration e = thrown()
        e.message == "Configuration for $option template option should be a non-negative integer but found \"$errorValue\""

        where:
        option | value   | errorValue
        "min"  | "'foo'" | "foo"
        "min"  | -1      | -1
        "max"  | "'foo'" | "foo"
        "max"  | -1      | -1
    }

    @Unroll
    def "conflicting min and required template option configuration"() {
        given:
        configScript """
            templateOptions {
                min = $min
                required = $required
            }
        """

        when:
        config.templateOptions

        then:
        InvalidGebConfiguration e = thrown()
        e.message == "Configuration for bounds and 'required' template options is conflicting"

        where:
        min | required
        0   | true
        1   | false
    }

    @Unroll
    def "conflicting max and required template option configuration"() {
        given:
        configScript """
            templateOptions {
                max = 0
                required = true
            }
        """

        when:
        config.templateOptions

        then:
        InvalidGebConfiguration e = thrown()
        e.message == "Configuration for bounds and 'required' template options is conflicting"
    }

    @Unroll
    def "inverted min and max template option configuration"() {
        given:
        configScript """
            templateOptions {
                min = 1
                max = 0
            }
        """

        when:
        config.templateOptions

        then:
        InvalidGebConfiguration e = thrown()
        e.message == "Configuration contains 'max' template option that is lower than the 'min' template option"
    }

}
