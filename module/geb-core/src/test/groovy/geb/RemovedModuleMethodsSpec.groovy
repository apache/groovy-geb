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
import spock.lang.Unroll

class RemovedModuleMethodsSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
        }
    }

    @Unroll
    def "calling the old, loosely typed, removed #methodName method throws method missing exception"() {
        given:
        to RemovedModuleMethodSpecPage

        when:
        page."$contentName"

        then:
        MissingMethodException exception = thrown()
        exception.method == methodName

        where:
        methodName   | contentName
        "module"     | "moduleWithBase"
        "module"     | "parameterisedModule"
        "module"     | "parameterisedModuleWithBase"
        "moduleList" | "unparameterisedModuleList"
        "moduleList" | "unparameterisedModuleListWithIndex"
        "moduleList" | "parameterisedModuleList"
        "moduleList" | "parameterisedModuleListWithIndex"
    }

}

class RemovedModuleMethodSpecPage extends Page {
    static content = {
        moduleWithBase { module Module, $() }
        parameterisedModule { module Module, param: "value" }
        parameterisedModuleWithBase { module Module, $(), param: "value" }
        unparameterisedModuleList { moduleList Module, $() }
        unparameterisedModuleListWithIndex { moduleList Module, $() }
        parameterisedModuleList { moduleList Module, $(), param: "value" }
        parameterisedModuleListWithIndex { moduleList Module, $(), param: "value" }
    }
}
