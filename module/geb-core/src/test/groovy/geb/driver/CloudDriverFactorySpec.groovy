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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import ratpack.test.embed.EmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification

import static ratpack.jackson.Jackson.fromJson
import static ratpack.jackson.Jackson.json

class CloudDriverFactorySpec extends Specification {

    private static final String TEST_CAPABILITY_NAME = "geb:testCapability"

    @AutoCleanup
    EmbeddedApp mockWebDriverServer = EmbeddedApp.of {
        it.handlers {
            it.post("session") {
                it.render(
                        it.parse(fromJson(NewSessionCommand)).map { command ->
                            json(
                                    value: [
                                            sessionId: UUID.randomUUID(),
                                            capabilities: command.capabilities.firstMatch[0],
                                            status: 0
                                    ]
                            )
                        }
                )
            }
        }
    }

    def "additional capabilities passed to create() override capabilities set in configureCapability() method"() {
        given:
        def factory = new TestCloudDriverFactory(mockWebDriverServer.address.toString()[0..-2])
        def testCapabilityCustomValue = true

        when:
        def driver = factory.create("username", "key", [(TEST_CAPABILITY_NAME): testCapabilityCustomValue]) as RemoteWebDriver

        then:
        driver.capabilities.getCapability(TEST_CAPABILITY_NAME) == testCapabilityCustomValue
    }

    private static class TestCloudDriverFactory extends CloudDriverFactory {

        private final String url

        TestCloudDriverFactory(String url) {
            this.url = url
        }

        @Override
        String assembleProviderUrl(String username, String password) {
            url
        }

        @Override
        protected void configureCapabilities(String username, String key, DesiredCapabilities desiredCapabilities) {
            desiredCapabilities.setCapability(TEST_CAPABILITY_NAME, false)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NewSessionCommand {
        Map<String, Object> capabilities
    }

}
