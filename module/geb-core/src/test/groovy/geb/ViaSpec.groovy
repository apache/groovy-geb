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
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

class ViaSpec extends GebSpecWithCallbackServer {
    def setupSpec() {
        responseHtml { HttpServletRequest request ->
            body {
                if (!request.parameterMap.hideA) {
                    div id: 'a'
                }
                if (request.parameterMap.showB) {
                    div id: 'b'
                }
            }
        }
    }

    def "verify our server is configured correctly"() {
        when:
        go '/'

        then:
        $('#a')
        !$('#b')

        when:
        go '/?hideA=true'

        then:
        !$('#a')
        !$('#b')
    }

    def "verify at checking works"() {
        when:
        via ViaSpecPageA
        and:
        at ViaSpecPageB

        then:
        PowerAssertionError error = thrown()
        error.message.contains('div')
    }

    def "successful at verification modifies browser's page instance"() {
        given:
        go ''

        when:
        at ViaSpecPageA

        then:
        page.getClass() == ViaSpecPageA
    }

    @Unroll
    def "verify isAt() works when using #scenario"() {
        when:
        via pageA

        then:
        isAt pageA
        !isAt(pageB)
        !isAt(pageB)

        where:
        scenario    | pageA              | pageB
        "classes"   | ViaSpecPageA       | ViaSpecPageB
        "instances" | new ViaSpecPageA() | new ViaSpecPageB()
    }

    def "when isAt() returns true it also modifies browser's page instance when using classes"() {
        given:
        go ''

        expect:
        isAt ViaSpecPageA
        page.getClass() == ViaSpecPageA

        and:
        !isAt(ViaSpecPageB)
        page.getClass() == ViaSpecPageA
    }

    def "when isAt() returns true it also modifies browser's page instance when using instances"() {
        given:
        def pageA = new ViaSpecPageA()
        def pageB = new ViaSpecPageB()
        go ''

        expect:
        isAt pageA
        page == pageA

        and:
        !isAt(pageB)
        page == pageA
    }

    @Unroll
    def "verify to() asserts that we are at the expected page - #scenario"() {
        when:
        to(*args)

        then:
        PowerAssertionError error = thrown()
        error.message.contains('div')

        where:
        scenario                                     | args
        'simple call using class'                    | [ViaSpecPageB]
        'simple call using instance'                 | [new ViaSpecPageB()]
        'call with map using class'                  | [[hideA: true], ViaSpecPageA]
        'call with map using instance'               | [[hideA: true], new ViaSpecPageA()]
        'call with parameter using class'            | [ViaSpecPageA, true]
        'call with parameter using instance'         | [new ViaSpecPageA(), true]
        'call with parameter and map using class'    | [[hideA: true], ViaSpecPageA, true]
        'call with parameter and map using instance' | [[hideA: true], new ViaSpecPageA(), true]
    }

    @Unroll
    def "verify to() succeeds when we are at the expected page - #scenario"() {
        when:
        def newPage = to(*args)

        then:
        notThrown(PowerAssertionError)
        newPage in args.find { it instanceof Class || it in Page }

        where:
        scenario                                     | args
        'simple call using class'                    | [ViaSpecPageA]
        'simple call using instance'                 | [new ViaSpecPageA()]
        'call with map using class'                  | [[showB: true], ViaSpecPageB]
        'call with map using instance'               | [[showB: true], new ViaSpecPageB()]
        'call with parameter using class'            | [ViaSpecPageB, true]
        'call with parameter using instance'         | [new ViaSpecPageB(), true]
        'call with parameter and map using class'    | [[showB: true], ViaSpecPageB, true]
        'call with parameter and map using instance' | [[showB: true], new ViaSpecPageB(), true]
    }

    @Unroll
    def "via() returns a page instance - #scenario"() {
        expect:
        via(*args) in args.find { it instanceof Class || it in Page }

        where:
        scenario                                     | args
        'simple call using class'                    | [ViaSpecPageA]
        'simple call using instance'                 | [new ViaSpecPageA()]
        'call with map using class'                  | [[showB: true], ViaSpecPageB]
        'call with map using instance'               | [[showB: true], new ViaSpecPageB()]
        'call with parameter using class'            | [ViaSpecPageB, true]
        'call with parameter using instance'         | [new ViaSpecPageB(), true]
        'call with parameter and map using class'    | [[showB: true], ViaSpecPageB, true]
        'call with parameter and map using instance' | [[showB: true], new ViaSpecPageB(), true]
    }

    def 'at() returns an instance of a page if it succeeds'() {
        when:
        via ViaSpecPageA

        then:
        at(ViaSpecPageA) in ViaSpecPageA
    }
}

class ViaSpecPageA extends Page {
    static at = { div }
    static content = {
        div(required: false) { $("#a") }
    }

    String convertToPath(param) {
        param ? "?hideA=$param" : ''
    }
}

class ViaSpecPageB extends Page {
    static at = { div }
    static content = {
        div(required: false) { $("#b") }
    }

    String convertToPath(param) {
        param ? "?showB=$param" : ''
    }
}

class ViaSpecPageC extends Page {
    static at = { false }
}
