/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package geb.testcontainers

import geb.download.DownloadSupport
import geb.report.CompositeReporter
import geb.report.PageSourceReporter
import geb.report.Reporter
import geb.report.ScreenshotReporter
import geb.test.GebTestManager
import geb.transform.DynamicallyDispatchesToBrowser
import geb.testcontainers.support.ContainerGebFileInputSource
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.images.builder.Transferable
import spock.lang.Shared
import spock.lang.Specification

/**
 * A {@link geb.spock.GebSpec GebSpec} equivalent that leverages Testcontainers
 * to run the browser inside a container.
 *
 * <p>Prerequisites:
 * <ul>
 *   <li>
 *       A <a href="https://java.testcontainers.org/supported_docker_environment/">compatible container runtime</a>
 *       (e.g., Docker) must be available for Testcontainers to utilize.
 *   </li>
 * </ul>
 *
 * @see ContainerGebConfiguration for how to customize the container's connection information
 *
 * @author Søren Berg Glasius
 * @author Mattias Reichel
 * @author James Daugherty
 * @since 4.1
 */
@DynamicallyDispatchesToBrowser
abstract class ContainerGebSpec extends Specification implements ContainerGebConfiguration {

    @Shared
    WebDriverContainerHolder holder

    @Delegate(includes = ['getBrowser'])
    GebTestManager getTestManager() {
        holder?.testManager
    }

    /**
     * Access the container running the web-driver, for convenience to execInContainer, copyFileToContainer etc.
     */
    BrowserWebDriverContainer getContainer() {
        holder?.container
    }

    /**
     * Returns the download support configured for localhost-aware downloads from containers.
     */
    @Delegate(interfaces = false)
    DownloadSupport getDownloadSupport() {
        holder?.downloadSupport
    }

    /**
     * Reports the current state of the browser under the given label.
     */
    void report(String message = '') {
        testManager?.report(message)
    }

    /**
     * The reporter that Geb should use when reporting is enabled.
     */
    Reporter createReporter() {
        new CompositeReporter(new PageSourceReporter(), new ScreenshotReporter())
    }

    /**
     * Copies a file from the host to the container for assignment to a Geb FileInput module.
     * This method is useful when you need to upload a file to a form in a Geb test and will work cross-platform.
     *
     * @param hostPath relative path to the file on the host
     * @param containerPath absolute path to where to put the file in the container
     * @return the file object to assign to the FileInput module
     * @since 4.2
     */
    File createFileInputSource(String hostPath, String containerPath) {
        container.copyFileToContainer(Transferable.of(new File(hostPath).bytes), containerPath)
        new ContainerGebFileInputSource(containerPath)
    }
}
