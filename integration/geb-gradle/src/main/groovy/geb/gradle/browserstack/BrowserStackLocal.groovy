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

import geb.gradle.cloud.ExternalTunnel
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecOperations

import javax.inject.Inject

abstract class BrowserStackLocal extends ExternalTunnel {
    public static final String LOCAL_ID_ENV_VAR = "GEB_BROWSERSTACK_LOCALID"

    private static final String HTTPS_PROTOCOL = 'https'

    final String outputPrefix = 'browserstack-tunnel'

    @Inject
    BrowserStackLocal(ExecOperations execOperations, ProjectLayout projectLayout) {
        super(execOperations, projectLayout)
        tunnelReadyMessage.convention("You can now access your local server(s) in our remote browser")
        identifier.convention("")
    }

    @Internal
    abstract Property<String> getAccessKey()

    @Internal
    abstract Property<String> getIdentifier()

    @Internal
    abstract Property<Boolean> getForce()

    @Internal
    abstract Property<String> getProxyHost()

    @Internal
    abstract Property<String> getProxyPort()

    @Internal
    abstract Property<String> getProxyUser()

    @Internal
    abstract Property<String> getProxyPass()

    @Internal
    abstract ListProperty<URL> getApplicationUrls()

    @Override
    List<Object> assembleCommandLine() {
        def commandLine = [executablePath, accessKey.get(), '-localIdentifier', identifier.get()]
        if (applicationUrls.getOrElse([])) {
            commandLine << "-only" << assembleAppSpecifier()
        }
        if (proxyHost.present) {
            commandLine << "-proxyHost" << proxyHost.get()
        }
        if (proxyPort.present) {
            commandLine << "-proxyPort" << proxyPort.get()
        }
        if (proxyUser.present) {
            commandLine << "-proxyUser" << proxyUser.get()
        }
        if (proxyPass.present) {
            commandLine << "-proxyPass" << proxyPass.get()
        }
        if (force.getOrElse(false)) {
            commandLine << "-forcelocal"
        }
        commandLine.addAll(additionalOptions.get())
        commandLine
    }

    private String assembleAppSpecifier() {
        applicationUrls.get().collect {
            "${it.host},${determinePort(it)},${it.protocol == HTTPS_PROTOCOL ? '1' : '0'}"
        }.join(',')
    }

    protected int determinePort(URL url) {
        url.port > 0 ? url.port : (url.protocol == HTTPS_PROTOCOL ? 443 : 80)
    }
}
