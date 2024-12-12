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
package geb.window

import geb.test.browsers.Chrome
import geb.test.browsers.Edge
import geb.test.browsers.Firefox
import geb.test.browsers.InternetExplorerAndEdge
import geb.test.browsers.Safari
import org.openqa.selenium.NoSuchWindowException
import spock.lang.Unroll

@Chrome
@Firefox
@InternetExplorerAndEdge
@Edge
@Safari
class BasicWindowHandlingSpec extends BaseWindowHandlingSpec {

    def setup() {
        go MAIN_PAGE_URL
    }

    @Unroll
    def "withWindow changes focus to window with given name and returns closure return value"() {
        when:
        openWindow(index)

        then:
        withWindow(windowName(index)) { title } == windowTitle(index)

        where:
        index << [1, 2]
    }

    @Unroll
    def "ensure original context is preserved after a call to withWindow"() {
        given:
        openWindow(1)

        when:
        withWindow(specification) {
        }

        then:
        inContextOfMainWindow

        when:
        withWindow(specification) { throw new Exception() }

        then:
        thrown(Exception)
        inContextOfMainWindow

        where:
        specification << [windowName(1), { title == windowTitle(1) }]
    }

    @Unroll
    def "ensure exception is thrown for a non existing window passed to withWindow"() {
        when:
        withWindow(specification) {
        }

        then:
        thrown(NoSuchWindowException)

        where:
        specification << ['nonexisting', { false }]
    }

    @Unroll
    def "withWindow closes matching windows if 'close' option is passed"() {
        given:
        openWindow(1)

        when:
        withWindow(specification, close: true) {
        }

        then:
        availableWindows.size() == old(availableWindows.size() - 1)

        where:
        specification << [{ title == windowTitle(1) }, windowName(1)]
    }

    def "ensure original context is preserved after a call to withNewWindow"() {
        when:
        withNewWindow({ openWindow(1) }) {
        }

        then:
        inContextOfMainWindow

        when:
        withNewWindow({ openWindow(2) }) { throw new Exception() }

        then:
        thrown(Exception)
        inContextOfMainWindow
    }

    @Unroll
    def "ensure withNewWindow block closure called in the context of the newly opened window"() {
        expect:
        withNewWindow({ openWindow(windowNum) }) { title } == expectedTitle

        where:
        expectedTitle  | windowNum
        windowTitle(1) | 1
        windowTitle(2) | 2
    }

    def "withNewWindow closes the new window by default"() {
        when:
        withNewWindow({ openWindow(1) }) {
        }

        then:
        availableWindows.size() == old(availableWindows.size())
        inContextOfMainWindow
    }
}
