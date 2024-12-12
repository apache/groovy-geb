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
package javascript

import geb.test.GebSpecWithCallbackServer

class ExecutingArbitraryCodeSpec extends GebSpecWithCallbackServer {

    def "single line"() {
        given:
        html { }

        expect:
        // tag::single_line[]
        assert js.exec(1, 2, "return arguments[0] + arguments[1];") == 3
        // end::single_line[]
    }

    def "multiline"() {
        given:
        html {
            script(type: "text/javascript", """
                function someJsMethod(a,b) {
                }
            """)
        }

        expect:
        // tag::multiline[]
        js.exec 1, 2, """
            someJsMethod(1, 2);
            // lots of javascript
            return true;
        """
        // end::multiline[]
    }
}
