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

import geb.gradle.cloud.ExternalTunnel
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecOperations

import javax.inject.Inject

abstract class LambdaTestTunnelOps extends ExternalTunnel {

    public static final String TUNNEL_NAME_ENV_VAR = "GEB_LAMBDATEST_TUNNEL_NAME"

    private static final String COMMA = ","

    final String outputPrefix = 'lambdatest-tunnel'

    @Inject
    LambdaTestTunnelOps(ExecOperations execOperations, ProjectLayout projectLayout, ProviderFactory providerFactory) {
        super(execOperations, projectLayout)
        tunnelReadyMessage.convention('You can start testing now')
        tunnelName.convention("")
        infoAPIPort.convention(providerFactory.provider(new FreePortNumberProvider()))
    }

    @Internal
    abstract Property<String> getUsername()

    @Internal
    abstract Property<String> getAccessKey()

    @Internal
    abstract Property<String> getTunnelName()

    @Internal
    abstract ListProperty<String> getAllowHosts()

    @Internal
    abstract ListProperty<String> getBypassHosts()

    @Internal
    abstract Property<String> getCallbackURL()

    @Internal
    abstract Property<String> getConfig()

    @Internal
    abstract Property<String> getClientCert()

    @Internal
    abstract Property<String> getClientKey()

    @Internal
    abstract Property<String> getDir()

    @Internal
    abstract Property<String> getDns()

    @Internal
    abstract Property<Boolean> getEgressOnly()

    @Internal
    abstract Property<String> getEnv()

    @Internal
    abstract Property<String> getInfoAPIPort()

    @Internal
    abstract Property<Boolean> getIngressOnly()

    @Internal
    abstract Property<Boolean> getLoadBalanced()

    @Internal
    abstract Property<String> getLogFile()

    @Internal
    abstract Property<Boolean> getMitm()

    @Internal
    abstract Property<String> getMode()

    @Internal
    abstract ListProperty<String> getmTLSHosts()

    @Internal
    abstract ListProperty<String> getNoProxy()

    @Internal
    abstract Property<String> getPidfile()

    @Internal
    abstract Property<String> getPort()

    @Internal
    abstract Property<String> getProxyhost()

    @Internal
    abstract Property<String> getProxypass()

    @Internal
    abstract Property<String> getProxyport()

    @Internal
    abstract Property<String> getProxyuser()

    @Internal
    abstract Property<String> getPacfile()

    @Internal
    abstract Property<Boolean> getSharedTunnel()

    @Internal
    abstract Property<String> getSshConnType()

    @Internal
    abstract Property<Boolean> getVersion()

    @Override
    List<Object> assembleCommandLine() {
        def commandLine = [executablePath]
        commandLine << "--user" << username.get()
        commandLine << "--key" << accessKey.get()
        commandLine << "-v"
        commandLine << '--tunnelName' << tunnelName.get()
        if (allowHosts.orElse([])) {
            commandLine << '--allowHosts' << allowHosts.get().join(COMMA)
        }
        if (bypassHosts.orElse([])) {
            commandLine << '--bypassHosts' << bypassHosts.get().join(COMMA)
        }
        if (callbackURL.present) {
            commandLine << '--callbackURL' << callbackURL.get()
        }
        if (config.present) {
            commandLine << "--config" << config.get()
        }
        if (clientCert.present) {
            commandLine << "--clientCert" << clientCert.get()
        }
        if (clientKey.present) {
            commandLine << "--clientKey" << clientKey.get()
        }
        if (dir.present) {
            commandLine << "--dir" << dir.get()
        }
        if (dns.present) {
            commandLine << "--dns" << dns.get()
        }
        if (egressOnly.orElse(false)) {
            commandLine << "--egress-only"
        }
        if (env.present) {
            commandLine << "--env" << env.get()
        }
        commandLine << "--infoAPIPort" << infoAPIPort.get()
        if (ingressOnly.orElse(false)) {
            commandLine << "--ingress-only"
        }
        if (loadBalanced.orElse(false)) {
            commandLine << "--load-balanced"
        }
        if (logFile.present) {
            commandLine << "--logFile" << logFile.get()
        }
        if (mitm.getOrElse(false)) {
            commandLine << "--mitm"
        }
        if (mode.present) {
            commandLine << "--mode" << mode.get()
        }
        if (mTLSHosts.orElse([])) {
            commandLine << "--mTLSHosts" << mTLSHosts.get().join(COMMA)
        }
        if (noProxy.orElse([])) {
            commandLine << "--no-proxy" << noProxy.get().join(COMMA)
        }
        if (pidfile.present) {
            commandLine << "--pidfile" << pidfile.get()
        }
        if (port.present) {
            commandLine << "--port" << port.get()
        }
        if (proxyhost.present) {
            commandLine << "--proxy-host" << proxyhost.get()
        }
        if (proxypass.present) {
            commandLine << "--proxy-pass" << proxypass.get()
        }
        if (proxyport.present) {
            commandLine << "--proxy-port" << proxyport.get()
        }
        if (proxyuser.present) {
            commandLine << "--proxy-user" << proxyuser.get()
        }
        if (pacfile.present) {
            commandLine << "--pacfile" << pacfile.get()
        }
        if (sharedTunnel.orElse(false)) {
            commandLine << "--shared-tunnel"
        }
        if (sshConnType.present) {
            commandLine << "--sshConnType" << sshConnType.get()
        }
        if (version.getOrElse(false)) {
            commandLine << "--version"
        }

        commandLine.addAll(additionalOptions.get())
        commandLine
    }
}
