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
package geb.gradle.lambdatest

import geb.gradle.cloud.task.SingleFileCopy
import geb.gradle.cloud.task.StartExternalTunnel
import geb.gradle.lambdatest.task.DownloadLambdaTestTunnel
import geb.gradle.lambdatest.task.StopLambdaTestTunnel
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS

class LambdaTestPlugin implements Plugin<Project> {

    public static final String CLOSE_TUNNEL_TASK_NAME = 'closeLambdaTestTunnel'
    public static final String OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME = 'openLambdaTestTunnelInBackground'
    public static final String UNZIP_TUNNEL_TASK_NAME = 'unzipLambdaTestTunnel'
    public static final String DOWNLOAD_TUNNEL_TASK_NAME = 'downloadLambdaTestTunnel'

    @Override
    void apply(Project project) {
        def allLambdaTestTests = project.tasks.register("allLambdaTestTests")

        def closeTunnel = project.tasks.register(CLOSE_TUNNEL_TASK_NAME, StopLambdaTestTunnel)

        def openLambdaTestTunnelInBackground = project.tasks.register(
            OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME, StartExternalTunnel
        ) {
            inBackground = true
            finalizedBy closeTunnel
        }

        def openLambdaTestTunnel = project.tasks.register('openLambdaTestTunnel', StartExternalTunnel)

        def lambdaTestExtension = project.extensions.create(
            'lambdaTest', LambdaTestExtension, allLambdaTestTests, openLambdaTestTunnelInBackground, closeTunnel,
            "LambdaTest Test"
        )

        [openLambdaTestTunnel, openLambdaTestTunnelInBackground, closeTunnel]*.configure {
            tunnel = lambdaTestExtension.tunnelOps
        }

        def downloadLambdaTestTunnel = project.tasks.register(DOWNLOAD_TUNNEL_TASK_NAME, DownloadLambdaTestTunnel)

        def unzipLambdaTestTunnel = project.tasks.register(UNZIP_TUNNEL_TASK_NAME, SingleFileCopy) {
            source.from(project.zipTree(downloadLambdaTestTunnel.flatMap { it.tunnelZip }))

            def fileExtension = Os.isFamily(FAMILY_WINDOWS) ? ".exe" : ""
            outputFile.set(project.layout.buildDirectory.file("lambdatest/LambdaTestTunnel${fileExtension}"))
        }

        lambdaTestExtension.tunnelOps.executable.set(unzipLambdaTestTunnel.flatMap { it.outputFile })
    }
}
