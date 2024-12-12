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

class TagTextClassesAttributesSpec extends GebSpecWithCallbackServer {
    def setup() {
        html """
            <html>
                // tag::html[]
                <p title="a" class="a para">a</p>
                <p title="b" class="b para">b</p>
                <p title="c" class="c para">c</p>
                // end::html[]
            </html>
        """
    }

    def "single element"() {
        expect:
        // tag::single_element[]
        assert $(".a").text() == "a"
        assert $(".a").tag() == "p"
        assert $(".a").@title == "a"
        assert $(".a").classes() == ["a", "para"]
        // end::single_element[]
    }

    def "muliple elements"() {
        expect:
        // tag::multiple_elements[]
        assert $("p")*.text() == ["a", "b", "c"]
        assert $("p")*.tag() == ["p", "p", "p"]
        assert $("p")*.@title == ["a", "b", "c"]
        assert $("p")*.classes() == [["a", "para"], ["b", "para"], ["c", "para"]]
        // end::multiple_elements[]
    }
}
