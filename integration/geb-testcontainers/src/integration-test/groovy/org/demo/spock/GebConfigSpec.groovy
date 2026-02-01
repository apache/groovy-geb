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

package org.demo.spock

import org.demo.spock.pages.HomePage
import org.openqa.selenium.remote.RemoteWebDriver

/**
 * Test spec to verify that our custom GebConfig.groovy configurations are
 * being used instead of the default WebDriverContainerHolder configuration.
 */
class GebConfigSpec extends ContainerGebSpecWithServer {

    def "should use custom RemoteWebDriver from GebConfig.groovy"() {
        // reportInfo "the config file should use environment blocks with browser names"

        expect: 'the driver to be a RemoteWebDriver'
        driver instanceof RemoteWebDriver

        when: 'getting the capabilities of the driver'
        def capabilities = ((RemoteWebDriver) driver).capabilities

        then: 'our custom capability set in GebConfig is available'
        capabilities.getCapability('grails:gebConfigUsed') == true

        and: 'the driver should use the browser from GebConfig.groovy'
        capabilities.browserName == System.getProperty('geb.env')

        when: 'navigating to a page'
        to(HomePage)

        then: 'the session should be active'
        driver.sessionId != null
    }

    def "should use the hostPort in GebConfig.groovy"() {
        // reportInfo "the config file should contain a 'hostPort = 8090' setting"

        when: "go to localhost"
        to(HomePage)

        then: "the page title should be correct"
        title == "Hello Geb"

        and: "the welcome header should be displayed"
        $("h1").text() == "Welcome to the Geb/Spock Test"
    }


    def cleanup() {
        sleep(1000) // give the last video time to copy
    }

}
