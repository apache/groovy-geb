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

import java.nio.file.Path

class PlaywrightHar {
    final PlaywrightWebDriver driver
    PlaywrightHar(PlaywrightWebDriver driver) { this.driver = driver }
    void routeFromHAR(Path path) { driver.context.routeFromHAR(path) }
    void routeFromHAR(String path) { routeFromHAR(Path.of(path)) }

    /**
     * Removes all routes on the context, including HAR routing and any
     * {@link PlaywrightNetwork#route} handlers. Playwright does not expose
     * path-specific HAR unroute; use {@link #unrouteAll()} for clarity.
     */
    @Deprecated
    @SuppressWarnings('UnusedMethodParameter')
    void unrouteFromHAR(Path path) {
        unrouteAll()
    }

    void unrouteAll() {
        driver.context.unrouteAll()
    }
}
