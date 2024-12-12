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

class NonCharacterKeystrokesSpec extends GebSpecWithServerUsingJavascript {

    def "sending ctrl+c"() {
        given:
        html """
            <html>
                ${jquery()}
                ${javascript '''
                    $(function() {
                        var cKey = 67;
                        $("input").keydown(function(event) {
                            if (event.keyCode == cKey && event.ctrlKey) {
                                $(event.target).addClass("chord-recorded");
                            }
                        });
                    });
                '''}
                <input type="text"/>
            </html>
        """

        when:
        // tag::keystrokes[]
        $("input") << Keys.chord(Keys.CONTROL, "c")
        // end::keystrokes[]

        then:
        $("input").hasClass("chord-recorded")
    }
}
