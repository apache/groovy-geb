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
import geb.Module
import geb.Page

class IntroductionSpec extends FormContentSpec {

    def "using modules"() {
        expect:
        // tag::using_modules[]
        Browser.drive {
            to ModulePage
            form.button.click()
            // end::using_modules[]
            assert form.button.hasClass("clicked")
            // tag::using_modules[]
        }
        // end::using_modules[]
    }

    def "using parameterized modules"() {
        expect:
        // tag::using_parameterized_modules[]
        Browser.drive {
            to ParameterizedModulePage
            form("personal-data").button.click()
            // end::using_parameterized_modules[]
            assert form("personal-data").button.hasClass("clicked")
            // tag::using_parameterized_modules[]
        }
        // end::using_parameterized_modules[]
    }

    def "using nested modules"() {
        expect:
        // tag::using_nested_modules[]
        Browser.drive {
            to OuterModulePage
            outerModule.form.button.click()
            // end::using_nested_modules[]
            assert outerModule.form.button.hasClass("clicked")
            // tag::using_nested_modules[]
        }
        // end::using_nested_modules[]
    }
}

// tag::module_page[]
class ModulePage extends Page {
    static content = {
        form { module FormModule }
    }
}
// end::module_page[]

// tag::parameterized_module[]
class ParameterizedModule extends Module {
    static content = {
        button {
            $("form", id: formId).find("input", type: "button")
        }
    }
    String formId
}
// end::parameterized_module[]

// tag::parameterized_module_page[]
class ParameterizedModulePage extends Page {
    static content = {
        form { id -> module(new ParameterizedModule(formId: id)) }
    }
}
// end::parameterized_module_page[]

// tag::outer_module_and_page[]
class OuterModule extends Module {
    static content = {
        form { module FormModule }
    }
}

class OuterModulePage extends Page {
    static content = {
        outerModule { module OuterModule }
    }
}
// end::outer_module_and_page[]