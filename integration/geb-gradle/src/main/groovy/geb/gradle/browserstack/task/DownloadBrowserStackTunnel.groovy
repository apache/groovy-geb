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
package geb.gradle.browserstack.task

import geb.gradle.cloud.task.DownloadExternalTunnel
import groovy.transform.InheritConstructors
import org.apache.tools.ant.taskdefs.condition.Os

@InheritConstructors(constructorAnnotations = true)
abstract class DownloadBrowserStackTunnel extends DownloadExternalTunnel {

    @Override
    protected String downloadUrl() {
        "https://www.browserstack.com/browserstack-local/BrowserStackLocal-${osSpecificUrlPart()}.zip"
    }

    protected String defaultOutputPath() {
        "browserstack/BrowserStackTunnel.zip"
    }

    private String osSpecificUrlPart() {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "win32"
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            "darwin-x64"
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            (Os.isArch("amd64") || Os.isArch("x86_64")) ? "linux-x64" : "linux-ia32"
        }
    }
}
