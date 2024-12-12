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
package geb.navigator

import geb.Browser
import geb.Page
import geb.test.GebSpecWithCallbackServer
import groovy.transform.InheritConstructors
import org.openqa.selenium.WebElement

class CustomNavigatorSpec extends GebSpecWithCallbackServer {

    def setup() {
        browser.config.rawConfig.innerNavigatorFactory = { Browser browser, Iterable<WebElement> elements ->
            new CustomNavigatorSpecCustomNavigator(browser, elements)
        }

        responseHtml {
            body {
                input type: 'text', value: 'some text'
            }
        }

        go()
    }

    def "can use not overridden methods from DefaultNavigator"() {
        given:
        def input = $('input')

        when:
        input.value('some other text')

        then:
        input.value() == 'some other text'
    }

    def "can use field access notation to access attributes"() {
        expect:
        $('input').@type == 'text'
    }

    def "can use custom navigator methods on content elements"() {
        when:
        to CustomNavigatorSpecPage

        then:
        input.typeAttribute == 'text'
    }
}

class CustomNavigatorSpecPage extends Page {
    static content = {
        input { $('input') }
    }
}

@InheritConstructors
class CustomNavigatorSpecCustomNavigator extends DefaultNavigator {

    String getTypeAttribute() {
        attr('type')
    }

}
