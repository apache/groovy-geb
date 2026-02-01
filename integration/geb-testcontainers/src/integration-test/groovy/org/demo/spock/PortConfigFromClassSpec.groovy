/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.demo.spock

import grails.plugin.geb.ContainerGebSpec
import spock.lang.Narrative

/**
 * Test spec to verify that the correct port configuration is being used.
 * Some reference taken from:
 * groovy-geb/module/geb-core/src/test/groovy/geb/conf/ConfigurationLoaderSpec.groovy
 */
@Narrative("To verify that class field overrides config file.")
class PortConfigFromClassSpec extends ContainerGebSpec {

    static TestFileServer server

    int hostPort = 8000

    def setupSpec() {
        server = new TestFileServer()
    }

    def "should use the class field value"() {
        given: "a server listening on port 8000"
        server.start(8000)

        when: "go to localhost"
        go "/" // browser is going to 8080

        then: "the page title should be correct"
        title == "Hello Geb"

        and: "the welcome header should be displayed"
        $("h1").text() == "Welcome to the Geb/Spock Test"

    }

    def cleanup() {
        sleep(1000) // give the last video time to copy
        server.stop()
    }

}
