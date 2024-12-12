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

import geb.test.GebSpecWithCallbackServer
// tag::import[]
import org.openqa.selenium.Keys

// end::import[]

class BackspaceSpec extends GebSpecWithCallbackServer {

    def "can clear text input using backspace"() {
        given:
        html {
            form {
                input(type: "text", name: "language", value: "groovy")
            }
        }

        when:
        // tag::backspace[]
        $("form").language() << Keys.BACK_SPACE
        // end::backspace[]

        then:
        // tag::backspace[]
        assert $("form").language == "groov"
        // end::backspace[]
    }
}
