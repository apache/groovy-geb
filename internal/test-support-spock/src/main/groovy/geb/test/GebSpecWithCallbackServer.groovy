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
package geb.test

import spock.lang.Shared

class GebSpecWithCallbackServer extends GebSpecWithServer {

    protected final static String JQUERY_CODE = CallbackHttpServer.getResource("/jquery-1.4.2.min.js").text

    @Shared
    CallbackHttpServer callbackServer = new CallbackHttpServer({ browser.config })

    @Override
    TestHttpServer getServerInstance() {
        callbackServer
    }

    def responseHtml(Closure htmlMarkup) {
        callbackServer.responseHtml(htmlMarkup)
    }

    def responseHtml(String html) {
        callbackServer.responseHtml(html)
    }

    void html(Closure html) {
        responseHtml(html)
        go()
    }

    void html(String html) {
        responseHtml(html)
        go()
    }

    void bodyWithJquery(Closure closure = {}) {
        html {
            head {
                script(type: "text/javascript") {
                    mkp.yieldUnescaped JQUERY_CODE
                }
            }
            body {
                closure.delegate = delegate
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
            }
        }
    }
}
