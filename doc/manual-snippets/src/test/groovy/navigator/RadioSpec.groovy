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

class RadioSpec extends GebSpecWithCallbackServer {

    def "setting radios"() {
        given:
        html """
            <html>
                // tag::html[]
                <form>
                    <label for="site-current">Search this site</label>
                    <input type="radio" id="site-current" name="site" value="current">

                    <label>Search Google
                        <input type="radio" name="site" value="google">
                    </label>
                </form>
                // end::html[]
            </html>
        """

        when:
        // tag::by_value[]
        $("form").site = "current"
        // end::by_value[]

        then:
        // tag::by_value[]
        assert $("form").site == "current"
        // end::by_value[]

        when:
        // tag::by_value[]
        $("form").site = "google"
        // end::by_value[]

        then:
        // tag::by_value[]
        assert $("form").site == "google"
        // end::by_value[]

        when:
        // tag::by_label[]
        $("form").site = "Search this site"
        // end::by_label[]

        then:
        // tag::by_label[]
        assert $("form").site == "current"
        // end::by_label[]

        when:
        // tag::by_label[]
        $("form").site = "Search Google"
        // end::by_label[]

        then:
        // tag::by_label[]
        assert $("form").site == "google"
        // end::by_label[]
    }
}
