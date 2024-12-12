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
// tag::import[]
import org.openqa.selenium.Keys

// end::import[]

class ControlClickSpec extends GebSpecWithServerUsingJavascript {

    def "control clicking"() {
        given:
        html """
            <html>
                ${jquery()}
                ${javascript '''
                    $(function() {
                        $("li").click(function(event) {
                            if (event.ctrlKey) {
                                $(event.target).addClass("ctrl-clicked");
                            }
                        });
                    });
                '''}
                <ul class="multiselect">
                    <li>Order 1</li>
                    <li>Order 2</li>
                    <li>Order 3</li>
                </ul>
            </html>
        """

        when:
        // tag::interact[]
        interact {
            keyDown(Keys.CONTROL)
            click($("ul.multiselect li", text: "Order 1"))
            click($("ul.multiselect li", text: "Order 2"))
            click($("ul.multiselect li", text: "Order 3"))
            keyUp(Keys.CONTROL)
        }
        // end::interact[]

        then:
        $("ul.multiselect li").every { it.hasClass("ctrl-clicked") }
    }
}
