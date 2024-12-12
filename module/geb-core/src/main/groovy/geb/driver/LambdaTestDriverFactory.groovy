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

import com.google.common.collect.ImmutableMap
import org.openqa.selenium.WebDriver
import org.openqa.selenium.remote.DesiredCapabilities

class LambdaTestDriverFactory extends CloudDriverFactory {

    public static final String LOCAL_IDENTIFIER_CAPABILITY = "LT:Options.tunnelName"

    @Override
    String assembleProviderUrl(String username, String password) {
        "https://$username:$password@hub.lambdatest.com/wd/hub"
    }

    WebDriver create(Map<String, Object> additionalCapabilities = [:]) {
        def specification = System.getProperty("geb.lambdatest.browser")
        def user = System.getenv("GEB_LAMBDATEST_USERNAME")
        def key = System.getenv("GEB_LAMBDATEST_AUTHKEY")
        create(specification, user, key, additionalCapabilities)
    }

    WebDriver create(String specification, String username, String password, String localId, Map<String, Object> capabilities = [:]) {
        def mergedCapabilities = ImmutableMap.builder().putAll(capabilities).put(LOCAL_IDENTIFIER_CAPABILITY, localId).build()
        create(specification, username, password, mergedCapabilities)
    }

    @Override
    protected void configureCapabilities(String username, String key, DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("LT:Options.tunnel", "true")
        def tunnelName = System.getenv("GEB_LAMBDATEST_TUNNEL_NAME")
        if (tunnelName) {
            desiredCapabilities.setCapability(LOCAL_IDENTIFIER_CAPABILITY, tunnelName)
        }
    }
}
