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

import geb.test.GebSpecWithCallbackServer
import geb.waiting.WaitTimeoutException

class ContentWaitConditionSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        def jquery = getClass().getResource("/jquery-1.4.2.min.js")

        html """
            <html>
                <head>
                    <script type="text/javascript">
                        ${jquery.text}
                    </script>
                    <script type="text/javascript" charset="utf-8">
                    setTimeout(function() {
                        \$("div").show();
                    }, 300);
                    </script>
                </head>
                <body>
                    <div style="display: none">initially hidden</div>
                </body>
            </html>
        """
    }

    def "implicitly waits for waitCondition content option to be fulfilled if it's specified when at checking"() {
        when:
        to(ContentWaitConditionSpecPage)

        then:
        div.displayed
    }

    def "statements in the waitCondition closure are implicitly asserted"() {
        when:
        to(ContentWaitConditionSpecPage).failingDiv

        then:
        WaitTimeoutException exception = thrown()
        exception.cause.message.contains("it.focused")
    }

}

class ContentWaitConditionSpecPage extends Page {
    static content = {
        div(waitCondition: { it.displayed }) { $('div') }
        failingDiv(wait: 0.05, waitCondition: { it.focused }) { $('div') }
    }
}
