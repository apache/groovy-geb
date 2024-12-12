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
package geb.conf

import geb.Browser
import geb.Configuration
import geb.error.InvalidGebConfiguration
import geb.navigator.Navigator
import geb.navigator.DefaultNavigator
import geb.navigator.factory.BrowserBackedNavigatorFactory
import geb.navigator.factory.DefaultInnerNavigatorFactory
import geb.navigator.factory.InnerNavigatorFactory
import geb.test.GebSpecWithCallbackServer
import groovy.transform.InheritConstructors
import org.openqa.selenium.WebElement

class ConfigurationNavigatorFactorySpec extends GebSpecWithCallbackServer {

    Configuration config
    ConfigObject raw

    def setup() {
        config = browser.config
        raw = config.rawConfig
    }

    def "creates navigator with default inner by default"() {
        expect:
        browser.navigatorFactory instanceof BrowserBackedNavigatorFactory
        browser.navigatorFactory.is(browser.navigatorFactory)
        config.innerNavigatorFactory instanceof DefaultInnerNavigatorFactory
    }

    def "can use closure based inner"() {
        when:
        raw.innerNavigatorFactory = { Browser browser, Iterable<WebElement> elements ->
            new ConfigurationNavigatorFactorySpecCustomNavigator(browser, elements)
        }
        go()

        then:
        $("p").class == ConfigurationNavigatorFactorySpecCustomNavigator
    }

    def "can use inner impl"() {
        when:
        def impl = new InnerNavigatorFactory() {
            @Override
            Navigator createNavigator(Browser browser, Iterable<WebElement> elements) {
                new ConfigurationNavigatorFactorySpecCustomNavigator(browser, elements)
            }
        }

        raw.innerNavigatorFactory = impl

        and:
        go()

        then:
        $("p").class == ConfigurationNavigatorFactorySpecCustomNavigator
    }

    def "error when invalid factory type"() {
        given:
        raw.navigatorFactory = 1

        when:
        browser.navigatorFactory

        then:
        thrown InvalidGebConfiguration
    }

    def "error when invalid factory closure return type"() {
        given:
        raw.navigatorFactory = { 1 }

        when:
        browser.navigatorFactory

        then:
        thrown InvalidGebConfiguration
    }

    def "error when inner factory is invalid"() {
        given:
        raw.innerNavigatorFactory = 1

        when:
        browser.navigatorFactory

        then:
        thrown InvalidGebConfiguration
    }

    def "can explicitly set"() {
        when:
        raw.innerNavigatorFactory = new InnerNavigatorFactory() {
            Navigator createNavigator(Browser browser, Iterable<WebElement> elements) {
                new ConfigurationNavigatorFactorySpecCustomNavigator(browser, elements)
            }
        }
        go()

        then:
        $("p").class == ConfigurationNavigatorFactorySpecCustomNavigator
    }

}

@InheritConstructors
class ConfigurationNavigatorFactorySpecCustomNavigator extends DefaultNavigator {
}
