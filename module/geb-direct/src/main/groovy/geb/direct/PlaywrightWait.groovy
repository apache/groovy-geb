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

import com.microsoft.playwright.options.LoadState
import java.util.function.Predicate

@SuppressWarnings('NoDouble')
class PlaywrightWait {
    final PlaywrightWebDriver driver
    PlaywrightWait(PlaywrightWebDriver driver) { this.driver = driver }
    void waitForLoadState(String state = 'load') { driver.page.waitForLoadState(LoadState.valueOf(state.toUpperCase())) }
    void waitForURL(String url) { driver.page.waitForURL(url) }
    Object waitForNavigation(Closure trigger) { driver.page.waitForNavigation { trigger.call() } }
    Object waitForSelector(String selector) { driver.page.waitForSelector(selector) }
    Object waitForFunction(String expression) { driver.page.waitForFunction(expression) }
    void waitForTimeout(double timeout) { driver.page.waitForTimeout(timeout) }
    Object waitForRequest(String url, Closure trigger) { driver.page.waitForRequest(url) { trigger.call() } }
    Object waitForRequest(Closure predicate, Closure trigger) { driver.page.waitForRequest({ request -> predicate.call(request) } as Predicate) { trigger.call() } }
    Object waitForResponse(String url, Closure trigger) { driver.page.waitForResponse(url) { trigger.call() } }
    Object waitForResponse(Closure predicate, Closure trigger) { driver.page.waitForResponse({ response -> predicate.call(response) } as Predicate) { trigger.call() } }
    Object waitForPopup(Closure trigger) { driver.page.waitForPopup { trigger.call() } }
    Object waitForDownload(Closure trigger) { driver.page.waitForDownload { trigger.call() } }
    Object waitForFileChooser(Closure trigger) { driver.page.waitForFileChooser { trigger.call() } }
    Object waitForConsoleMessage(Closure trigger) { driver.page.waitForConsoleMessage { trigger.call() } }
    Object waitForEvent(String event, Closure trigger) {
        switch (event.toLowerCase()) {
            case 'consolemessage': return waitForConsoleMessage(trigger)
            case 'download': return waitForDownload(trigger)
            case 'popup': return waitForPopup(trigger)
            case 'request': return waitForRequest({ true }, trigger)
            case 'response': return waitForResponse({ true }, trigger)
            case 'navigation': return waitForNavigation(trigger)
            default: throw new UnsupportedOperationException("Unsupported page wait event: $event")
        }
    }
    Object waitForContextEvent(String event, Closure trigger) {
        switch (event.toLowerCase()) {
            case 'consolemessage': return driver.context.waitForConsoleMessage { trigger.call() }
            case 'page': return driver.context.waitForPage { trigger.call() }
            default: throw new UnsupportedOperationException("Unsupported context wait event: $event")
        }
    }
}
