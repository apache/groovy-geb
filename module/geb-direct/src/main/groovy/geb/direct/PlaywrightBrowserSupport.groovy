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

import geb.Browser
import org.openqa.selenium.WebDriver

final class PlaywrightBrowserSupport {
    private PlaywrightBrowserSupport() { }
    static PlaywrightWebDriver driver(Browser browser) { driver(browser.driver) }
    static PlaywrightWebDriver driver(WebDriver driver) {
        if (!(driver instanceof PlaywrightWebDriver)) {
            throw new IllegalArgumentException("Expected PlaywrightWebDriver but got ${driver.class.name}")
        }
        driver as PlaywrightWebDriver
    }
    static page(Browser browser) { driver(browser).page }
    static context(Browser browser) { driver(browser).context }
    static PlaywrightNetwork network(Browser browser) { driver(browser).network }
    static PlaywrightTracing tracing(Browser browser) { driver(browser).tracing }
    static PlaywrightLocators locators(Browser browser) { driver(browser).locators }
    static PlaywrightWait wait(Browser browser) { driver(browser).wait }
    static PlaywrightKeyboard keyboard(Browser browser) { driver(browser).keyboard }
    static PlaywrightMouse mouse(Browser browser) { driver(browser).mouse }
    static PlaywrightTouchscreen touchscreen(Browser browser) { driver(browser).touchscreen }
    static PlaywrightStorage storage(Browser browser) { driver(browser).storage }
    static PlaywrightEmulation emulation(Browser browser) { driver(browser).emulation }
    static PlaywrightDownloads downloads(Browser browser) { driver(browser).downloads }
    static PlaywrightHar har(Browser browser) { driver(browser).har }
    static PlaywrightConsole console(Browser browser) { driver(browser).console }
    static PlaywrightApiClient apiClient(Browser browser) { driver(browser).apiClient }
    static PlaywrightCoverage coverage(Browser browser) { driver(browser).coverage }
    static PlaywrightPdf pdf(Browser browser) { driver(browser).pdf }
    static PlaywrightAccessibility accessibility(Browser browser) { driver(browser).accessibility }
    static PlaywrightVideoSupport video(Browser browser) { driver(browser).video }
}
