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
package geb.window

import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.NoSuchWindowException

class WindowAndAlertSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml { request ->
            body {
                if (request.requestURI.endsWith("closeable.html")) {
                    input(type: "button", onclick: 'confirm("confirm"); window.close();', name: "close", value: "Close")
                } else {
                    input(type: "button", onclick: 'window.open("/closeable.html", "_blank");', name: "open", value: "Open")
                }
            }
        }
    }

    def "using withConfirm when accepting the dialog closes a window does not cause NoSuchWindowException"() {
        given:
        go "/"

        when:
        withNewWindow({ $(name: "open").click() }, close: false) {
            withConfirm(true) { $(name: "close").click() }
        }

        then:
        notThrown(NoSuchWindowException)

        and:
        availableWindows.size() == 1
    }
}
