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

import geb.Configuration
import geb.download.helper.SelfSignedCertificateHelper
import geb.test.CallbackHttpsServer
import geb.test.GebSpecWithServer
import geb.test.TestHttpServer
import spock.lang.Shared

import javax.net.ssl.HttpsURLConnection

class HttpsDownloadingSpec extends GebSpecWithServer {

    private static final Closure<Void> CONFIGURE_CONNECTION_FOR_SELF_SIGNED_CERT = { HttpURLConnection connection ->
        if (connection instanceof HttpsURLConnection) {
            def helper = new SelfSignedCertificateHelper(getClass().getResource('/keystore.jks'), 'password')
            helper.acceptCertificatesFor(connection as HttpsURLConnection)
        }
    }

    @Shared
    CallbackHttpsServer httpsServer = new CallbackHttpsServer(browser.config)

    Configuration config
    ConfigObject rawConfig

    def setup() {
        config = browser.config
        rawConfig = config.rawConfig

        httpsServer.get = { req, res ->
            res.contentType = "text/plain"
            res.outputStream << "from https"
        }
    }

    TestHttpServer getServerInstance() {
        httpsServer
    }

    void 'can download from endpoints with self-signed certificates'() {
        expect:
        downloadText(browser.baseUrl, CONFIGURE_CONNECTION_FOR_SELF_SIGNED_CERT) == 'from https'
    }

    void 'download connections can be configured globally'() {
        when:
        rawConfig.defaultDownloadConfig = CONFIGURE_CONNECTION_FOR_SELF_SIGNED_CERT

        then:
        downloadText(browser.baseUrl) == 'from https'
    }
}
