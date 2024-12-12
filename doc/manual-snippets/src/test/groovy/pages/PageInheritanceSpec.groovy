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
package pages

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer
import geb.Page

class PageInheritanceSpec extends DriveMethodSupportingSpecWithServer {

    def "pages can be organized in an inheritance hierarchy"() {
        given:
        server.html {
            h1("Specialized page")
            div(class: "footer", "This is the footer")
        }

        expect:
        // tag::example[]
        Browser.drive {
            to SpecializedPage
            assert heading.text() == "Specialized page"
            assert footer.text() == "This is the footer"
        }
        // end::example[]
    }
}

// tag::pages[]
class BasePage extends Page {
    static content = {
        heading { $("h1") }
    }
}

class SpecializedPage extends BasePage {
    static content = {
        footer { $("div.footer") }
    }
}
// end::pages[]
