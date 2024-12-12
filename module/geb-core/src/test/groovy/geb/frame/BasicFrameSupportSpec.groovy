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
package geb.frame

import geb.test.browsers.CrossBrowser
import org.openqa.selenium.NoSuchFrameException
import spock.lang.Unroll

@CrossBrowser
class BasicFrameSupportSpec extends BaseFrameSupportSpec {

    @Unroll("expect withFrame to fail if called for a non existing frame '#frame'")
    def "expect withFrame to fail if called for a non existing frame"() {
        when:
        withFrame(frame) {
        }

        then:
        thrown(NoSuchFrameException)

        where:
        frame << ['frame', 'idontexist']
    }

    @Unroll
    def "expect withFrame to fail if called for a navigator that doesn't contain a frame"() {
        when:
        withFrame($(selector)) {
        }

        then:
        NoSuchFrameException e = thrown()
        e.message.startsWith(message)

        where:
        message                          | selector
        ''                               | 'span'
        'No elements for given content:' | 'foo'
    }

    def "expect withFrame to fail if called for an empty navigator"() {
        when:
        withFrame($('nonexistingelem')) {
        }

        then:
        thrown(NoSuchFrameException)
    }

    @Unroll("expect the closure argument passed to withFrame to be executed for '#frameid' as frame identifier")
    def "expect the closure argument passed to withFrame to be executed"() {
        given:
        go pagePath

        when:
        boolean called = false
        withFrame(frameid) {
            called = true
        }

        then:
        called

        where:
        pagePath | frameid
        "frames" | "header"
        "iframe" | "inline"
        "frames" | 0
    }

    @Unroll("withFrame changes focus to frame and returns closure return value for frame identifier '#frame'")
    def "withFrame changes focus to frame with given identifier and returns closure return value"() {
        given:
        go pagePath

        expect:
        getFrameText(frame) == text

        where:
        pagePath | frame    | text
        "frames" | 'header' | 'header'
        "frames" | 'footer' | 'footer'
        "iframe" | 'inline' | 'inline'
        "frames" | 0        | 'header'
        "frames" | 1        | 'footer'
        "iframe" | 0        | 'inline'
    }

    @Unroll("withFrame changes focus to frame and returns closure return value for selector '#selector'")
    def "withFrame changes focus to frame with given selector and returns closure return value"() {
        given:
        go pagePath

        expect:
        getFrameText($(selector)) == text

        where:
        pagePath | selector     | text
        "frames" | '#header-id' | 'header'
        "frames" | '#footer'    | 'footer'
        "iframe" | '#inline'    | 'inline'
    }
}
