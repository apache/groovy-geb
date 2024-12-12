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
package geb

import geb.error.InvalidPageContent
import geb.error.ModuleInstanceNotInitializedException
import geb.test.GebSpecWithCallbackServer
import spock.lang.Issue
import spock.lang.Unroll

class ModulesSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml {
            body {
                div('class': 'a') { p('a') }
                div('class': 'b') { p('a') }
                div('class': 'c') {
                    div('class': 'd') { p('d') }
                }
                div { p('some text') }
                input(type: "text", name: "input")
            }
        }
    }

    def "modules"() {
        when:
        to ModulesSpecPage
        then:
        divNoBase("a").p.text() == "a"
        divWithBase("a").p.text() == "a"
        divWithBaseAndSpecificBaseAndParam.p.text() == "d"
        divA.p.text() == "a"
        divC.innerDiv.p.text() == "d"
        divCWithRelativeInner.innerDiv.p.text() == "d"
    }

    def "call in mixed in method from TextMatchingSupport"() {
        when:
        to ModulesSpecPage
        then:
        divA.iContains("b").matches("abc")
    }

    def "module base must return a navigator"() {
        when:
        to ModulesSpecPage
        badBase
        then:
        thrown(InvalidPageContent)
    }

    @Issue("https://github.com/geb/issues/issues/2")
    def "can access module instance methods from content"() {
        when:
        to ModulesSpecPage
        then:
        instanceMethod.val == 3
    }

    @Issue("https://github.com/geb/issues/issues/38")
    def "can coerce module to boolean"() {
        when:
        to ModulesSpecPage

        then:
        optional("a")
        !optional("e")
        !optional("e").p
    }

    @Unroll
    def 'content created with #scenario contains a list of expected Module instances and of expected size'() {
        when:
        to ModulesSpecPage

        then:
        page[repeating].size() == 5
        page[repeating].every { it.stringRepresentation == ModulesSpecDivModuleNoLocator.name }
        page[repeatingWithParam].size() == 5
        page[repeatingWithParam].every { it.stringRepresentation == ModulesSpecDivModuleWithLocator.name }

        where:
        scenario                 | repeating              | repeatingWithParam
        "module list"            | "repeating"            | "repeatingWithParam"
        "spread operator"        | "repeatingUsingSpread" | "repeatingWithParamUsingCollect"
    }

    @Unroll
    def 'content created with module list contains consecutive modules'() {
        when:
        to ModulesSpecPage

        then:
        repeating[index].p.text() == repeatingText
        repeatingWithParam[index] as Boolean == repeatingWithParamAvailable

        where:
        index | repeatingText | repeatingWithParamAvailable
        0     | 'a'           | false
        1     | 'a'           | false
        2     | 'd'           | true
        3     | 'd'           | false
    }

    @Unroll
    def 'content created with spread operator contains consecutive modules'() {
        when:
        to ModulesSpecPage

        then:
        repeatingUsingSpread[index].p.text() == repeatingText
        repeatingWithParamUsingCollect[index] as Boolean == repeatingWithParamAvailable

        where:
        index | repeatingText | repeatingWithParamAvailable
        0     | 'a'           | false
        1     | 'a'           | false
        2     | 'd'           | true
        3     | 'd'           | false
    }

    def 'moduleList also supports ranges'() {
        when:
        to ModulesSpecPage

        then:
        repeating[1..2]*.p*.text() == ['a', 'd']
        repeatingUsingSpread[1..2]*.p*.text() == ['a', 'd']
    }

    def 'passing a class that does not extend from module as a module produces a human readable error'() {
        when:
        to ContainsAnInvalidModulePage
        invalid

        then:
        IllegalArgumentException e = thrown()
        e.message == "${Page} is not a subclass of ${Module}"
    }

    def 'can access an attribute of a module base'() {
        when:
        to ModulesSpecPage

        then:
        divA.@class == 'a'
    }

    def 'can access an attribute of a module base for a module that is not returned from a content definition'() {
        when:
        to ModulesSpecPage

        then:
        module(ModulesSpecSpecificDivModule).@class == 'a'
    }

    def 'base definitions have access to Geb text matchers'() {
        when:
        to ModulesSpecPage

        then:
        baseUsingMatcher
    }

    def 'base definitions have access to focused elements'() {
        when:
        to ModulesSpecPage
        input.click()

        then:
        baseUsingFocused.focused
    }

    @Unroll
    def "exception should be thrown when support class #className methods are used on an uninitialized module instance"() {
        def moduleInstance = new ModulesSpecDivModuleNoLocator()

        when:
        moduleInstance."$methodName"(*args)

        then:
        Throwable e = thrown(ModuleInstanceNotInitializedException)
        e.message == "Instance of module class geb.ModulesSpecDivModuleNoLocator has not been initialized. Please pass it to Navigable.module() or Navigator.module() before using it."

        where:
        className                | methodName    | args
        "PageContentSupport"     | "someContent" | []
        "Navigator"              | "find"        | ["div"]
        "DownloadSupport"        | "download"    | [""]
        "WaitingSupport"         | "waitFor"     | [{}]
        "FrameSupport"           | "withFrame"   | ["frame-id", {}]
        "AlertAndConfirmSupport" | "withAlert"   | [{}]
        "JavascriptInterface"    | "getJs"       | []
        "InteractionsSupport"    | "interact"    | [{}]
    }

    def "modules can access the browser instance"() {
        when:
        to PageWithBrowserAccessingModule

        then:
        browserAccessingModule.currentUrl == browser.currentUrl
    }

    def "accessing content names of a module"() {
        when:
        to ModulesSpecPage

        then:
        divNoBase("a").contentNames == ['p'].toSet()
    }

    def "accessing browser instance in base definition"() {
        when:
        to ModulesSpecPage

        then:
        browserAccessingBase.hasClass("a")
    }

    def "accessing content which uses a constant in the definition"() {
        when:
        to ModulesSpecPage

        then:
        constantUsing.p
    }
}

@SuppressWarnings("UnnecessaryCollectCall")
class ModulesSpecPage extends Page {
    static content = {
        // A module that doesn't define a locator, given one at construction
        divNoBase { $("div.$it").module(ModulesSpecDivModuleNoLocator) }

        // A module that defines a locator, given a param at construction
        divWithBase { module(new ModulesSpecDivModuleWithLocator(className: it)) }

        // A module that defines a location, and uses a param given at construction in the locator
        divWithBaseAndSpecificBaseAndParam { $("div.c").module(new ModulesSpecDivModuleWithLocator(className: "d")) }

        // A module that defines a location, and is contructed with no base or params
        divA { module ModulesSpecSpecificDivModule }

        // A module that itself has a module
        divC { module ModulesSpecDivModuleWithNestedDiv }

        // A module whose inner module is defined by the owner module's base
        divCWithRelativeInner { module ModulesSpecDivModuleWithNestedDivRelativeToModuleBase }

        input { $("input") }

        badBase { module ModulesSpecBadBase }

        baseUsingFocused { module ModulesSpecBaseUsingFocused }

        instanceMethod { module InstanceMethodModule }

        optional(required: false) { $("div.$it").module(OptionalModule) }

        // A list of modules, with the base of each module being set to the nth given navigator
        repeating { $('div').moduleList ModulesSpecDivModuleNoLocator }
        repeatingUsingSpread { $('div')*.module(ModulesSpecDivModuleNoLocator) }
        repeatingWithParam(required: false) {
            $('div').moduleList { new ModulesSpecDivModuleWithLocator(className: 'd') }
        }
        repeatingWithParamUsingCollect(required: false) {
            $('div').collect { it.module(new ModulesSpecDivModuleWithLocator(className: 'd')) }
        }

        baseUsingMatcher { module ModulesSpecBaseUsingTextMatcher }

        browserAccessingBase { module BrowserAccessingInBaseDefinitionModule }

        constantUsing { module ConstantUsingContentDefinitionModule }
    }
}

class ModulesSpecDivModuleNoLocator extends Module {
    static content = {
        p { $("p") }
    }
}

class ModulesSpecDivModuleWithLocator extends Module {
    static base = { $("div.$className") }
    static content = {
        p { $("p") }
    }
    def className
}

class ModulesSpecSpecificDivModule extends Module {
    static base = { $("div.a") }
    static content = {
        p { $("p") }
    }
}

class ModulesSpecDivModuleWithNestedDiv extends Module {
    static base = { $("div.c") }
    static content = {
        innerDiv { module(new ModulesSpecDivModuleWithLocator(className: "d")) }
    }
}

class ModulesSpecDivModuleWithNestedDivRelativeToModuleBase extends Module {
    static base = { $("div.c") }
    static content = {
        innerDiv { $().module(new ModulesSpecDivModuleWithLocator(className: "d")) }
    }
}

class ModulesSpecBaseUsingTextMatcher extends Module {
    static base = { $("div", text: startsWith('some')) }
}

class ModulesSpecBadBase extends Module {
    static base = { 1 }
}

class ModulesSpecBaseUsingFocused extends Module {
    static base = { focused() }
}

class InstanceMethodModule extends Module {
    static content = {
        val { getValue() }
    }

    def getValue() { 3 }
}

class OptionalModule extends Module {
    static content = {
        p(required: false) { $("p") }
    }
}

class ContainsAnInvalidModulePage extends Page {
    static content = {
        invalid { module Page }
    }
}

class PageWithBrowserAccessingModule extends Page {
    static content = {
        browserAccessingModule { module(BrowserAccessingModule) }
    }
}

class BrowserAccessingModule extends Module {
    String getCurrentUrl() {
        browser.currentUrl
    }
}

class BrowserAccessingInBaseDefinitionModule extends Module {
    static base = {
        browser.find("div.a")
    }
}

class ConstantUsingContentDefinitionModule extends Module {

    private final static String SELECTOR = "p"

    static content = {
        p { $(SELECTOR) }
    }
}