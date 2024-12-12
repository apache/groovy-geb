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

import spock.lang.Specification

class WaitingConfigSpec extends Specification implements InlineConfigurationLoader {

    def "default configuration"() {
        when:
        configScript """
            // tag::default_config[]
            waiting {
                timeout = 10
                retryInterval = 0.5
            }
            // end::default_config[]
        """

        then:
        config.defaultWait.timeout == 10
        config.defaultWait.retryInterval == 0.5
    }

    def "presets"() {
        when:
        configScript """
            // tag::presets[]
            waiting {
                presets {
                    slow {
                        timeout = 20
                        retryInterval = 1
                    }
                    quick {
                        timeout = 1
                    }
                }
            }
            // end::presets[]
        """

        then:
        with(config.getWaitPreset("slow")) {
            timeout == 20
            retryInterval == 1
        }
        with(config.getWaitPreset("quick")) {
            timeout == 1
            retryInterval == 0.1
        }
    }

    def "including cause in exception message"() {
        when:
        configScript """
            // tag::include_cause[]
            waiting {
                includeCauseInMessage = true
            }
            // end::include_cause[]
        """

        then:
        config.includeCauseInWaitTimeoutExceptionMessage
    }
}
