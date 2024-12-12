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
package geb.navigator

import geb.error.SingleElementNavigatorOnlyMethodException
import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class SingleElementNavigatorOnlyMethodsSpec extends GebSpecWithCallbackServer {

    @Unroll
    def "#methodName can only be called on single element navigators"() {
        given:
        html {
            div "first"
            div "second"
        }

        when:
        $("div")."$methodName"(*arguments)

        then:
        SingleElementNavigatorOnlyMethodException e = thrown()
        e.message.startsWith("Method ${description} can only be called on single element navigators but it was called on a navigator with size 2.")

        where:
        methodName      | arguments | description
        "hasClass"      | [""]      | "hasClass(java.lang.String)"
        "is"            | [""]      | "is(java.lang.String)"
        "isDisplayed"   | []        | "isDisplayed()"
        "tag"           | []        | "tag()"
        "text"          | []        | "text()"
        "getAttribute"  | [""]      | "getAttribute(java.lang.String)"
        "attr"          | [""]      | "getAttribute(java.lang.String)"
        "classes"       | []        | "classes()"
        "value"         | []        | "value()"
        "click"         | []        | "click()"
        "getHeight"     | []        | "getHeight()"
        "getWidth"      | []        | "getWidth()"
        "getX"          | []        | "getX()"
        "getY"          | []        | "getY()"
        "css"           | [""]      | "css(java.lang.String)"
        "isFocused"     | []        | "isFocused()"
        "singleElement" | []        | "singleElement()"
    }

}