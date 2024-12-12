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
package configuration

import geb.Browser
import geb.ConfigurationLoader
import geb.navigator.DefaultNavigator
import geb.test.GebSpecWithCallbackServer
import geb.test.GebTestManager
import groovy.transform.InheritConstructors

import java.util.function.Supplier

class NavigatorFactoryConfigSpec extends GebSpecWithCallbackServer {

    private static final Supplier<Browser> BROWSER_SUPPLIER = configToBrowserSupplier {
        InlineConfiguration.parseConfigScript("""
            import configuration.MyCustomNavigator
            // tag::config[]
            import geb.Browser
            import org.openqa.selenium.WebElement

            innerNavigatorFactory = { Browser browser, Iterable<WebElement> elements ->
                new MyCustomNavigator(browser, elements)
            }
            // end::config[]

            reportsDir = "${new ConfigurationLoader().conf.reportsDir.absolutePath.replaceAll("\\\\", "\\\\\\\\")}"
        """)
    }

    private static final GebTestManager TEST_MANAGER = managerBuilder()
            .withBrowserCreator(BROWSER_SUPPLIER)
            .build()

    @Override
    GebTestManager getTestManager() {
        TEST_MANAGER
    }

    def "specifying custom navigator implementation"() {
        when:
        html {
        }

        then:
        $() instanceof MyCustomNavigator
    }
}

@InheritConstructors
class MyCustomNavigator extends DefaultNavigator {
}
