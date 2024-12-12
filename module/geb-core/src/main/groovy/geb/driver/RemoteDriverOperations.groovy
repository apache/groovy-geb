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
package geb.driver

import geb.error.IncorrectDriverTypeException
import org.openqa.selenium.WebDriver

/**
 * Wraps the operations on remote drivers to avoid a hard dependency on selenium-remote-client.
 */
class RemoteDriverOperations {

    final ClassLoader classLoader

    RemoteDriverOperations(ClassLoader classLoader) {
        this.classLoader = classLoader
    }

    /**
     * If the driver is a remote driver, a proxy will be returned that implements the feature
     * interfaces of the actual driver on the remote side. If it is not, the passed in driver
     * is returned.
     */
    WebDriver getAugmentedDriver(WebDriver driver) {
        asRemoteWebDriver(driver).map { remoteWebDriverClass ->
            softLoadRemoteDriverClass("Augmenter").newInstance().augment(driver) as WebDriver
        }.orElse(driver)
    }

    Optional<Class<? extends WebDriver>> getOptionalRemoteWebDriverClass() {
        Optional.ofNullable(softLoadRemoteDriverClass("RemoteWebDriver")) as Optional<Class<? extends WebDriver>>
    }

    Class softLoadRemoteDriverClass(String name) {
        try {
            classLoader.loadClass("org.openqa.selenium.remote.$name")
        } catch (ClassNotFoundException e) {
            null
        }
    }

    Optional<WebDriver> asRemoteWebDriver(WebDriver driver) {
        optionalRemoteWebDriverClass.map { remoteWebDriverClass ->
            remoteWebDriverClass.isInstance(driver) ?
                driver :
                null as WebDriver
        }
    }

    Object executeCommand(WebDriver driver, String commandName, Map<String, ?> parameters = [:]) {
        def remoteWebDriver = asRemoteWebDriver(driver).orElseThrow {
            throw new IncorrectDriverTypeException(
                "This operation is only possible on instances of RemoteWebDriver but ${driver} was passed"
            )
        }

        def executor = remoteWebDriver.getCommandExecutor()
        def command = softLoadRemoteDriverClass("Command").newInstance(driver.getSessionId(), commandName, parameters)
        executor.execute(command).value
    }
}