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

class ModuleIsANavigatorSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html """
            // tag::html[]
            <html>
                <form method="post" action="login">
                    <input name="login" type="text"></input>
                    <input name="password" type="password"></input>
                    <input type="submit" value="Login"></input>
                </from>
            </html>
            // end::html[]
        """
    }

    def "calling navigator methods on module instances"() {
        expect:
        // tag::on_module[]
        Browser.drive {
            to LoginPage
            assert form.@method == "post"
            assert form.displayed
        }
        // end::on_module[]
    }

    def "calling navigator methods from within a module"() {
        expect:
        // tag::in_module[]
        Browser.drive {
            to LoginPage
            assert form.action == "login"
        }
        // end::in_module[]
    }
}

// tag::content[]
// tag::in_module_definition[]
class LoginFormModule extends Module {
    // end::in_module_definition[]
    static base = { $("form") }
    // end::content[]

    // tag::in_module_definition[]
    String getAction() {
        getAttribute("action")
    }
    // tag::content[]
}
// end::in_module_definition[]

class LoginPage extends Page {
    static content = {
        form { module LoginFormModule }
    }
}
// end::content[]
