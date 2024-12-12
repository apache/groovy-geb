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

import geb.gradle.cloud.task.StartExternalTunnel
import geb.gradle.cloud.task.StopExternalTunnel
import org.gradle.api.Plugin
import org.gradle.api.Project

class SaucePlugin implements Plugin<Project> {

    public static final String CLOSE_TUNNEL_TASK_NAME = 'closeSauceTunnel'
    public static final String OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME = 'openSauceTunnelInBackground'
    public static final String UNPACK_CONNECT_TASK_NAME = 'unpackSauceConnect'

    @Override
    void apply(Project project) {
        def allSauceLabsTests = project.tasks.register("allSauceLabsTests")

        def closeTunnel = project.tasks.register(CLOSE_TUNNEL_TASK_NAME, StopExternalTunnel)

        def openSauceTunnelInBackground = project.tasks.register(
            OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME, StartExternalTunnel
        ) {
            inBackground = true
            finalizedBy closeTunnel
        }

        def openSauceTunnel = project.tasks.register('openSauceTunnel', StartExternalTunnel)

        def sauceLabsExtension = project.extensions.create(
            'sauceLabs', SauceLabsExtension, allSauceLabsTests, openSauceTunnelInBackground, closeTunnel, "Sauce Test"
        )

        [openSauceTunnel, openSauceTunnelInBackground, closeTunnel]*.configure {
            tunnel = sauceLabsExtension.connect
        }

        def sauceConnectConfiguration = project.configurations.create('sauceConnect').defaultDependencies {
            def message = "sauceConnect configuration is empty, please add a " +
                "dependency on 'ci-sauce' artifact from 'com.saucelabs' group to it"
            throw new IllegalStateException(message)
        }

        def unpackSauceConnect = project.tasks.register(UNPACK_CONNECT_TASK_NAME, UnpackSauceConnect) {
            sauceConnect.from(sauceConnectConfiguration)
        }

        sauceLabsExtension.connect.executable.set(unpackSauceConnect.flatMap { it.outputFile })
    }
}
