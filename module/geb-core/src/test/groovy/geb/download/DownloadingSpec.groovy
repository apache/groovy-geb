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
package geb.download

import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

import javax.servlet.http.Cookie

class DownloadingSpec extends GebSpecWithCallbackServer {

    def setup() {
        callbackServer.get = { req, res ->
            res.contentType = "text/plain"
            res.outputStream << "initial"
        }
    }

    def "cookies are copied"() {
        given:
        callbackServer.get = { req, res ->
            res.contentType = "text/plain"
            res.addCookie(new Cookie("a", "1"))
            res.addCookie(new Cookie("b", "2"))
            res.outputStream << "cookies set"
        }

        when:
        go()

        and:
        def cookies
        callbackServer.get = { req, res ->
            cookies = req.cookies
            res.contentType = "text/plain"
            res.outputStream << "cookies received"
        }

        then:
        downloadText() == "cookies received"

        and:
        cookies.size() == 2
        cookies.find { it.name == "a" }.value == "1"
        cookies.find { it.name == "b" }.value == "2"
    }

    def "links are resolved relative to current page"() {
        given:
        go()
        callbackServer.get = { req, res ->
            res.contentType = "text/plain"
            res.outputStream << "${req.requestURI}"
        }

        expect:
        downloadText() == "/"
        downloadText("abc") == "/abc"
        downloadText("def/ghi") == "/def/ghi"

        when:
        go "def/ghi"

        then:
        downloadText("jkl") == "/def/jkl"
        downloadText("/mno") == "/mno"
    }

    def "http 500 causes DownloadException"() {
        given:
        go()
        callbackServer.get = { req, res ->
            res.sendError(500, "bang!")
        }

        when:
        downloadText("123")

        then:
        thrown(DownloadException)
    }

    @Unroll("download variants - method: #method")
    def "download variants - method: #method"() {
        given:
        callbackServer.get = { req, res ->
            res.contentType = "text/plain"
            res.outputStream << "123"
        }

        and:
        go()

        when:
        def result = this."$method"()
        if (resultProcessor instanceof Closure) {
            result = resultProcessor.call(result)
        }

        then:
        result == compareTo

        // Exercise the variants, just by calling them
        when:
        this."$method"("abc")
        this."$method"([:])

        then:
        notThrown(Exception)

        when:
        def called = false
        def callback = {
            assert it instanceof HttpURLConnection
            called = true
        }

        def wasCalled = {
            called = false
            it()
            assert called
            true
        }

        then:
        wasCalled { this."$method"(callback) }
        wasCalled { this."$method"("123", callback) }
        wasCalled { this."$method"([:], callback) }

        where:
        method            | resultProcessor | compareTo
        "downloadBytes"   | null            | "123" as byte[]
        "downloadContent" | { it.text }     | "123"
        "downloadText"    | null            | "123"
    }
}