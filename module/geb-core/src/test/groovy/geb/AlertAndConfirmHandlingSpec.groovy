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

import geb.test.*

class AlertAndConfirmHandlingSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            res.outputStream << """
                <html>
                <body>
                    <script type="text/javascript" charset="utf-8">
                        var i = 0;
                        var confirmResult;
                    </script>
                    <input type="button" name="hasAlert" onclick="alert(++i);" />
                    <input type="button" name="noAlert" />
                    <input type="button" name="hasConfirm" onclick="confirmResult = confirm(++i);" />
                    <input type="button" name="noConfirm" />

                    <input type="button" name="hasAlertReload" onclick="alert(++i); window.location.reload();" />
                    <input type="button" name="noAlertReload" onclick="window.location.reload();"/>
                    <input type="button" name="hasConfirmReload" onclick="confirmResult = confirm(++i); window.location.reload();" />
                    <input type="button" name="noConfirmReload" onclick="window.location.reload();" />

                    <input type="button" name="hasAsynchronousAlert" onclick="setTimeout(function() { alert('asynchronous alert') }, 100);" />
                    <input type="button" name="hasAsynchronousConfirm" onclick="setTimeout(function() { confirm('asynchronous confirm') }, 100);" />

                </body>
                </html>
            """
        }
    }

    def setup() {
        go()
    }

    def "handle alert"() {
        expect:
        rationalise(withAlert { hasAlert().click() }) == 1
    }

    def "expect alert with page change"() {
        expect:
        withAlert { hasAlertReload().click() } == true
    }

    def "expect alert but don't get it"() {
        when:
        withAlert { noAlert().click() }
        then:
        thrown(AssertionError)
    }

    def "expect alert but don't get it with page change"() {
        expect:
        // no way of knowing if alert happened or not
        withAlert { noAlertReload().click() } == true
    }

    def "no alert and don't get one"() {
        when:
        withNoAlert { noAlert().click() }
        then:
        notThrown(AssertionError)
    }

    def "no alert and don't get one with page change"() {
        when:
        withNoAlert { noAlertReload().click() }
        then:
        notThrown(AssertionError)
    }

    def "no alert and do get one"() {
        when:
        withNoAlert { hasAlert().click() }
        then:
        thrown(AssertionError)
    }

    def "no alert and do get one with page change"() {
        when:
        withNoAlert { hasAlertReload().click() }
        then:
        notThrown(AssertionError)
    }

    def "nested alerts"() {
        when:
        def innerMsg
        def outerMsg = withAlert {
            innerMsg = withAlert { hasAlert().click() }
            hasAlert().click()
        }
        then:
        rationalise(innerMsg) == 1
        rationalise(outerMsg) == 2
    }

    def "withAlert supports waiting"() {
        expect:
        withAlert(wait: true) { hasAsynchronousAlert().click() } == 'asynchronous alert'
    }

    def "handle confirm"() {
        expect:
        rationalise(withConfirm(true) { hasConfirm().click() }) == 1
        confirmResult == true
        rationalise(withConfirm(false) { hasConfirm().click() }) == 2
        confirmResult == false
        rationalise(withConfirm { hasConfirm().click() }) == 3
        confirmResult == true
    }

    def "handle confirm with page change"() {
        expect:
        withConfirm(true) { hasConfirmReload().click() } == true
    }

    def "expect confirm but don't get it"() {
        when:
        withConfirm { noConfirm().click() }
        then:
        thrown(AssertionError)
    }

    def "expect confirm but don't get it with page change"() {
        expect:
        withConfirm { noConfirmReload().click() } == true
    }

    def "no confirm and don't get one"() {
        when:
        withNoConfirm { noConfirm().click() }
        then:
        notThrown(AssertionError)
    }

    def "no confirm and don't get one with page change"() {
        when:
        withNoConfirm { noConfirmReload().click() }
        then:
        notThrown(AssertionError)
    }

    def "no confirm and do get one with page change"() {
        when:
        withNoConfirm { hasConfirmReload().click() }
        then:
        notThrown(AssertionError)
    }

    def "nested confirms"() {
        when:
        def innerMsg
        def innerConfirmResult
        def outerConfirmResult
        def outerMsg = withConfirm(true) {
            innerMsg = withConfirm(false) { hasConfirm().click() }
            innerConfirmResult = confirmResult
            hasConfirm().click()
        }
        outerConfirmResult = confirmResult
        then:
        rationalise(innerMsg) == 1
        rationalise(outerMsg) == 2
        innerConfirmResult == false
        outerConfirmResult == true
    }

    def "withConfirm supports waiting"() {
        expect:
        withConfirm(wait: true) { hasAsynchronousConfirm().click() } == 'asynchronous confirm'
    }

    def "pages and modules have the methods too"() {
        given:
        page AlertAndConfirmHandlingSpecPage
        when:
        page.testOneOfTheMethods()
        mod.testOneOfTheMethods()
        then:
        notThrown(Exception)
    }

    // HTMLUnit does something strange when converting types
    // and changes integer '1' to string '1.0' when coercing
    private rationalise(value) {
        value[0].toInteger()
    }

    private getConfirmResult() {
        js.confirmResult
    }

}

class AlertAndConfirmHandlingSpecPage extends Page {
    static content = {
        mod { module AlertAndConfirmHandlingSpecModule }
    }

    def testOneOfTheMethods() {
        withNoAlert { true }
    }
}

class AlertAndConfirmHandlingSpecModule extends Module {
    def testOneOfTheMethods() {
        withNoAlert { true }
    }
}