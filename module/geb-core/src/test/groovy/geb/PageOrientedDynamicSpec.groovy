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

import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class PageOrientedDynamicSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            def path = req.requestURI == "/b" ? "b" : "a"
            def other = path == "b" ? "a" : "b"
            res.outputStream << "<html>"
            if (path == "b") {
                res.outputStream << """
                <head>
                    <script type="text/javascript" charset="utf-8">
                    setTimeout(function() {
                        document.body.innerHTML += '<div id="dynamic">dynamic</div>';
                    }, 100);
                    </script>
                </head>"""
            }
            res.outputStream << """
            <body>
                <a href="/$other" id="$path">$other</a>
                <div id="uri">$req.requestURI</div>
                <input type="text" name="input"></input>
            </body>"""
        }
    }

    @Unroll
    def "implicitly waits when at checking if toWait content option is specified for '#contentName' content"() {
        when:
        to PageOrientedDynamicSpecPageA

        and:
        page[contentName].click()

        then:
        page in PageOrientedDynamicSpecPageC

        where:
        contentName << ["linkWithToWait", "linkWithToWaitUsingPageInstance"]
    }

    def "implicitly waits when at checking after clicking on content that has to option specified if global atCheckWaiting is specified"() {
        given:
        config.atCheckWaiting = true

        when:
        to PageOrientedDynamicSpecPageA

        and:
        linkToPageWithDynamicContent.click()

        then:
        page in PageOrientedDynamicSpecPageC
    }

    @Unroll
    def "implicitly waits when at checking if toWait content option is specified and to option contains a list of candidates for '#contentName' content"() {
        when:
        to PageOrientedDynamicSpecPageA

        and:
        page[contentName].click()

        then:
        page in PageOrientedDynamicSpecPageC

        where:
        contentName << ["linkWithToWaitAndVariantTo", "linkWithToWaitAndVariantToUsingPageInstances"]
    }
}

class PageOrientedDynamicSpecPageA extends Page {
    static at = { link }
    static content = {
        link { $("#a") }
        linkToPageWithDynamicContent(to: PageOrientedDynamicSpecPageC) { link }
        linkWithToWait(to: PageOrientedDynamicSpecPageC, toWait: true) { link }
        linkWithToWaitUsingPageInstance(to: new PageOrientedDynamicSpecPageC(), toWait: true) { link }
        linkWithToWaitAndVariantTo(to: [PageOrientedDynamicSpecPageB, PageOrientedDynamicSpecPageC], toWait: true) { link }
        linkWithToWaitAndVariantToUsingPageInstances(to: [new PageOrientedDynamicSpecPageB(), new PageOrientedDynamicSpecPageC()], toWait: true) { link }
    }
}

class PageOrientedDynamicSpecPageB extends Page {
    static at = { false }
}

class PageOrientedDynamicSpecPageC extends Page {
    static at = { dynamic }
    static content = {
        dynamic { $("#dynamic") }
    }
}
