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

class LifecycleHooksSpec extends DriveMethodSupportingSpecWithServer {

    def "on load"() {
        expect:
        //tag::on_load[]
        Browser.drive {
            to FirstPage
            to SecondPage
            assert page.previousPageName == "FirstPage"
        }
        //end::on_load[]
    }

    def "on unload"() {
        expect:
        //tag::on_unload[]
        Browser.drive {
            def firstPage = to FirstPage
            to SecondPage
            assert firstPage.newPageName == "SecondPage"
        }
        //end::on_unload[]
    }
}

// tag::on_load_pages[]
// tag::on_unload_pages[]
class FirstPage extends Page {
    // end::on_load_pages[]
    String newPageName

    void onUnload(Page newPage) {
        newPageName = newPage.class.simpleName
    }
    // tag::on_load_pages[]
}

class SecondPage extends Page {
    // end::on_unload_pages[]
    String previousPageName

    void onLoad(Page previousPage) {
        previousPageName = previousPage.class.simpleName
    }
    // tag::on_unload_pages[]
}
// end::on_load_pages[]
// end::on_unload_pages[]
