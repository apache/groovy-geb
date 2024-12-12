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
package geb.gradle.saucelabs

import geb.gradle.PluginSpec

import static geb.gradle.saucelabs.SaucePlugin.*
import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE

class SaucePluginSpec extends PluginSpec {

    def "tunnel related tasks are skipped if tunnel is disabled"() {
        given:
        buildScript """
            plugins {
                id 'geb-saucelabs'
                id 'java'
            }

            sauceLabs {
                useTunnel = false
                browsers {
                    chromeLinux
                }
            }
        """

        when:
        def buildResult = runBuild("chromeLinuxTest")

        then:
        with(buildResult) {
            task(":chromeLinuxTest").outcome == NO_SOURCE
            !task(":$UNPACK_CONNECT_TASK_NAME")
            !task(":$OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME")
            !task(":$CLOSE_TUNNEL_TASK_NAME")
        }
    }

}
