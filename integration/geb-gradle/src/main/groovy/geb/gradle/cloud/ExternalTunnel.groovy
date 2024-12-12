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
package geb.gradle.cloud

import groovy.util.logging.Slf4j
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecOperations

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Slf4j
abstract class ExternalTunnel {

    final protected ExecOperations execOperations
    final protected ProjectLayout projectLayout

    protected Process tunnelProcess

    ExternalTunnel(ExecOperations execOperations, ProjectLayout projectLayout) {
        this.execOperations = execOperations
        this.projectLayout = projectLayout
        timeout.convention(3)
        timeoutUnit.convention(TimeUnit.MINUTES)
        additionalOptions.convention([])
    }

    @InputFile
    abstract RegularFileProperty getExecutable()

    @Internal
    abstract Property<Integer> getTimeout()

    @Internal
    abstract Property<TimeUnit> getTimeoutUnit()

    @Internal
    abstract String getOutputPrefix()

    abstract List<Object> assembleCommandLine()

    @Internal
    abstract Property<String> getTunnelReadyMessage()

    @Internal
    abstract ListProperty<String> getAdditionalOptions()

    @Internal
    String getExecutablePath() {
        executable.asFile.get().absolutePath
    }

    void startTunnel(boolean background) {
        def command = assembleCommandLine()*.toString()
        log.debug("Executing command: {}", command)
        if (background) {
            tunnelProcess = new ProcessBuilder(command).
                redirectErrorStream(true).
                directory(projectLayout.buildDirectory.asFile.get()).
                start()

            def latch = new CountDownLatch(1)
            Thread.start {
                try {
                    tunnelProcess.inputStream.eachLine { String line ->
                        if (latch.count) {
                            log.info "$outputPrefix: $line"
                            if (line.contains(tunnelReadyMessage.get())) {
                                latch.countDown()
                            }
                        } else {
                            log.debug "$outputPrefix: $line"
                        }
                    }
                } catch (IOException ignore) {
                }
            }

            if (!latch.await(timeout.get(), timeoutUnit.get())) {
                throw new RuntimeException("Timeout waiting for tunnel to open")
            }
        } else {
            execOperations.exec {
                commandLine command
            }
        }
    }

    void stopTunnel() {
        if (tunnelProcess) {
            log.info "disconnecting tunnel"
            tunnelProcess.destroy()
        }
    }
}
