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
package geb.waiting

import geb.Configuration
import geb.navigator.factory.BrowserBackedNavigatorFactory
import geb.test.GebSpecWithCallbackServer
import groovy.transform.InheritConstructors
import spock.lang.Unroll

@Unroll
class BaseNavigatorWaitingSpec extends GebSpecWithCallbackServer {

    Configuration config
    ConfigObject rawConfig

    def setup() {
        responseHtml """
            <html>
                <head>
                    <script type="text/javascript" charset="utf-8">
                        setTimeout(function() {
                            document.body.innerHTML = "<div></div>";
                        }, 100);
                    </script>
                </head>
                <body></body>
            </html>
        """

        config = browser.config
        config.setWaitPreset('forBaseNavigator', 1, 0.1)
        rawConfig = config.rawConfig
        rawConfig.navigatorFactory = {
            new TestBaseNavigatorNavigatorFactory(browser, browser.config.innerNavigatorFactory)
        }
    }

    void 'base navigator waiting can be configured via config file to wait with waitFor parameter: #waitFor'() {
        given:
        rawConfig.baseNavigatorWaiting = waitFor

        when:
        go()
        $()

        then:
        notThrown(NoSuchElementException)

        where:
        waitFor << [true, 1, 'forBaseNavigator', [1, 0.1]]
    }

    void 'base navigator waiting can be configured programmatically'() {
        given:
        config.baseNavigatorWaiting = true

        when:
        go()
        $()

        then:
        notThrown(NoSuchElementException)
    }
}

@InheritConstructors
class TestBaseNavigatorNavigatorFactory extends BrowserBackedNavigatorFactory {

    protected String getBaseXPathExpression() {
        "//div"
    }
}
