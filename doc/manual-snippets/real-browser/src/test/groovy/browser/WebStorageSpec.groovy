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
package browser

import fixture.Browser
import fixture.DriveMethodSupportingSpecWithServer

class WebStorageSpec extends DriveMethodSupportingSpecWithServer {

    def "reading and writing to local storage"() {
        expect:
        // tag::write_and_read[]
        Browser.drive {
            // end::write_and_read[]
            go()
            // tag::write_and_read[]
            localStorage["username"] = "Alice"

            assert localStorage["username"] == "Alice"
        }
        // end::write_and_read[]
    }
}
