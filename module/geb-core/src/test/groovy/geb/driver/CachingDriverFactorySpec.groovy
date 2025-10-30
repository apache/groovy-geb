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
package geb.driver

import geb.test.GebSpec
import org.openqa.selenium.NoSuchSessionException
import org.openqa.selenium.WebDriver
import spock.lang.Issue
import spock.util.concurrent.BlockingVariable

class CachingDriverFactorySpec extends GebSpec {
    def "global driver and per-thread driver factories are independent"() {
        given:
        def stubFactory = Stub(DriverFactory)
        def globalFactory = CachingDriverFactory.global(stubFactory, false)
        def perThreadFactory = CachingDriverFactory.perThread(stubFactory, false)
        def globalDriver2 = new BlockingVariable<WebDriver>()
        def perThreadDriver2 = new BlockingVariable<WebDriver>()

        when:
        def globalDriver1 = globalFactory.driver
        Thread.start { globalDriver2.set(globalFactory.driver) }
        def perThreadDriver1 = perThreadFactory.driver
        Thread.start { perThreadDriver2.set(perThreadFactory.driver) }

        then:
        globalDriver1 == globalDriver2.get()
        globalDriver1 != perThreadDriver1
        perThreadDriver1 != perThreadDriver2.get()
    }

    @Issue("https://github.com/apache/groovy-geb/issues/288")
    def "first driver should quit after clear cache and quit driver"() {
        given:
        def firstDriver = browser.driver

        when:
        CachingDriverFactory.clearCacheAndQuitDriver()
        browser.driver = firstDriver
        go('https://groovy.apache.org/geb/manual/snapshot/')

        then:
        thrown(NoSuchSessionException)
    }
}
