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
package geb.spock

import geb.Browser
import geb.test.CallbackHttpServer
import geb.test.browsers.Chrome
import geb.test.browsers.RequiresRealBrowser
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

@RequiresRealBrowser
@Chrome
class AutoClearWebStorageSpec extends Specification {

    EmbeddedSpecRunner specRunner = new EmbeddedSpecRunner(throwFailure: false)

    @Shared
    @AutoCleanup("stop")
    def server = new CallbackHttpServer()

    def setupSpec() {
        server.start()
    }

    def setup() {
        specRunner.addClassImport(ConfigModifyingGebSpec)
    }

    def "web storage is cleaned after the test if configured to do so"() {
        given:
        def browser = new Browser()

        when:
        specRunner.runWithImports """
            class AutoClearWebStorage extends ConfigModifyingGebSpec {
                def "web storage is modified during a test"() {
                    given:
                    baseUrl = "${server.baseUrl}"
                    config.autoClearWebStorage = true

                    when:
                    go()
                    localStorage["test"] = "test"
                    sessionStorage["test"] = "test"

                    then:
                    true
                }
            }
        """

        then:
        browser.localStorage.size() == 0
        browser.sessionStorage.size() == 0
    }

}
