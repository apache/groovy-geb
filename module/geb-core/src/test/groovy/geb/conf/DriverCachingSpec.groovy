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
package geb.conf

import geb.BuildAdapter
import geb.Configuration
import geb.driver.CachingDriverFactory
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

class DriverCachingSpec extends Specification {

    def num = 0

    private static class NullBuildAdapter implements BuildAdapter {
        String getBaseUrl() { null }

        File getReportsDir() { null }
    }

    def conf(cacheDriverPerThread = false, cacheDriver = true) {
        def conf = new Configuration(new ConfigObject(), new Properties(), new NullBuildAdapter())
        conf.cacheDriver = cacheDriver
        conf.cacheDriverPerThread = cacheDriverPerThread
        conf.driverConf = { new HtmlUnitDriver() }
        conf
    }

    def setupSpec() {
        CachingDriverFactory.clearCacheCache()
    }

    def "per thread caching yields a new driver on a different thread"() {
        given:
        def conf1 = conf(true)
        def conf2 = conf(true)

        and:
        def holder = new BlockingVariable()

        when:
        Thread.start { holder.set(conf2.createDriver()) }

        and:
        def driver1 = conf1.createDriver()
        def driver2 = holder.get()

        then:
        !driver1.is(driver2)

        and:
        driver1.is conf(true).createDriver()

        when:
        CachingDriverFactory.clearCacheAndQuitDriver()

        then:
        !driver1.is(conf(true).createDriver())

        cleanup:
        driver1.quit()
        driver2.quit()
    }

    def "global caching yields the same driver on different threads"() {
        given:
        def conf1 = conf()
        def conf2 = conf()

        and:
        def holder = new BlockingVariable()

        when:
        Thread.start { holder.set(conf2.createDriver()) }

        and:
        def driver1 = conf1.createDriver()
        def driver2 = holder.get()

        then:
        driver1.is(driver2)

        and:
        driver1.is conf().createDriver()

        when:
        CachingDriverFactory.clearCacheAndQuitDriver()

        then:
        !driver1.is(conf(true).createDriver())

        cleanup:
        driver1.quit()
        driver2.quit()
    }

    def "disabled caching yields different drivers on each call"() {
        given:
        def conf = conf(false, false)

        when:
        def driver1 = conf.createDriver()
        def driver2 = conf.createDriver()

        then:
        !driver1.is(driver2)

        cleanup:
        driver1.quit()
        driver2.quit()
    }

    def cleanup() {
        CachingDriverFactory.clearCacheCache()
    }

}
