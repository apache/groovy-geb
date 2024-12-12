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

import org.openqa.selenium.WebDriver
import org.openqa.selenium.remote.DesiredCapabilities

class SauceLabsDriverFactory extends CloudDriverFactory {

    private final String host

    SauceLabsDriverFactory(String host = "ondemand.saucelabs.com") {
        this.host = host
    }

    WebDriver create(Map<String, Object> additionalCapabilities = [:]) {
        def specification = System.getProperty("geb.saucelabs.browser")
        def user = System.getenv("GEB_SAUCE_LABS_USER")
        def key = System.getenv("GEB_SAUCE_LABS_ACCESS_PASSWORD")
        create(specification, user, key, additionalCapabilities)
    }

    @Override
    String assembleProviderUrl(String username, String password) {
        "https://$host/wd/hub"
    }

    @Override
    protected void configureCapabilities(String username, String key, DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("sauce:options.username", username)
        desiredCapabilities.setCapability("sauce:options.accessKey", key)

        def tunnelId = System.getenv("GEB_SAUCE_LABS_TUNNEL_ID")
        if (tunnelId) {
            desiredCapabilities.setCapability("sauce:options.tunnel-identifier", tunnelId)
        }
    }
}
