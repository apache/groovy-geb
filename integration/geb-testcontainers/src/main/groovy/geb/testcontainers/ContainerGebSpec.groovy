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

import geb.Page
import geb.test.GebTestManager
import geb.testcontainers.support.ContainerSupport
import geb.testcontainers.support.ReportingSupport
import geb.testcontainers.support.delegate.BrowserDelegate
import geb.testcontainers.support.delegate.DownloadSupportDelegate
import geb.testcontainers.support.delegate.DriverDelegate
import geb.testcontainers.support.delegate.PageDelegate
import groovy.transform.CompileStatic
import spock.lang.Shared
import spock.lang.Specification

/**
 * A {@link geb.spock.GebSpec GebSpec} that leverages Testcontainers
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
@CompileStatic
abstract class ContainerGebSpec extends Specification implements ContainerSupport, ReportingSupport, BrowserDelegate, PageDelegate, DriverDelegate, DownloadSupportDelegate {

    @Shared
    static GebTestManager testManager

    static void setTestManager(GebTestManager testManager) {
        this.testManager = testManager
    }

    @Override
    Page getPage() {
        // Be explicit which trait to use (PageDelegate vs BrowserDelegate)
        PageDelegate.super.page
    }
}