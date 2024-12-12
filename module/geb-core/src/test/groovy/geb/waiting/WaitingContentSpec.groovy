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
package geb.waiting

import geb.Page
import geb.error.RequiredPageContentNotPresent
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import spock.lang.Unroll

class WaitingContentSpec extends WaitingSpec {

    def params = [:]
    def factory = { $("div") }
    def showDelay = 0.1

    def "no wait"() {
        when:
        content

        then:
        thrown RequiredPageContentNotPresent
    }

    def "default wait"() {
        when:
        params = [wait: true]

        then:
        content.text() == "a"
    }

    def "wait timeout"() {
        given:
        showDelay = 10

        when:
        params = [wait: 0.05]

        and:
        content

        then:
        thrown WaitTimeoutException
    }

    def "custom retry interval"() {
        when:
        params = [wait: [5, 0.01]]

        then:
        content
    }

    def "wait preset"() {
        when:
        params = [wait: "somepreset"]

        then:
        content
    }

    @Unroll
    def "invalid wait values"() {
        when:
        params = [wait: value]

        and:
        content

        then:
        thrown IllegalArgumentException

        where:
        value << [[], [1], [1, 2, 3], ["asds", "asdas"]]
    }

    def "waiting for non content - fail"() {
        given:
        factory = { false }
        params = [wait: 0.1]

        when:
        content

        then:
        thrown WaitTimeoutException
    }

    def "waiting for non content - pass"() {
        when:
        def counter = 0
        factory = { counter++ > 3 }
        params = [wait: [1, 0.05]]

        then:
        content
    }

    @Unroll
    def "content with wait option set throws timeout exception with power assertion error in cause for '#contentName'"() {
        when:
        to StaticallySpecifiedContentPage
        page[contentName]

        then:
        WaitTimeoutException exception = thrown()
        exception.cause in PowerAssertionError
        exception.cause.message.contains('$("div")')

        where:
        contentName << ['waitContent', 'waitContentExplicitlyRequired']
    }

    def "content with wait option returns content"() {
        when:
        to StaticallySpecifiedContentPage
        js.showIn(0)

        then:
        waitContent.text() == "a"
    }

    protected getContent() {
        DynamicallySpecifiedContentPage.content = {
            delegate.div(params, factory)
        }

        go()
        page DynamicallySpecifiedContentPage
        js.showIn(showDelay)
        div
    }

}

class DynamicallySpecifiedContentPage extends Page {
    static content = null
}

class StaticallySpecifiedContentPage extends Page {
    static content = {
        waitContent(wait: 0.1) { $("div") }
        waitContentExplicitlyRequired(wait: 0.1, required: true) { $("div") }
    }
}
