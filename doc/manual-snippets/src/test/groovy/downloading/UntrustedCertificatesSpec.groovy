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
package downloading

// tag::import[]
import geb.download.helper.SelfSignedCertificateHelper
// end::import[]
import geb.test.CallbackHttpsServer
import geb.test.GebSpecWithCallbackServer
import geb.test.TestHttpServer
import spock.lang.Shared

import javax.net.ssl.HttpsURLConnection

class UntrustedCertificatesSpec extends GebSpecWithCallbackServer {

    @Shared
    CallbackHttpsServer httpsServer = new CallbackHttpsServer(browser.config)

    TestHttpServer getServerInstance() {
        httpsServer
    }

    def "using untrusted certificates"() {
        given:
        httpsServer.get = { req, res ->
            res.contentType = "text/plain"
            res.outputStream << "from https server"
        }
        go "/"

        when:
        // tag::example[]
        def text = downloadText { HttpURLConnection connection ->
            if (connection instanceof HttpsURLConnection) {
                def keystore = getClass().getResource('/keystore.jks')
                def helper = new SelfSignedCertificateHelper(keystore, 'password')
                helper.acceptCertificatesFor(connection as HttpsURLConnection)
            }
        }
        // end::example[]

        then:
        text == "from https server"
    }
}
