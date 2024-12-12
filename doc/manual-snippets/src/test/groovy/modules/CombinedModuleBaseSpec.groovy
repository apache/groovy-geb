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
package modules

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Module
import geb.Page

class CombinedModuleBaseSpec extends DriveMethodSupportingSpecWithServer {

    def "using modules with both dynamic and static base"() {
        given:
        server.html """
            // tag::html[]
            <html>
                <div class="a">
                    <form>
                        <input name="thing" value="a"/>
                    </form>
                </div>
                <div class="b">
                    <form>
                        <input name="thing" value="b"/>
                    </form>
                </div>
            </html>
            // end::html[]
        """

        expect:
        // tag::example[]
        Browser.drive {
            to ThingsPage
            assert formA.thingValue == "a"
            assert formB.thingValue == "b"
        }
        // end::example[]
    }
}

// tag::content[]
class ThingModule extends Module {
    static base = { $("form") }
    static content = {
        thingValue { thing().value() }
    }
}

class ThingsPage extends Page {
    static content = {
        formA { $("div.a").module(ThingModule) }
        formB { $("div.b").module(ThingModule)  }
    }
}
// end::content[]
