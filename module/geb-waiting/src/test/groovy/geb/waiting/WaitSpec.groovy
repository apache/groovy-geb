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

import geb.fixture.CrossPlatformSupport
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import spock.lang.Specification
import spock.lang.Timeout

/**
 * Tests the waiting algorithm, note that WaitingSupportSpec also tests Wait and covers many cases.
 */
@SuppressWarnings("TrailingWhitespace")
class WaitSpec extends Specification implements CrossPlatformSupport {

    @Timeout(5)
    def "wait algorithm handles cases where the block takes a long time"() {
        given:
        def wait = new Wait(0.4, 0.1)

        when:
        wait.waitFor { sleep 1000 }

        then:
        thrown WaitTimeoutException
    }

    def "waitFor block contents are implicitly asserted"() {
        given:
        def wait = new Wait(0.1)

        when:
        wait.waitFor { 'not empty'.empty }

        then:
        WaitTimeoutException exception = thrown()
        exception.cause in PowerAssertionError
        exception.cause.message.contains("'not empty'.empty")
    }

    def "waitFor timeout exception message does not contain cause by default"() {
        given:
        def wait = new Wait(0.15)

        when:
        wait.waitFor { 'not empty'.empty }

        then:
        WaitTimeoutException exception = thrown()
        exception.message == "condition did not pass in 0.15 seconds (failed with exception)"
    }

    def "waitFor timeout exception message contains cause when enabled"() {
        given:
        def wait = new Wait(0.15, Wait.DEFAULT_RETRY_INTERVAL, true)

        when:
        wait.waitFor { 'not empty'.empty }

        then:
        WaitTimeoutException exception = thrown()
        normalizeEndOfLines(exception.message) == """condition did not pass in 0.15 seconds. Failed with exception:
Assertion failed: 

'not empty'.empty
            |
            false
"""
    }
}