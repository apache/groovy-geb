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
package geb.binding

import geb.Browser
import geb.Initializable
import geb.Page
import geb.test.GebSpecWithCallbackServer

class BindingUpdaterSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            res.outputStream << """
            <html>
            <body>
                <p>content</p>
            </body>
            </html>"""
        }
        go "/"
    }

    def binding = new Binding()
    def updater = new BindingUpdater(binding, browser)

    def "updater lifecycle"() {
        when:
        binding.browser

        then:
        thrown MissingPropertyException

        when:
        updater.initialize()

        then:
        binding.browser.is(browser)
        binding.$.call("p").text() == "content"
        binding.page.is(browser.page)

        when:
        updater.remove()

        and:
        binding.browser == null

        then:
        thrown MissingPropertyException

        when:
        page BindingUpdaterSpecPage1

        and:
        binding.page

        then:
        thrown MissingPropertyException
    }

    def "dollar function dispatch works in binding"() {
        given:
        def shell = new GroovyShell(binding)

        when:
        updater.initialize()

        then:
        shell.evaluate("\$('p', text: 'content').size()") == 1
    }

    def "page changing"() {
        given:
        updater.initialize()

        when:
        page BindingUpdaterSpecPage1

        then:
        binding.page instanceof BindingUpdaterSpecPage1

        when:
        page BindingUpdaterSpecPage2

        then:
        binding.page instanceof BindingUpdaterSpecPage2
    }

    def "dispatching with wrong args produces MME"() {
        when:
        updater.initialize()

        and:
        binding.go(123)

        then:
        thrown MissingMethodException
    }

    def "all browser methods are delegated to"() {
        given:
        updater.initialize()

        when:
        def notDelegatedMethods = findPublicInstanceNonInternalMethodNames(Browser) - binding.variables.keySet()

        then:
        !notDelegatedMethods
    }

    def "all page methods are delegated to"() {
        given:
        updater.initialize()

        when:
        def notDelegatedMethods = findPublicInstanceNonInternalMethodNames(Page) - findPublicInstanceNonInternalMethodNames(Browser) - binding.variables.keySet()

        then:
        !notDelegatedMethods
    }

    def "only existing browser methods are delegated to"() {
        when:
        def extraDelegatedMethods = BindingUpdater.FORWARDED_BROWSER_METHODS - findPublicInstanceNonInternalMethodNames(Browser)

        then:
        !extraDelegatedMethods
    }

    def "only existing page methods are delegated to"() {
        when:
        def extraDelegatedMethods = BindingUpdater.FORWARDED_PAGE_METHODS - findPublicInstanceNonInternalMethodNames(Page)

        then:
        !extraDelegatedMethods
    }

    private Set<String> findPublicInstanceNonInternalMethodNames(Class clazz) {
        def ignoredMethods = Object.methods + GroovyObject.methods + Initializable.methods
        def internalMethodNames = ignoredMethods*.name + ["methodMissing", "propertyMissing"]
        def publicInstanceMethods = clazz.metaClass.methods.findAll { it.public && !it.static }
        publicInstanceMethods*.name.toSet() - internalMethodNames
    }
}

class BindingUpdaterSpecPage1 extends Page {
    static content = {
        p1 { $("p") }
    }
}

class BindingUpdaterSpecPage2 extends Page {
    static content = {
        p2 { $("p") }
    }
}