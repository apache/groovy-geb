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
package geb.module

import geb.Module
import geb.error.InvalidModuleBaseException
import geb.test.GebSpecWithCallbackServer

abstract class InputBasedModuleSpec<T> extends GebSpecWithCallbackServer {

    abstract String getInputType()

    abstract String getOtherInputType()

    Class<? extends Module> getModuleType() {
        getClass().genericSuperclass.actualTypeArguments.first()
    }

    def "can base the module on input of the right type"() {
        given:
        html {
            input(type: inputType)
        }

        when:
        $("input").module(moduleType)

        then:
        noExceptionThrown()
    }

    def "can base the module on an empty navigator"() {
        given:
        html {
        }

        when:
        $("input").module(moduleType)

        then:
        noExceptionThrown()
    }

    def "creating the module for anything other than input results in an exception"() {
        given:
        html {
            div("div")
        }

        when:
        $("div").module(moduleType)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base element for ${moduleType.name} module was 'div' but only input is allowed as the base element."
    }

    def "creating the module for an input of type other than the expected type results in an exception"() {
        given:
        html {
            input(type: otherInputType)
        }

        when:
        $("input").module(moduleType)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base element for ${moduleType.name} module was an input of type '${otherInputType}' but only input of type ${inputType} is allowed as the base element."
    }

    def "creating the module with a base navigator containing more than one element results in error"() {
        given:
        html {
            input(type: inputType)
            input(type: inputType)
        }

        when:
        $("input").module(moduleType)

        then:
        InvalidModuleBaseException e = thrown()
        e.message == "Specified base navigator for ${moduleType.name} module has 2 elements but at most one element is allowed."
    }
}
