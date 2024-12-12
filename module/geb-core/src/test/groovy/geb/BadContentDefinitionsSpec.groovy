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

import geb.error.InvalidPageContent
import geb.test.GebSpec
import spock.lang.Unroll

class BadContentDefinitionsSpec extends GebSpec {

    def "no args"() {
        when:
        page BadContentDefinitionsSpecNoArgs
        then:
        thrown(InvalidPageContent)
    }

    def "non map arg"() {
        when:
        page BadContentDefinitionsSpecNonMap
        then:
        thrown(InvalidPageContent)
    }

    def "non closure factory"() {
        when:
        page BadContentDefinitionsSpecNonClosureFactory
        then:
        thrown(InvalidPageContent)
    }

    def "non map & non closure factory"() {
        when:
        page BadContentDefinitionsSpecNonMapNonClosureFactory
        then:
        thrown(InvalidPageContent)
    }

    def "more than two args"() {
        when:
        page BadContentDefinitionsSpecThreeArgs
        then:
        thrown(InvalidPageContent)
    }

    def "unknown element aliased"() {
        when:
        page BadContentDefinitionsSpecUnknownElementAliased
        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template 'foo' of '${BadContentDefinitionsSpecUnknownElementAliased.name}' aliases an unknown element 'bar'"
    }

    @Unroll("#scenario")
    def "bad element count bounds definitions"() {
        when:
        page new BadContentDefinitionsSpecTemplateOptions(templateOptions: options)

        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template 'foo' of '${BadContentDefinitionsSpecTemplateOptions.name}' $message"

        where:
        scenario                                        | options                        | message
        'both times and max specified'                  | [max: 1, times: 2]             | '''contains both 'max' and 'times' options which cannot be used together'''
        'both times and min specified'                  | [min: 1, times: 2]             | '''contains both 'min' and 'times' options which cannot be used together'''
        'required false and positive min specified'     | [required: false, min: 1]      | '''contains conflicting bounds and 'required' options'''
        'required true and max 0 specified'             | [required: true, max: 0]       | '''contains conflicting bounds and 'required' options'''
        'required true and min 0 specified'             | [required: true, min: 0]       | '''contains conflicting bounds and 'required' options'''
        'required false and positive times'             | [required: false, times: 1]    | '''contains conflicting bounds and 'required' options'''
        'required false and positive times range'       | [required: false, times: 1..2] | '''contains conflicting bounds and 'required' options'''
        'required true and times range starting from 0' | [required: true, times: 0..1]  | '''contains conflicting bounds and 'required' options'''
        'inverted times'                                | [times: 2..1]                  | '''contains inverted 'times' option'''
        'inverted min and max'                          | [min: 2, max: 1]               | '''contains 'max' option that is lower than the 'min' option'''
        'min that is not a number'                      | [min: 'a']                     | '''contains 'min' option that is not a non-negative integer'''
        'min that is not a non-negative integer'        | [min: -1]                      | '''contains 'min' option that is not a non-negative integer'''
        'min that is not an integer'                    | [min: 1.5]                     | '''contains 'min' option that is not a non-negative integer'''
        'max that is not a number'                      | [max: 'a']                     | '''contains 'max' option that is not a non-negative integer'''
        'max that is not a non-negative integer'        | [max: -1]                      | '''contains 'max' option that is not a non-negative integer'''
        'max that is not an integerger'                 | [max: 1.5]                     | '''contains 'max' option that is not a non-negative integer'''
        'times that is not a number'                    | [times: 'a']                   | '''contains 'times' option that is not a non-negative integer or a range of non-negative integers'''
        'times that is not a non-negative integer'      | [times: -1]                    | '''contains 'times' option that is not a non-negative integer or a range of non-negative integers'''
        'times that is not an integer'                  | [times: 1.5]                   | '''contains 'times' option that is not a non-negative integer or a range of non-negative integers'''
        'times that is a range over negative numbers'   | [times: -2..2]                 | '''contains 'times' option that is not a non-negative integer or a range of non-negative integers'''
        'times that is a range over floats'             | [times: 1.5..10.5]             | '''contains 'times' option that is not a non-negative integer or a range of non-negative integers'''
        'times that is a range over chars'              | [times: 'a'..'f']              | '''contains 'times' option that is not a non-negative integer or a range of non-negative integers'''
    }

    def "waitCondition that is not a closure"() {
        when:
        page new BadContentDefinitionsSpecTemplateOptions(templateOptions: [waitCondition: 'not a closure'])

        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template 'foo' of '${BadContentDefinitionsSpecTemplateOptions.name}' contains 'waitCondition' option that is not a closure"
    }

}

class BadContentDefinitionsSpecNoArgs extends Page {
    static content = { foo() }
}

class BadContentDefinitionsSpecNonMap extends Page {
    static content = { foo(1) }
}

class BadContentDefinitionsSpecNonClosureFactory extends Page {
    static content = { foo([:], 1) }
}

class BadContentDefinitionsSpecNonMapNonClosureFactory extends Page {
    static content = { foo(1, 1) }
}

class BadContentDefinitionsSpecThreeArgs extends Page {
    static content = { foo(1, 1, 2) }
}

class BadContentDefinitionsSpecUnknownElementAliased extends Page {
    static content = { foo(aliases: 'bar') }
}

class BadContentDefinitionsSpecTemplateOptions extends Page {
    static content = { foo(templateOptions) { } }

    Map<String, ?> templateOptions
}
