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

import org.openqa.selenium.Capabilities
import org.openqa.selenium.WebDriver
import org.openqa.selenium.remote.DesiredCapabilities

abstract class CloudDriverFactory {

    protected static final String VENDOR_PREFIX_DELIMETER = "."

    abstract String assembleProviderUrl(String username, String password)

    WebDriver create(String username, String key, Map<String, Object> capabilities) {
        create("", username, key, capabilities)
    }

    WebDriver create(String specification, String username, String key, Map<String, Object> additionalCapabilities = [:]) {
        def remoteDriverOperations = new RemoteDriverOperations(getClass().classLoader)
        Class<? extends WebDriver> remoteWebDriverClass = remoteDriverOperations
            .optionalRemoteWebDriverClass
            .orElseThrow {
                def message = 'org.openqa.selenium.remote.RemoteWebDriver needs to be on the classpath to create ' +
                    'RemoteWebDriver instances'
                throw new ClassNotFoundException(message)
            }

        def url = new URL(assembleProviderUrl(username, key))

        Properties capabilities = new Properties()
        if (specification) {
            capabilities.load(new StringReader(specification))
        }
        capabilities.putAll(additionalCapabilities)

        def desiredCapabilities = remoteDriverOperations.softLoadRemoteDriverClass('DesiredCapabilities').newInstance()
        configureCapabilities(username, key, desiredCapabilities)
        capabilities.each { capability, value ->
            desiredCapabilities.setCapability(capability, value)
        }

        remoteWebDriverClass.getConstructor(URL, Capabilities).newInstance(url, makeW3cCompliant(desiredCapabilities))
    }

    @SuppressWarnings("UnusedMethodParameter")
    protected void configureCapabilities(String username, String key, DesiredCapabilities desiredCapabilities) {
    }

    private DesiredCapabilities makeW3cCompliant(DesiredCapabilities source) {
        def sourceCapabilities = source.asMap()
        def compliantCapabilities = [:].withDefault { [:] } as Map<String, Object>

        sourceCapabilities.each { key, value ->
            if (key.contains(VENDOR_PREFIX_DELIMETER)) {
                def (prefix, newKey) = key.tokenize(VENDOR_PREFIX_DELIMETER)
                compliantCapabilities[prefix][newKey] = value
            } else {
                compliantCapabilities[key] = value
            }
        }

        new DesiredCapabilities(compliantCapabilities)
    }
}
