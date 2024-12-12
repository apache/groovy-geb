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

import geb.Module
import geb.Page
import geb.test.GebSpecWithCallbackServer

class ReusingModulesSpec extends GebSpecWithCallbackServer {

    def "reusing module definitions"() {
        given:
        callbackServer.html {
            div(class: "cart-info") {
                span(class: "item-count", "4")
                span(class: "total-cost", "22.34")
            }
        }

        when:
        to HomePage

        then:
        cartInfo.itemCount == 4
        cartInfo.totalCost == 22.34

        when:
        to OtherPage

        then:
        cartInfo.itemCount == 4
        cartInfo.totalCost == 22.34
    }
}

// tag::content[]
class CartInfoModule extends Module {
    static content = {
        section { $("div.cart-info") }
        itemCount { section.find("span.item-count").text().toInteger() }
        totalCost { section.find("span.total-cost").text().toBigDecimal() }
    }
}

class HomePage extends Page {
    static content = {
        cartInfo { module CartInfoModule }
    }
}

class OtherPage extends Page {
    static content = {
        cartInfo { module CartInfoModule }
    }
}
// end::content[]