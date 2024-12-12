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

class RepeatingContentSpec extends GebSpecWithCallbackServer {

    def setup() {
        html """
            // tag::html[]
            <html>
                <table>
                    <tr>
                        <th>Product</th><th>Quantity</th><th>Price</th>
                    </tr>
                    <tr>
                        <td>The Book Of Geb</td><td>1</td><td>5.99</td>
                    </tr>
                    <tr>
                        <td>Geb Single-User License</td><td>1</td><td>99.99</td>
                    </tr>
                    <tr>
                        <td>Geb Multi-User License</td><td>1</td><td>199.99</td>
                    </tr>
                </table>
            </html>
            // end::html[]
        """
    }

    def "modeling repeating content"() {
        when:
        to CheckoutPage

        then:
        // tag::collection[]
        assert cartItems.every { it.price > 0.0 }
        // end::collection[]
        // tag::indexing[]
        assert cartItems[0].productName == "The Book Of Geb"
        assert cartItems[1..2]*.productName == ["Geb Single-User License", "Geb Multi-User License"]
        // end::indexing[]
    }

    def "modeling repeating content using parameterized module"() {
        when:
        to CheckoutPageWithParametrizedCart

        then:
        assert cartItems.every { it.price > 0.0 }
        assert cartItems[0].productName == "The Book Of Geb"
        assert cartItems[1..2]*.productName == ["Geb Single-User License", "Geb Multi-User License"]
    }
}

// tag::module[]
class CartRow extends Module {
    static content = {
        cell { $("td", it) }
        productName { cell(0).text() }
        quantity { cell(1).text().toInteger() }
        price { cell(2).text().toBigDecimal() }
    }
}
// end::module[]

// tag::page[]
class CheckoutPage extends Page {
    static content = {
        cartItems {
            $("table tr").tail().moduleList(CartRow) // tailing to skip the header row
        }
    }
}
// end::page[]

// tag::parameterized_module[]
class ParameterizedCartRow extends Module {
    static content = {
        cell { $("td", it) }
        productName { cell(nameIndex).text() }
        quantity { cell(quantityIndex).text().toInteger() }
        price { cell(priceIndex).text().toBigDecimal() }
    }

    def nameIndex
    def quantityIndex
    def priceIndex
}

class CheckoutPageWithParametrizedCart extends Page {
    static content = {
        cartItems {
            $("table tr").tail().moduleList {
                new ParameterizedCartRow(nameIndex: 0, quantityIndex: 1, priceIndex: 2)
            }
        }
    }
}
// end::parameterized_module[]