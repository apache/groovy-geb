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

import geb.Page
import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.By

class CompositionSpec extends GebSpecWithCallbackServer {

    def setup() {
        responseHtml """
            <html>
                // tag::html[]
                    <p class="a">1</p>
                    <p class="b">2</p>
                    <p class="c">3</p>
                // end::html[]
            </html>
        """
        go()
    }

    def "dollar"() {
        expect:
        // tag::dollar[]
        assert $($("p.a"), $("p.b"))*.text() == ["1", "2"]
        // end::dollar[]
    }

    def "add"() {
        expect:
        // tag::add[]
        assert $("p.a").add("p.b").add(By.className("c"))*.text() == ["1", "2", "3"]
        // end::add[]
    }

    def "content"() {
        when:
        page CompositionSpecPage

        then:
        // tag::content[]
        assert $(pElement("a"), pElement("b"))*.text() == ["1", "2"]
        // end::content[]
    }
}

class CompositionSpecPage extends Page {
    // tag::content_definition[]
    static content = {
        pElement { pClass -> $('p', class: pClass) }
    }
    // end::content_definition[]
}