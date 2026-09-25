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
package geb.direct

import com.microsoft.playwright.Route
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@SuppressWarnings('UnnecessarySetter')
class PlaywrightNetwork {
    final PlaywrightWebDriver driver
    private final Map<Closure, Consumer<Route>> handlersByClosure = new ConcurrentHashMap<>()

    PlaywrightNetwork(PlaywrightWebDriver driver) { this.driver = driver }

    void route(String url, Closure handler) {
        Consumer<Route> adapted = handlersByClosure.computeIfAbsent(handler) { Closure source ->
            ({ Route route -> source.call(route) } as Consumer<Route>)
        }
        driver.context.route(url, adapted)
    }

    void unroute(String url) { driver.context.unroute(url) }

    void unroute(String url, Closure handler) {
        Consumer<Route> adapted = handlersByClosure.remove(handler)
        if (adapted != null) {
            driver.context.unroute(url, adapted)
        }
    }

    void intercept(String url, Closure handler) { route(url, handler) }

    void continueRequest(Route route) { route.resume() }

    void abort(Route route) { route.abort() }

    void resume(Route route, Map<String, String> headers) {
        route.resume(new Route.ResumeOptions().setHeaders(headers))
    }

    void abort(Route route, String errorCode) { route.abort(errorCode) }

    void fulfill(Route route, int status = 200, String body = null, Map<String, String> headers = [:]) {
        Route.FulfillOptions options = new Route.FulfillOptions().setStatus(status).setHeaders(headers)
        if (body != null) {
            options.setBody(body)
        }
        route.fulfill(options)
    }

    void routeFromHAR(Path path) { driver.context.routeFromHAR(path) }

    void routeFromHAR(String path) { routeFromHAR(Path.of(path)) }

    Object waitForRequest(String url, Closure trigger) { driver.wait.waitForRequest(url, trigger) }

    Object waitForResponse(String url, Closure trigger) { driver.wait.waitForResponse(url, trigger) }
}
