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
package navigator

import geb.test.GebSpecWithCallbackServer

class SizeAndLocationSpec extends GebSpecWithCallbackServer {

    def setup() {
        html """
            <html>
                // tag::html[]
                <div style="height: 20px; width: 40px; position: absolute; left: 20px; top: 10px"></div>
                <div style="height: 40px; width: 100px; position: absolute; left: 30px; top: 150px"></div>
                // end::html[]
            </html>
        """
    }

    def "single element"() {
        expect:
        // tag::single_element[]
        assert $("div", 0).height == 20
        assert $("div", 0).width == 40
        assert $("div", 0).x == 20
        assert $("div", 0).y == 10
        // end::single_element[]
    }

    def "muliple elements"() {
        expect:
        // tag::multiple_elements[]
        assert $("div")*.height == [20, 40]
        assert $("div")*.width == [40, 100]
        assert $("div")*.x == [20, 30]
        assert $("div")*.y == [10, 150]
        // end::multiple_elements[]
    }
}
