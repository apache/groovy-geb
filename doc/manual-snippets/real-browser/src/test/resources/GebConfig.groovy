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
import org.openqa.selenium.chrome.ChromeOptions
import org.testcontainers.Testcontainers
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.utility.ResourceReaper

reportsDir = "build/geb"

Testcontainers.exposeHostPorts(8080)

driver = {
    def container = new BrowserWebDriverContainer<>()
            .withCapabilities(new ChromeOptions())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)

    container.start()

    ResourceReaper.instance().registerContainerForCleanup(container.containerId, container.dockerImageName)

    container.webDriver
}

testHttpServerHost = "host.testcontainers.internal"

testHttpServerPortHandler = { int port -> Testcontainers.exposeHostPorts(port) }