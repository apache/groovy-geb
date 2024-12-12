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
import geb.Page

class BaseAndContextSpec extends FormContentSpec {

    def "using module with dynamic base"() {
        expect:
        // tag::using_module_with_dynamic_base[]
        Browser.drive {
            to PageDefiningModuleWithBase
            form.button.click()
            // end::using_module_with_dynamic_base[]
            assert form.button.hasClass("clicked")
            // tag::using_module_with_dynamic_base[]
        }
        // end::using_module_with_dynamic_base[]
    }

    def "creating a module outside of content definition"() {
        expect:
        // tag::creating_module_inline[]
        Browser.drive {
            go "/"
            $("form").module(FormModule).button.click()
            // end::creating_module_inline[]
            assert $("input").hasClass("clicked")
            // tag::creating_module_inline[]
        }
        // end::creating_module_inline[]
    }

    def "using module with static base"() {
        expect:
        // tag::using_module_with_static_base[]
        Browser.drive {
            to PageUsingModuleWithBase
            form.button.click()
            // end::using_module_with_static_base[]
            assert form.button.hasClass("clicked")
            // tag::using_module_with_static_base[]
        }
        // end::using_module_with_static_base[]
    }
}

// tag::module_with_base_page[]
class PageDefiningModuleWithBase extends Page {
    static content = {
        form { $("form").module(FormModule) }
    }
}
// end::module_with_base_page[]

// tag::form_module_with_base[]
class FormModuleWithBase extends FormModule {
    static base = { $("form") }
}

class PageUsingModuleWithBase extends Page {
    static content = {
        form { module FormModuleWithBase }
    }
}
// end::form_module_with_base[]
