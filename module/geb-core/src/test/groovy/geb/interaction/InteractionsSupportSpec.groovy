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
package geb.interaction

import geb.Module
import geb.Page
import geb.test.GebSpecWithCallbackServer
import geb.test.browsers.Chrome
import geb.test.browsers.Firefox
import geb.test.browsers.InternetExplorerAndEdge

@Chrome
@Firefox
@InternetExplorerAndEdge
class InteractionsSupportSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            body {
                input(id: 'first-input', value: '')
                input(id: 'second-input', value: '')
            }
        }
    }

    def "navigators are unpacked in interact block"() {
        when:
        interact {
            moveToElement $('#first-input')
            click()
            sendKeys 'GEB'
        }
        interact {
            moveToElement $('#second-input')
            click()
            sendKeys 'geb'
        }

        then:
        $('#first-input').value() == 'GEB'
        $('#second-input').value() == 'geb'
    }

    def "content items are unpacked in interact block"() {
        given:
        page InteractionPage

        when:
        interact {
            moveToElement interactions.first
            click()
            sendKeys 'GEB'
        }
        interact {
            moveToElement interactions.second
            click()
            sendKeys 'geb'
        }

        then:
        interactions.first == 'GEB'
        interactions.second == 'geb'
    }

    def "can use interaction blocks in page classes"() {
        given:
        page InteractionPage

        when:
        fillInputs()

        then:
        interactions.first == 'GEB'
        interactions.second == 'geb'
    }

    def "can use interaction blocks in module classes"() {
        given:
        page InteractionPage

        when:
        interactions.fillInputs()

        then:
        interactions.first == 'GEB'
        interactions.second == 'geb'
    }
}

class InteractionPage extends Page {
    static content = {
        interactions { module InteractionsModule }
    }

    void fillInputs() {
        interact {
            moveToElement interactions.first
            click()
            sendKeys 'GEB'
        }
        interact {
            moveToElement interactions.second
            click()
            sendKeys 'geb'
        }
    }
}

class InteractionsModule extends Module {
    static content = {
        first { $('#first-input') }
        second { $('#second-input') }
    }

    void fillInputs() {
        interact {
            moveToElement first
            delegate.click()
            sendKeys 'GEB'
        }
        interact {
            moveToElement second
            delegate.click()
            sendKeys 'geb'
        }
    }
}
