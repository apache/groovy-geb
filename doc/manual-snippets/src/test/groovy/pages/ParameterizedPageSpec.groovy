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

import javax.servlet.http.HttpServletRequest

class ParameterizedPageSpec extends DriveMethodSupportingSpecWithServer {

    def "using parametrized pages"() {
        given:
        server.html { HttpServletRequest request ->
            if (request.requestURI.endsWith("bookPage")) {
                h1 "The Book of Geb"
            } else {
                a(href: "bookPage", "The Book of Geb")
            }
        }

        expect:
        // tag::example[]
        Browser.drive {
            to BooksPage
            book("The Book of Geb").click()

            at(new BookPage(forBook: "The Book of Geb"))
        }
        // end::example[]
    }
}

// tag::pages[]
class BooksPage extends Page {
    static content = {
        book { bookTitle -> $("a", text: bookTitle) }
    }
}

class BookPage extends Page {
    static at = { forBook == bookTitle }

    static content = {
        bookTitle { $("h1").text() }
    }

    String forBook
}
// end::pages[]
