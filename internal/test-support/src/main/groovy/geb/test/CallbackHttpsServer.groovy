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

import geb.Configuration
import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory

import static org.eclipse.jetty.http.HttpVersion.HTTP_1_1

class CallbackHttpsServer extends CallbackHttpServer {

    private static final String PASSWORD = 'password'

    CallbackHttpsServer(Configuration configuration) {
        super(configuration)
    }

    String getProtocol() {
        'https'
    }

    protected Connector createConnector(Server server, int port) {
        def sslContextFactory = new SslContextFactory(
                keyStorePassword: PASSWORD,
                trustStorePassword: PASSWORD,
                keyManagerPassword: PASSWORD,
                keyStorePath: getClass().getResource('/keystore.jks').toString()
        )
        def httpsConfig = new HttpConfiguration(securePort: port)
        httpsConfig.addCustomizer(new SecureRequestCustomizer())
        def connector = new ServerConnector(
                server,
                new SslConnectionFactory(sslContextFactory, HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig)
        )
        connector.port = port
        connector
    }
}
