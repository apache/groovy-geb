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
package geb.gradle.browserstack

import geb.gradle.browserstack.task.DownloadBrowserStackTunnel
import geb.gradle.cloud.task.SingleFileCopy
import geb.gradle.cloud.task.StartExternalTunnel
import geb.gradle.cloud.task.StopExternalTunnel
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS

class BrowserStackPlugin implements Plugin<Project> {

    public static final String CLOSE_TUNNEL_TASK_NAME = 'closeBrowserStackTunnel'
    public static final String OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME = 'openBrowserStackTunnelInBackground'
    public static final String UNZIP_TUNNEL_TASK_NAME = 'unzipBrowserStackTunnel'

    @Override
    void apply(Project project) {
        def allBrowserStackTests = project.tasks.register("allBrowserStackTests")

        def closeTunnel = project.tasks.register(CLOSE_TUNNEL_TASK_NAME, StopExternalTunnel)

        def openBrowserStackTunnelInBackground = project.tasks.register(
            OPEN_TUNNEL_IN_BACKGROUND_TASK_NAME, StartExternalTunnel
        ) {
            inBackground = true
            finalizedBy closeTunnel
        }

        def openBrowserStackTunnel = project.tasks.register('openBrowserStackTunnel', StartExternalTunnel)

        def browserStackExtension = project.extensions.create(
            'browserStack', BrowserStackExtension, allBrowserStackTests, openBrowserStackTunnelInBackground,
            closeTunnel, "BrowserStack Test"
        )

        [openBrowserStackTunnel, openBrowserStackTunnelInBackground, closeTunnel]*.configure {
            tunnel = browserStackExtension.local
        }

        def downloadBrowserStackTunnel = project.tasks.register(
            'downloadBrowserStackTunnel', DownloadBrowserStackTunnel
        )

        def unzipBrowserStackTunnel = project.tasks.register(UNZIP_TUNNEL_TASK_NAME, SingleFileCopy) {
            source.from(project.zipTree(downloadBrowserStackTunnel.flatMap { it.tunnelZip }))

            def fileExtension = Os.isFamily(FAMILY_WINDOWS) ? ".exe" : ""
            outputFile.set(project.layout.buildDirectory.file("browserstack/BrowserStackTunnel${fileExtension}"))
        }

        browserStackExtension.local.executable.set(unzipBrowserStackTunnel.flatMap { it.outputFile })
    }
}
