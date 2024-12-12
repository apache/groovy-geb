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

import geb.Configuration
import geb.driver.DriverCreationException
import geb.error.UnableToLoadAnyDriversException
import geb.error.UnknownDriverShortNameException
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class ConfigurationDriverCreationSpec extends Specification {

    @Shared
    ClassLoader originalContextClassLoader

    @Shared
    ClassLoader classLoader

    def d

    def setupSpec() {
        // We have to suppress the ie driver on the classpath
        classLoader = new ClassLoader(getClass().classLoader) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                name.startsWith('org.openqa.selenium.ie.') ?
                findClass(name) :
                super.loadClass(name, resolve)
            }
        }

        originalContextClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
    }

    def cleanupSpec() {
        Thread.currentThread().contextClassLoader = originalContextClassLoader
    }

    def cleanup() {
        d?.quit()
    }

    def p(m = [:]) {
        def p = new Properties()
        p.putAll(m)
        p
    }

    def c(m = [:]) {
        def c = loadClass(ConfigObject).newInstance()
        c.putAll(m)
        c
    }

    def conf(Object[] args) {
        def configurationArguments = args.size() < 2 ? [args.size() == 0 ? null : args[0], p()] : args

        def conf = loadClass(Configuration).newInstance(*configurationArguments)
        conf.cacheDriver = false
        conf
    }

    def loadClass(Class clazz) {
        classLoader.loadClass(clazz.name)
    }

    boolean isInstanceOf(Class clazz, Object instance) {
        loadClass(clazz).isInstance(instance)
    }

    def "no property"() {
        when:
        d = conf().driver
        then:
        isInstanceOf(HtmlUnitDriver, d)
    }

    def "specific short name"() {
        when:
        d = conf(c(), p("geb.driver": "htmlunit")).driver
        then:
        isInstanceOf(HtmlUnitDriver, d)
    }

    def "specific valid short name but not available"() {
        when:
        conf(c(), p("geb.driver": "ie")).driver
        then:
        Exception e = thrown()
        isInstanceOf(UnableToLoadAnyDriversException, e)
    }

    def "specific invalid shortname"() {
        when:
        conf(c(), p("geb.driver": "garbage")).driver
        then:
        Exception e = thrown()
        isInstanceOf(UnknownDriverShortNameException, e)
    }

    def "specific list of drivers"() {
        when:
        d = conf(c(), p("geb.driver": "ie:htmlunit")).driver
        then:
        isInstanceOf(HtmlUnitDriver, d)
    }

    def "specific valid class name"() {
        when:
        d = conf(c(), p("geb.driver": HtmlUnitDriver.name)).driver
        then:
        isInstanceOf(HtmlUnitDriver, d)
    }

    def "specific invalid class name"() {
        when:
        d = conf(c(), p("geb.driver": "a.b.c")).driver
        then:
        Exception e = thrown()
        isInstanceOf(UnableToLoadAnyDriversException, e)
    }

    def "specify instance"() {
        when:
        def driver = loadClass(HtmlUnitDriver).newInstance()
        d = conf(c(driver: driver)).driver
        then:
        Exception e = thrown()
        isInstanceOf(IllegalStateException, e)
    }

    def "specify driver name in config"() {
        when:
        d = conf(c(driver: HtmlUnitDriver.name)).driver
        then:
        isInstanceOf(HtmlUnitDriver, d)
    }

    def "specify driver names in config"() {
        when:
        d = conf(c(driver: "ie:htmlunit")).driver
        then:
        isInstanceOf(HtmlUnitDriver, d)
    }

    def "specify creation closure"() {
        when:
        def config = new ConfigObject()
        config.cacheDriver = false
        config.driver = { new HtmlUnitDriver() }
        d = new Configuration(config, p()).driver

        then:
        d instanceof HtmlUnitDriver
    }

    @Issue('https://github.com/geb/issues/issues/231')
    def "DriverCreationException is thrown when creation closure returns something that is not a driver instance"() {
        given:
        def config = new ConfigObject()
        config.cacheDriver = false
        config.driver = { 'not a driver' }

        when:
        new Configuration(config).driver

        then:
        thrown(DriverCreationException)
    }
}
