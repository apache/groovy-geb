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
package geb.download.helper

import javax.net.ssl.*
import java.security.KeyStore

class SelfSignedCertificateHelper {
    URL keystoreUrl
    String keystoreFilePassword

    SelfSignedCertificateHelper(URL keystoreUrl, String keystoreFilePassword) {
        this.keystoreUrl = keystoreUrl
        this.keystoreFilePassword = keystoreFilePassword
    }

    @SuppressWarnings("UnnecessarySetter")
    void acceptCertificatesFor(HttpsURLConnection con) {
        con.setSSLSocketFactory(socketFactory)
        con.hostnameVerifier = hostnameVerifier
    }

    private SSLSocketFactory getSocketFactory() {
        def keyStore = KeyStore.getInstance(KeyStore.defaultType)
        keyStore.load(keystoreUrl.openStream(), keystoreFilePassword.toCharArray())
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.defaultAlgorithm)
        tmf.init(keyStore)
        SSLContext ctx = SSLContext.getInstance('TLS')
        ctx.init(null, tmf.trustManagers, null)
        ctx.socketFactory
    }

    private HostnameVerifier getHostnameVerifier() {
        new HostnameVerifier() {
            @Override
            boolean verify(String hostname, SSLSession sslSession) {
                true
            }
        }
    }

}
