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
package geb

import geb.test.GebSpecWithCallbackServer

class ContentToStringSpec extends GebSpecWithCallbackServer {

    def "page content string representation"() {
        given:
        html {
            div {
                p 'text'
                a 'text'
            }
        }
        to PageContentSpecPage

        expect:
        div.a.toString() == 'geb.PageContentSpecPage -> div: geb.PageContentSpecModule -> a: geb.PageContentSpecInnerModule'
        div(1).a("foo").toString() == 'geb.PageContentSpecPage -> div(1): geb.PageContentSpecModule -> a(foo): geb.PageContentSpecInnerModule'
        divWithArgs("foo", "bar").toString() == 'geb.PageContentSpecPage -> divWithArgs(foo, bar): geb.PageContentSpecModule'
        div.p.toString() == 'geb.PageContentSpecPage -> div: geb.PageContentSpecModule -> p: geb.navigator.DefaultNavigator'
        div.toString() == 'geb.PageContentSpecPage -> div: geb.PageContentSpecModule'

        and:
        $('div').module(PageContentSpecModule).p.toString() == 'geb.PageContentSpecModule -> p: geb.navigator.DefaultNavigator'
        $('div').module(PageContentSpecModule).toString() == 'geb.PageContentSpecModule'
    }

}

class PageContentSpecPage extends Page {

    static content = {
        div { $('div').module(PageContentSpecModule) }
        divWithArgs { first, second -> $('div').module(PageContentSpecModule) }
    }

}

class PageContentSpecModule extends Module {

    static content  = {
        p { $('p') }
        a { $('a').module(PageContentSpecInnerModule) }
    }

}

class PageContentSpecInnerModule extends Module {
}
