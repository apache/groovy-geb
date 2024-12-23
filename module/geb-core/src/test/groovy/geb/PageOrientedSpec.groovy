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

import geb.content.TemplateDerivedPageContent
import geb.error.*
import geb.test.GebSpecWithCallbackServer
import spock.lang.Issue
import spock.lang.Unroll

class PageOrientedSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            def path = req.requestURI == "/b" ? "b" : "a"
            def other = path == "b" ? "a" : "b"
            res.outputStream << """
            <html>
                <head>
                    <script type="text/javascript" charset="utf-8">
                    setTimeout(function() {
                        document.body.innerHTML += '<div id="dynamic">dynamic</div>';
                    }, 100);
                    </script>
                </head>
                <body>
                    <a href="/$other" id="$path">$other</a>
                    <div id="uri">$req.requestURI</div>
                    <input type="text" name="input"></input>
                </body>
            </html>"""
        }
    }

    def "verify our server is configured correctly"() {
        when:
        go "/"
        then:
        $("#a").empty == false

        when:
        go "/a"
        then:
        $("#a").empty == false

        when:
        go "/b"
        then:
        $("#b").empty == false
    }

    @Unroll
    def "verify the Page API works for '#contentName' content"() {
        when:
        via PageOrientedSpecPageA

        then:
        at PageOrientedSpecPageA

        when:
        page[contentName].click()

        then:
        at PageOrientedSpecPageB

        when:
        page[contentName].click()

        then:
        at PageOrientedSpecPageA

        where:
        contentName << ["link", "linkUsingPageInstance"]
    }

    @Unroll
    def "verify the Page API #methodName method works with a Page instance"() {
        given:
        def parametrizedPage = new PageOrientedSpecParametrizedPage(id: "uri")

        when:
        def currentPage = "$methodName"(parametrizedPage)

        then:
        currentPage == parametrizedPage

        where:
        methodName << ["via", "at", "to", "page"]
    }

    def "check accessing non navigator content"() {
        when:
        to PageOrientedSpecPageA
        then:
        linkText == "b"
    }

    def "error when required value not present"() {
        when:
        to PageOrientedSpecPageA
        notPresentValueRequired.text()
        then:
        thrown(RequiredPageValueNotPresent)
    }

    def "error when required component not present"() {
        when:
        to PageOrientedSpecPageA
        notPresentRequired.text()
        then:
        thrown(RequiredPageContentNotPresent)
    }

    def "no error when non required component not present"() {
        when:
        to PageOrientedSpecPageA
        notPresentNotRequired.text()
        then:
        notThrown(RequiredPageContentNotPresent)
    }

    def "no error when non required component times out"() {
        when:
        to PageOrientedSpecPageA
        def content = notPresentNotRequiredWithWait
        then:
        notThrown(Exception)
        !content
        content in TemplateDerivedPageContent
    }

    def "error when explicitly requiring a component that is not present"() {
        when:
        to PageOrientedSpecPageA
        notPresentNotRequired.require()
        then:
        thrown(RequiredPageContentNotPresent)
    }

    def "no error when explicitly requiring component that is present"() {
        when:
        to PageOrientedSpecPageA
        link.require()
        then:
        notThrown(RequiredPageContentNotPresent)
    }

    @Unroll
    def "clicking on content with to specified changes the page for '#contentName' content"() {
        when:
        to PageOrientedSpecPageA
        page[contentName].click()

        then:
        page in PageOrientedSpecPageB

        where:
        contentName << ["link", "linkUsingPageInstance"]
    }

    @Unroll
    def "variant to should cycle through and select match for '#contentName' content"() {
        when:
        to PageOrientedSpecPageA
        page[contentName].click()

        then:
        page in PageOrientedSpecPageB

        where:
        contentName << ["linkWithVariantTo", "linkWithVariantToUsingPageInstances"]
    }

    @Unroll
    def "exception should be thrown when page specified in to is not the page we end up at - clicking on #clicked"() {
        when:
        to PageOrientedSpecPageA
        page[clicked].click()

        then:
        UnexpectedPageException e = thrown()
        e.message ==~ "At checker page verification failed for page .*"
        e.cause in cause

        where:
        clicked                                            | cause
        'linkWithNotMatchingTo'                            | AssertionError
        'linkWithNotMatchingToUsingPageInstance'           | AssertionError
        'linkWithToClassWithPlainFalseAt'                  | null
        'linkWithToClassWithPlainFalseAtUsingPageInstance' | null
    }

    @Unroll
    def "exception should be thrown when support class #className methods are used on an uninitialized page instance"() {
        def pageInstance = new PageOrientedSpecPageA()

        when:
        pageInstance."$methodName"(*args)

        then:
        Throwable e = thrown(PageInstanceNotInitializedException)
        e.message == "Instance of page class geb.PageOrientedSpecPageA has not been initialized. Please pass it to Browser.to(), Browser.via(), Browser.page() or Browser.at() before using it."

        where:
        className                | methodName    | args
        "PageContentSupport"     | "someContent" | []
        "Navigable"              | "find"        | [""]
        "DownloadSupport"        | "download"    | [""]
        "WaitingSupport"         | "waitFor"     | [{}]
        "FrameSupport"           | "withFrame"   | ["frame-id", {}]
        "InteractionsSupport"    | "interact"    | [{}]
        "AlertAndConfirmSupport" | "withAlert"   | [{}]
        "Browser"                | "verifyAt"    | []
    }

    @Unroll
    def "unexpected exceptions thrown in at checkers should bubble up from click for '#contentName' content"() {
        when:
        to PageOrientedSpecPageA
        page[contentName].click()

        then:
        Throwable e = thrown()
        e.message == "from at checker"

        where:
        contentName << ["linkWithToClassThrowingExceptionInAt", "linkWithToClassThrowingExceptionInAtUsingPageInstance"]
    }

    @Unroll
    def "exception should be thrown when no to values match for '#contentName' content"() {
        when:
        to PageOrientedSpecPageA
        page[contentName].click()

        then:
        thrown(UnexpectedPageException)

        where:
        contentName << ["linkWithVariantToNoMatches", "linkWithVariantToNoMatchesUsingPageInstances"]
    }

    def "call in mixed in method from TextMatchingSupport"() {
        when:
        to PageOrientedSpecPageA
        then:
        contains("b").matches("abc")
    }

    def "can use attribute notation on page content"() {
        when:
        to PageOrientedSpecPageA
        then:
        link.@id == "a"
    }

    @Issue("https://github.com/geb/issues/issues/2")
    def "can call instance methods from content definition blocks"() {
        when:
        to InstanceMethodPage
        then:
        val == 3
    }

    @Issue("https://github.com/geb/issues/issues/139")
    def "convertToPath should not introduce slashes were it should not"() {
        when: 'we go to the page by specifying the parameter manually'
        via ConvertPage, theParam: "foo"
        def manual = $('#uri').text()

        and: 'using the convertToPath method'
        via ConvertPage, 'foo'
        def converted = $('#uri').text()

        then: 'the results are the same'
        converted == manual

        then: 'the raw page url does not contain the extra slash'
        getPageUrl(convertToPath('foo')) == '/theview?theParam=foo'

        and: 'the default convertToPath still works'
        getPageUrl(convertToPath(1, 2)) == '/theview/1/2'
    }

    def "verify content aliasing works"() {
        when:
        to PageOrientedSpecPageA
        then:
        linkTextAlias == 'b'
    }

    def 'at check should fail when no at checker is defined on the page object class'() {
        when:
        at PageWithoutAtChecker

        then:
        def e = thrown UndefinedAtCheckerException
        e.message == "No at checker has been defined for page class geb.PageWithoutAtChecker."
    }

    @Unroll
    def "exception should be thrown when no at checker is defined for one of the to pages for '#contentName' content"() {
        when:
        to PageWithLinkToPageWithoutAtChecker
        page[contentName].click()

        then:
        thrown UndefinedAtCheckerException

        where:
        contentName << ["link", "linkUsingPageInstances"]
    }

    @Unroll
    def "invalid page parameter (#pageParameter) for content throws an informative exception"() {
        when:
        to pageClass

        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template '$contentName' of '${pageClass.name}' contains 'page' content parameter that is not a class that extends Page: $pageParameter"

        where:
        pageClass                        | contentName  | pageParameter
        PageContentStringPageParam       | 'wrongClass' | String
        PageContentPageInstancePageParam | 'instance'   | new PageContentPageInstancePageParam()
    }

    @Unroll
    def "implicitly waits when at checking if toWait content option is specified for '#contentName' content"() {
        when:
        to PageOrientedSpecPageA

        and:
        page[contentName].click()

        then:
        page in PageOrientedSpecPageE

        where:
        contentName << ["linkWithToWait", "linkWithToWaitUsingPageInstance"]
    }

    def "implicitly waits when at checking after clicking on content that has to option specified if global atCheckWaiting is specified"() {
        given:
        config.atCheckWaiting = true

        when:
        to PageOrientedSpecPageA

        and:
        linkToPageWithDynamicContent.click()

        then:
        page in PageOrientedSpecPageE
    }

    @Unroll
    def "implicitly waits when at checking if toWait content option is specified and to option contains a list of candidates for '#contentName' content"() {
        when:
        to PageOrientedSpecPageA

        and:
        page[contentName].click()

        then:
        page in PageOrientedSpecPageE

        where:
        contentName << ["linkWithToWaitAndVariantTo", "linkWithToWaitAndVariantToUsingPageInstances"]
    }

    def "unrecognized content template parameters are reported"() {
        when:
        to PageWithContentUsingUnrecognizedParams

        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template 'withInvalidParams' of '${PageWithContentUsingUnrecognizedParams.name}' uses unknown content parameters: bar, foo"
    }

    def "ensure that an exception message with all page wise error details is thrown when no match is found in given list of pages"() {
        when:
        to PageOrientedSpecPageA
        page(PageWithAtCheckerReturningFalse, PageOrientedSpecPageB, PageOrientedSpecPageC, PageWithAtCheckWaiting)

        then:
        UnexpectedPageException e = thrown()
        println e.getMessage()
        e.getMessage() =~ '''(?ms)^Unable to find page match\\. At checker verification results:$.*
^Result for geb\\.PageWithAtCheckerReturningFalse: false$.*
^Result for geb\\.PageOrientedSpecPageB: geb\\.error\\.RequiredPageContentNotPresent:.*
^Result for geb\\.PageOrientedSpecPageC: Assertion failed:.*
^false$.*
^Result for geb\\.PageWithAtCheckWaiting: geb\\.waiting\\.WaitTimeoutException:.*
Caused by: Assertion failed:.*
^false$.*'''
    }

    @Unroll
    def "at() method taking assertions closure verifies the at checker for page #scenario"() {
        given:
        via PageOrientedSpecPageA

        when:
        at(page) {}

        then:
        thrown AssertionError

        where:
        scenario   | page
        'instance' | new PageOrientedSpecPageC()
        'class'    | PageOrientedSpecPageC
    }

    @Unroll
    def "assertions closure passed to at() is executed with the page instance set as the delegate when #scenario is passed as the first argument"() {
        given:
        via PageOrientedSpecPageA

        expect:
        at(page) { linkText } == 'b'

        where:
        scenario   | page
        'instance' | new PageOrientedSpecPageA()
        'class'    | PageOrientedSpecPageA
    }

    @Unroll
    def "statements in the assertions closure passed to at() are implicitly asserted when #scenario is passed as the first argument"() {
        given:
        via PageOrientedSpecPageA

        when:
        at(page) {
            linkText == 'a'
        }

        then:
        AssertionError exception = thrown()
        exception.message.contains("linkText == 'a'")

        where:
        scenario   | page
        'instance' | new PageOrientedSpecPageA()
        'class'    | PageOrientedSpecPageA
    }

    def "accessing content names"() {
        expect:
        to(PageOrientedSpecPageWithContent).contentNames == ['simple', 'parameterized'].toSet()
    }

    def "accessing focused element in content definition"() {
        expect:
        to(PageOrientedSpecPageA).focusedContent.focused
    }

    @Unroll
    def "calling at methods from within a Page implementation results in a MissingMethodException and not the at checker closure to be executed"() {
        when:
        page(PageOrientedSpecPageB).at(*args)

        then:
        MissingMethodException e = thrown()
        e.method == "at"
        e.arguments == args
        e.type == PageOrientedSpecPageB

        where:
        args << [[PageOrientedSpecPageA], [new PageOrientedSpecPageA()], []]
    }

    @Issue("https://github.com/geb/issues/issues/640")
    def "using 'container' as a content element name is supported"() {
        expect:
        to(PageWithContentCalledContainer).container.tag() == "body"
    }
}

class PageOrientedSpecPageA extends Page {
    static at = { link }
    static content = {
        link(to: PageOrientedSpecPageB) { $("#a") }
        linkUsingPageInstance(to: new PageOrientedSpecPageB()) { $("#a") }
        linkWithNotMatchingTo(to: PageOrientedSpecPageC) { $("#a") }
        linkWithNotMatchingToUsingPageInstance(to: new PageOrientedSpecPageC()) { $("#a") }
        linkWithToClassThrowingExceptionInAt(to: PageWithAtCheckerThrowingException) { $("#a") }
        linkWithToClassThrowingExceptionInAtUsingPageInstance(to: new PageWithAtCheckerThrowingException()) { $("#a") }
        linkWithToClassWithPlainFalseAt(to: PageWithAtCheckerReturningFalse) { $("#a") }
        linkWithToClassWithPlainFalseAtUsingPageInstance(to: new PageWithAtCheckerReturningFalse()) { $("#a") }
        linkWithVariantTo(to: [PageOrientedSpecPageD, PageOrientedSpecPageC, PageOrientedSpecPageB]) { link }
        linkWithVariantToUsingPageInstances(to: [new PageOrientedSpecPageD(), new PageOrientedSpecPageC(), new PageOrientedSpecPageB()]) { link }
        linkWithVariantToNoMatches(to: [PageOrientedSpecPageD, PageOrientedSpecPageC]) { link }
        linkWithVariantToNoMatchesUsingPageInstances(to: [new PageOrientedSpecPageD(), new PageOrientedSpecPageC()]) { link }
        linkToPageWithDynamicContent(to: PageOrientedSpecPageE) { link }
        linkWithToWait(to: PageOrientedSpecPageE, toWait: true) { link }
        linkWithToWaitUsingPageInstance(to: new PageOrientedSpecPageE(), toWait: true) { link }
        linkWithToWaitAndVariantTo(to: [PageOrientedSpecPageC, PageOrientedSpecPageE], toWait: true) { link }
        linkWithToWaitAndVariantToUsingPageInstances(to: [new PageOrientedSpecPageC(), new PageOrientedSpecPageE()], toWait: true) { link }
        linkText { link.text().trim() }
        linkTextAlias(aliases: 'linkText')
        notPresentValueRequired { $("div#asdfasdf").text() }
        notPresentRequired { $("div#nonexistant") }
        notPresentNotRequired(required: false) { $("div#nonexistant") }
        notPresentNotRequiredWithWait(required: false, wait: 0.1) { $("div#nonexistant") }
        focusedContent { focused() }
    }
}

class PageOrientedSpecPageB extends Page {
    static at = { link }
    static content = {
        link(to: PageOrientedSpecPageA) { $("#b") }
        linkUsingPageInstance(to: new PageOrientedSpecPageA()) { $("#b") }
        linkText { link.text() }
    }
}

class PageOrientedSpecPageC extends Page {
    static at = { false }
}

@SuppressWarnings(["ComparisonOfTwoConstants", "InvertedCondition"])
class PageOrientedSpecPageD extends Page {
    static at = { assert 1 == 2 }
}

class PageOrientedSpecPageE extends Page {
    static at = { dynamic }
    static content = {
        dynamic { $("#dynamic") }
    }

}

class ConvertPage extends Page {
    static url = '/theview'

    String convertToPath(param) {
        "?theParam=$param"
    }
}

class InstanceMethodPage extends Page {
    static content = {
        val { getValue() }
    }

    def getValue() { 3 }
}

class PageContentPageInstancePageParam extends Page {
    static content = {
        instance(page: new PageContentPageInstancePageParam()) { $('a') }
    }
}

class PageContentStringPageParam extends Page {
    static content = {
        wrongClass(page: String) { $('a') }
    }
}

class PageWithAtChecker extends Page {
    static at = { false }
}

class PageWithoutAtChecker extends Page {
}

class PageWithLinkToPageWithoutAtChecker extends Page {
    static content = {
        link(to: [PageWithAtChecker, PageWithoutAtChecker]) { $("#a") }
        linkUsingPageInstances(to: [new PageWithAtChecker(), new PageWithoutAtChecker()]) { $("#a") }
    }
}

class PageWithAtCheckerThrowingException extends Page {
    static at = { throw new Throwable('from at checker') }
}

class PageWithAtCheckerReturningFalse extends Page {
    // this circumvents implicit assertion AST transformation
    static atChecker = { false }
    static at = atChecker
}

class PageOrientedSpecParametrizedPage extends Page {
    static at = { elementWithId }
    static content = {
        elementWithId { $(id: id) }
    }

    String id
}

class PageWithContentUsingUnrecognizedParams extends Page {
    static content = {
        withInvalidParams(foo: 1, bar: 2) { $() }
    }
}

class PageWithAtCheckWaiting extends Page {
    static at = { waitFor(0.1) { false } }
}

class PageOrientedSpecPageWithContent extends Page {
    static content = {
        simple { $() }
        parameterized { id -> $(id: id) }
    }
}

class PageWithMethodCallingAt extends Page {
    void callAt() {
        at(Page)
    }
}

class PageWithContentCalledContainer extends Page {
    static content = {
        container { $("body") }
    }
}
