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

import geb.Configuration
import geb.Page
import spock.lang.Unroll

import static java.lang.System.currentTimeMillis

class AtCheckWaitingSpec extends WaitingSpec {

    Configuration config
    ConfigObject rawConfig

    def setup() {
        config = browser.config
        rawConfig = config.rawConfig

        config.setWaitPreset('forAtCheck', 1, 0.1)
    }

    void 'by default at checking does not wait'() {
        given:
        via AtCheckWaitingSpecPage

        when:
        js.showIn(1)

        then:
        !isAt(AtCheckWaitingSpecPage)
    }

    @Unroll
    void 'at checking can be configured via config file to wait with waitFor parameter: #waitFor'() {
        given:
        rawConfig.atCheckWaiting = waitFor
        via AtCheckWaitingSpecPage

        when:
        js.showIn(0.1)

        then:
        at AtCheckWaitingSpecPage

        where:
        waitFor << [true, 1, 'forAtCheck']
    }

    void 'at checking can be configured programmatically to wait'() {
        given:
        config.atCheckWaiting = true
        via AtCheckWaitingSpecPage

        when:
        js.showIn(0.1)

        then:
        at AtCheckWaitingSpecPage
    }

    void 'if at checker fails with waiting enabled it should provide assertion verification output'() {
        given:
        rawConfig.atCheckWaiting = 0.1
        via AtCheckWaitingSpecPage

        when:
        at AtCheckWaitingSpecPage

        then:
        WaitTimeoutException e = thrown()
        e.cause.message.contains('$("div", text: "a")')
    }

    void 'isAt() does not fail when implicit at waiting is enabled and it times out'() {
        given:
        rawConfig.atCheckWaiting = 0.1

        when:
        via AtCheckWaitingSpecPage

        then:
        !isAt(AtCheckWaitingSpecPage)
    }

    void 'at check waiting can be configured at page level'() {
        given:
        via AtCheckWaitingDefinedInPage

        when:
        js.showIn(0.1)

        then:
        isAt(AtCheckWaitingDefinedInPage)
    }

    void 'verify atCheckWaiting in page has priority over atCheckWaiting value in config'() {
        given:
        rawConfig.atCheckWaiting = 0.1
        via AtCheckWaitingDefinedInPage

        when:
        js.showIn(0.15)

        then:
        isAt(AtCheckWaitingDefinedInPage)
    }

    @Unroll
    void 'verify possible values for atCheckWaiting property in page level are consistent with the ones for wait option of content definitions - #pageClass.simpleName'() {
        given:
        via pageClass

        when:
        js.showIn(0.1)

        then:
        browser.page.verifyAtSafely()

        where:
        pageClass << [BooleanAtCheckWaitingSpecPage, NumberAtCheckWaitingSpecPage, ListOfNumbersAtCheckWaitingSpecPage]
    }

    void 'globally configured atCheckWaiting is used around checking of the list of pages and not around each one of them'() {
        given:
        def globalAtCheckWaiting = 0.5
        rawConfig.atCheckWaiting = globalAtCheckWaiting
        via AtCheckWaitingSpecPage

        when:
        js.showIn(0.1)
        def timestamp = currentTimeMillis()

        and:
        page(AlwaysFailingAtCheckWaitingSpecPage, AtCheckWaitingSpecPage)

        then:
        currentTimeMillis() - timestamp < globalAtCheckWaiting * 1000
    }
}

class AlwaysFailingAtCheckWaitingSpecPage extends Page {
    static at = { false }
}

class AtCheckWaitingSpecPage extends Page {
    static at = { $("div", text: "a") }
}

class AtCheckWaitingDefinedInPage extends AtCheckWaitingSpecPage {
    static atCheckWaiting = "forAtCheck"
}

class BooleanAtCheckWaitingSpecPage extends AtCheckWaitingSpecPage {
    static atCheckWaiting = true
}

class NumberAtCheckWaitingSpecPage extends AtCheckWaitingSpecPage {
    static atCheckWaiting = 0.5
}

class ListOfNumbersAtCheckWaitingSpecPage extends AtCheckWaitingSpecPage {
    static atCheckWaiting = [0.5, 0.2]
}