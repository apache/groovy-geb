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
import geb.buildadapter.BuildAdapterFactory
import geb.driver.BrowserStackDriverFactory
import geb.driver.LambdaTestDriverFactory
import geb.driver.SauceLabsDriverFactory
import org.openqa.selenium.Capabilities
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.testcontainers.Testcontainers
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.shaded.org.apache.commons.io.FileUtils
import org.testcontainers.utility.ResourceReaper

import static org.testcontainers.containers.BrowserWebDriverContainer.getDockerImageForCapabilities

testValue = true // used in a test in geb-core

String getForkIndex(int total) {
    String workerName = System.getProperty('org.gradle.test.worker')
    int workerNumber = workerName ? ((workerName =~ /[^\d]*([\d]+)/)[0][1]).toInteger() : 0
    (workerNumber % total).toString()
}

void setPortIndexProperty(String index) {
    System.setProperty('geb.port.index', index)
}

BrowserWebDriverContainer containerForCapabilities(Capabilities capabilities) {
    new BrowserWebDriverContainer<>(getDockerImageForCapabilities(capabilities, "4.2.2-20220622"))
        .withCapabilities(capabilities)
}

BrowserWebDriverContainer containerForDriver(String driverName) {
    def container

    switch (driverName) {
        case "chrome":
            def options = new ChromeOptions()
            options.setCapability("se:cdpEnabled", false)
            container = containerForCapabilities(options)
            break
        case "firefox":
            def options = new FirefoxOptions()
            options.setCapability("se:cdpEnabled", false)
            container = containerForCapabilities(options)
            container.withSharedMemorySize(2 * FileUtils.ONE_GB)
            break
        default:
            throw new Exception("Unsupported dockerized driver: $driverName")
    }

    container.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            .start()

    ResourceReaper.instance().registerContainerForCleanup(container.containerId, container.dockerImageName)

    container
}

driver = "htmlunit"

if (!BuildAdapterFactory.getBuildAdapter(this.class.classLoader).reportsDir) {
    reportsDir = "build/geb"
}

def sauceLabsBrowser = System.getProperty("geb.saucelabs.browser")
if (sauceLabsBrowser) {
    setPortIndexProperty(getForkIndex(5))
    driver = {
        new SauceLabsDriverFactory().create()
    }
}

def browserStackBrowser = System.getProperty("geb.browserstack.browser")
if (browserStackBrowser) {
    setPortIndexProperty(getForkIndex(5))
    driver = {
        new BrowserStackDriverFactory().create()
    }
}

def lambdaTestBrowser = System.getProperty("geb.lambdatest.browser")
if (lambdaTestBrowser) {
    setPortIndexProperty(getForkIndex(5))
    driver = {
        new LambdaTestDriverFactory().create()
    }
}

def dockerizedDriver = System.getProperty("geb.dockerized.driver")
if (dockerizedDriver) {
    Testcontainers.exposeHostPorts(8080)

    driver = {
        containerForDriver(dockerizedDriver).webDriver
    }

    testHttpServerHost = "host.testcontainers.internal"

    testHttpServerPortHandler = { int port -> Testcontainers.exposeHostPorts(port) }
}

if (System.getProperty("geb.local.driver") == "chrome") {
    driver = {
        def chromeOptions = new ChromeOptions()
                .addArguments('headless')
                .addArguments('--remote-allow-origins=*') // TODO: Can be removed Selenium > 4.8.2
                .addArguments('--no-sandbox')
        new ChromeDriver(chromeOptions)
    }
}