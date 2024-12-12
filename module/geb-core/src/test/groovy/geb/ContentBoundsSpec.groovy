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

import geb.error.ContentCountOutOfBoundsException
import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class ContentBoundsSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            p "a paragraph"
        }
    }

    @Unroll("number of elements (#count) in the returned content does not match the specified bounds #templateOptions")
    def "number of elements in the returned content does not match the specified bounds"() {
        when:
        to(new ContentBoundsSpecPage(templateOptions: templateOptions, count: count)).elements

        then:
        ContentCountOutOfBoundsException e = thrown()
        e.message == "Page content 'geb.ContentBoundsSpecPage -> elements: geb.navigator.DefaultNavigator' should return a navigator with $boundsText but has returned a navigator with $actual"

        where:
        templateOptions | count | boundsText            | actual
        [max: 1]        | 2     | 'at most 1 element'   | '2 elements'
        [min: 2]        | 1     | 'at least 2 elements' | '1 element'
        [times: 1]      | 2     | 'at most 1 element'   | '2 elements'
        [times: 3]      | 2     | 'at least 3 elements' | '2 elements'
        [times: 3]      | 4     | 'at most 3 elements'  | '4 elements'
        [times: 3..5]   | 2     | 'at least 3 elements' | '2 elements'
        [times: 3..5]   | 6     | 'at most 5 elements'  | '6 elements'
        [times: 3..<5]  | 5     | 'at most 4 elements'  | '5 elements'
    }

    @Unroll
    def "min, max and times options can override default value of the required option"() {
        expect:
        to(new ContentBoundsSpecPage(templateOptions: templateOptions, count: 0)).elements.empty

        where:
        templateOptions << [
                [max: 0],
                [min: 0],
                [min: 0, max: 0],
                [times: 0]
        ]
    }

    def "required option can override default value of the min option"() {
        expect:
        to(new ContentBoundsSpecPage(templateOptions: [required: false], count: 0)).elements.empty
    }

}

class ContentBoundsSpecPage extends Page {
    static content = {
        elements(templateOptions) { count ? (['p'] * count).collect { $(it) }.sum() : $('#non-existing-element') }
    }

    Map<String, ?> templateOptions
    int count
}
