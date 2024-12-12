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
package navigator

import fixture.GebSpecWithServerUsingJavascript
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
// tag::import[]
import org.openqa.selenium.Keys

// tag::import[]

class InteractionsSpec extends GebSpecWithServerUsingJavascript {

    def setup() {
        html """
            <html>
                ${jquery()}
                ${javascript '''
                    $(function() {
                        $("li").click(function(event) {
                            if (event.shiftKey) {
                                $(event.target).addClass("shift-clicked");
                            }
                        });
                    });
                '''}
                <ul>
                    <li class="clicky">shift click me!</li>
                </ul>
            </html>
        """
    }

    def "using WebDriver's Actions class directly"() {
        given:
        // tag::actions_creation[]
        def actions = new Actions(driver)
        // end::actions_creation[]

        when:
        // tag::action_definition[]
        WebElement someItem = $("li.clicky").firstElement()
        def shiftClick = actions.keyDown(Keys.SHIFT).click(someItem).keyUp(Keys.SHIFT).build()
        // end::action_definition[]

        and:
        // tag::action_execution[]
        shiftClick.perform()
        // end::action_execution[]

        then:
        $("li.clicky").hasClass("shift-clicked")
    }

    def "using interact closure"() {
        when:
        // tag::interact[]
        interact {
            keyDown Keys.SHIFT
            click $("li.clicky")
            keyUp Keys.SHIFT
        }
        // end::interact[]

        then:
        $("li.clicky").hasClass("shift-clicked")
    }

    def "accessing actions within interact closure"() {
        when:
        // tag::actions_in_interact[]
        interact {
            keyDown Keys.SHIFT
            actions.click($("li.clicky").singleElement())
            keyUp Keys.SHIFT
        }
        // end::actions_in_interact[]

        then:
        $("li.clicky").hasClass("shift-clicked")
    }
}
