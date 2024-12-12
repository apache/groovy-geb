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
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

class UnwrappingModulesSnippetSpec extends DriveMethodSupportingSpecWithServer {

    def setup() {
        server.html {
            div(class: "the-content", "content text")
        }
    }

    @SuppressWarnings('UnusedVariable')
    def "assignment of a module to a variable of its declared type fails"() {
        when:
        //tag::module_variable_fail[]
        Browser.drive {
            to ModuleUnwrappingPage
            UnwrappedModule foo = theModule   // <1>
        }
        //end::module_variable_fail[]
        then:
        thrown(GroovyCastException)
    }

    def "method invocation with an argument of module's declared type fails"() {
        when:
        //tag::module_argument_fail[]
        Browser.drive {
            to ModuleUnwrappingPage
            getContentText(theModule)   // <1>
        }
        //end::module_argument_fail[]
        then:
        thrown(MissingMethodException)
    }

    @SuppressWarnings('UnusedVariable')
    def "unwrapped module may be used with its declared type"() {
        when:
        //tag::module_cast[]
        Browser.drive {
            to ModuleUnwrappingPage
            UnwrappedModule unwrapped = theModule as UnwrappedModule
            getContentText(theModule as UnwrappedModule)
        }
        //end::module_cast[]
        then:
        noExceptionThrown()
    }

    //tag::module_argument_fail_method[]
    String getContentText(UnwrappedModule module) {
        module.theContent.text()
    }
    //end::module_argument_fail_method[]
}

// tag::page[]
class ModuleUnwrappingPage extends Page {
    static content = {
        theModule { module(UnwrappedModule) }
    }
}
// end::page[]

// tag::module[]
class UnwrappedModule extends Module {
    static content = {
        theContent { $(".the-content") }
    }
}
// end::module[]