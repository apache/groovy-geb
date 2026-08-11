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

import org.openqa.selenium.WebDriver
import java.time.Duration

/**
 * Selenium timeouts. Implicit wait only affects element lookup, not Playwright action timeouts.
 */
@SuppressWarnings(['UnnecessarySetter', 'DuplicateNumberLiteral'])
class PlaywrightTimeouts implements WebDriver.Timeouts {
    private static final long DEFAULT_ALERT_TIMEOUT_MS = 1000L

    final PlaywrightWebDriver driver
    private long alertTimeoutMillis
    private long implicitWaitMillis
    private long scriptTimeoutMillis

    PlaywrightTimeouts(PlaywrightWebDriver driver) {
        this.driver = driver
        implicitWaitMillis = 0L
        scriptTimeoutMillis = driver.session.options.defaultTimeoutMs as long
        alertTimeoutMillis = DEFAULT_ALERT_TIMEOUT_MS
    }

    long getAlertTimeoutMillis() { alertTimeoutMillis }

    long getImplicitWaitMillis() { implicitWaitMillis }

    long getScriptTimeoutMillis() { scriptTimeoutMillis }

    WebDriver.Timeouts implicitlyWait(Duration duration) {
        implicitWaitMillis = duration.toMillis()
        this
    }

    WebDriver.Timeouts pageLoadTimeout(Duration duration) {
        driver.context.setDefaultNavigationTimeout(duration.toMillis())
        this
    }

    WebDriver.Timeouts scriptTimeout(Duration duration) {
        scriptTimeoutMillis = duration.toMillis()
        this
    }
}
