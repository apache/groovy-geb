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
package geb.direct.support

import com.sun.net.httpserver.HttpServer
import geb.direct.PlaywrightDriver
import geb.direct.PlaywrightWebDriver
import org.opentest4j.TestAbortedException

import java.nio.file.Files
import java.nio.file.Path

final class PlaywrightSpecSupport {
    private static final String TRUE = 'true'
    private PlaywrightSpecSupport() { }

    static boolean skipped() {
        System.getProperty('geb.direct.playwright.skip') == TRUE || System.getenv('PLAYWRIGHT_SKIP') == TRUE
    }

    static PlaywrightWebDriver driver(Closure configuration = {}) {
        try {
            PlaywrightDriver.create {
                headless = true
                browserType = 'chromium'
                Closure configured = configuration.rehydrate(delegate, configuration.owner, configuration.thisObject)
                configured.resolveStrategy = Closure.DELEGATE_FIRST
                configured.call()
            }
        } catch (Exception exception) {
            throw new TestAbortedException("Playwright Chromium is unavailable: ${exception.message}", exception)
        }
    }

    static HttpServer server(Closure<String> response) {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0)
        server.createContext('/') { exchange ->
            byte[] body = response.call(exchange.requestURI.path, exchange.requestHeaders).getBytes('UTF-8')
            exchange.responseHeaders.set('Content-Type', 'text/html; charset=UTF-8')
            exchange.sendResponseHeaders(200, body.length)
            exchange.responseBody.withCloseable { it.write(body) }
        }
        server.start()
        server
    }

    static String url(HttpServer server, String path = '/') {
        "http://localhost:${server.address.port}${path}"
    }

    static Path tempFile(String prefix = 'geb-direct-', String suffix = '.txt', String content = 'Geb Direct') {
        Path file = Files.createTempFile(prefix, suffix)
        Files.writeString(file, content)
        file
    }
}
