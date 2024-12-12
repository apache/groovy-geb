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
package geb

import geb.error.IncorrectDriverTypeException
import geb.test.GebSpecWithCallbackServer
import geb.test.browsers.*

import java.time.Duration
import java.util.regex.Pattern

class BrowserNetworkLatencySpec extends GebSpecWithCallbackServer {

    @Firefox
    @RequiresRealBrowser
    def "setting network latency using a driver that does not implement or is augmentable to HasNetworkConditions"() {
        when:
        networkLatency = Duration.ofMillis(1)

        then:
        def e = thrown(IncorrectDriverTypeException)

        and:
        def message = "This operation is only possible on driver instances implementing or augmentable to " +
            "org.openqa.selenium.chromium.HasNetworkConditions"
        e.message =~ /${Pattern.quote(message)}/
    }

    @RequiresRealBrowser
    @Chrome
    @LocalChrome
    def "setting network latency on a supported browser"() {
        given:
        bodyWithJquery {
            button("Execute ajax request")
            script(type: "text/javascript") {
                mkp.yieldUnescaped '''
                    $("button").click(function() {
                        $.ajax({
                            success: function() {
                                $("body").append('<div class="ajax-completed">Completed</div>');
                            }
                        });
                    });
                '''
            }
        }

        and:
        // tag::setNetworkLatency[]
        def networkLatency = Duration.ofMillis(500)
        browser.networkLatency = networkLatency
        // end::setNetworkLatency[]

        when:
        def start = System.nanoTime()
        $("button").click()
        waitFor { $(".ajax-completed") }
        def duration = Duration.ofNanos(System.nanoTime() - start)

        then:
        duration >= networkLatency

        cleanup:
        browser.networkLatency = Duration.ZERO
    }
}
