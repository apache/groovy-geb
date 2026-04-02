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
package geb.spock

import geb.Page
import geb.test.CallbackHttpServer
import spock.lang.Shared

class DynamicDispatchCompatibilitySpec extends GebSpec {

    @Shared
    CallbackHttpServer server = new CallbackHttpServer(browser.config)

    def setupSpec() {
        server.start()
        server.get = { req, res ->
            res.outputStream << """
            <html>
            <body>
                <div class="d1" id="d1">d1</div>
            </body>
            </html>"""
        }
    }

    def setup() {
        baseUrl = server.baseUrl
        go()
    }

    def cleanupSpec() {
        server.stop()
    }

    def 'metaClass-added page method can be used from content template'() {
        given:
        MetaContentPage.metaClass.dynamicText = { $('#d1').text() }
        to MetaContentPage

        expect:
        page.theText == 'd1'
    }

    def 'propertyMissing on page is honored inside content template'() {
        given:
        to PropertyMissingContentPage

        expect:
        page.dynamicThing == 'changed-by-property-missing'
    }

    def 'methodMissing on page is honored inside content template'() {
        given:
        to MethodMissingContentPage

        expect:
        page.dynamicThing == 'from-method-missing'
    }

    def 'metaClass-added page method is visible from page instance method'() {
        given:
        PageMethodCallPage.metaClass.dynamicText = { $('#d1').text() }
        to PageMethodCallPage

        expect:
        page.callDynamicText() == 'd1'
    }

    def 'propertyMissing remains visible from page instance method'() {
        given:
        to PagePropertyMethodPage

        expect:
        page.readDynamicProperty() == 'dynamic-prop-value'
    }

    def 'methodMissing remains visible from page instance method'() {
        given:
        to PageMethodMissingMethodPage

        expect:
        page.callDynamicMethod() == 'dynamic-method-value'
    }

    def 'metaClass-added navigator method can be called'() {
        given:
        def navigator = $('#d1')
        navigator.metaClass.dynamicTextValue = { -> delegate.text() }

        expect:
        navigator.dynamicTextValue() == 'd1'
    }

    def 'instance metaClass can add a navigator method'() {
        given:
        def navigator = $('#d1')
        navigator.metaClass.dynamicTextValue = { -> delegate.text() }

        expect:
        navigator.dynamicTextValue() == 'd1'
    }

    def 'page code can call a runtime-added navigator instance method'() {
        given:
        to NavigatorHolderPage
        def navigator = $('#d1')
        navigator.metaClass.dynamicTextValue = { -> delegate.text() }

        expect:
        page.readNavigatorText(navigator) == 'd1'
    }

}

class MetaContentPage extends Page {
    static content = {
        theText { dynamicText() }
    }
}

class PropertyMissingContentPage extends Page {
    def prop = 'initial'

    static content = {
        dynamicThing { bogus }
    }

    @Override
    def propertyMissing(String name) {
        prop = 'changed-by-property-missing'
        prop
    }
}

class MethodMissingContentPage extends Page {
    static content = {
        dynamicThing { doSomethingDynamic() }
    }

    @Override
    def methodMissing(String name, args) {
        'from-method-missing'
    }
}

class PageMethodCallPage extends Page {
    def callDynamicText() {
        dynamicText()
    }
}

class PagePropertyMethodPage extends Page {
    def readDynamicProperty() {
        bogus
    }

    @Override
    def propertyMissing(String name) {
        'dynamic-prop-value'
    }
}

class PageMethodMissingMethodPage extends Page {
    def callDynamicMethod() {
        doSomethingDynamic()
    }

    @Override
    def methodMissing(String name, args) {
        'dynamic-method-value'
    }
}

class NavigatorHolderPage extends Page {
    def readNavigatorText(navigator) {
        navigator.dynamicTextValue()
    }
}
