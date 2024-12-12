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
package browser

// tag::driver_imports[]
// tag::imports[]
import geb.Browser
// end::imports[]
import org.openqa.selenium.htmlunit.HtmlUnitDriver
// end::driver_imports[]
// tag::configuration_imports[]
import geb.ConfigurationLoader
// end::configuration_imports[]
import spock.lang.Specification

class BrowserCreationSpec extends Specification {

    def "creating browser instance"() {
        when:
        // tag::creation[]
        def browser = new Browser()
        // end::creation[]

        then:
        browser
    }

    def "assigning a driver in constructor"() {
        when:
        // tag::setting_driver_in_constructor[]
        def browser = new Browser(driver: new HtmlUnitDriver())
        // end::setting_driver_in_constructor[]

        then:
        browser
    }

    def "assigning a driver"() {
        when:
        // tag::setting_driver[]
        def browser = new Browser()
        browser.driver = new HtmlUnitDriver()
        // end::setting_driver[]

        then:
        browser
    }

    def "configuration construction"() {
        when:
        // tag::configuration_construction[]
        def loader = new ConfigurationLoader("a-custom-environment")
        def browser = new Browser(loader.conf)
        // end::configuration_construction[]

        then:
        browser
    }
}
