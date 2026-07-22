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

import com.microsoft.playwright.options.Geolocation
import com.microsoft.playwright.options.ColorScheme
import com.microsoft.playwright.options.Media
import com.microsoft.playwright.options.ReducedMotion

@SuppressWarnings(['NoDouble', 'UnnecessarySetter'])
class PlaywrightEmulation {
    final PlaywrightWebDriver driver
    PlaywrightEmulation(PlaywrightWebDriver driver) { this.driver = driver }
    void setGeolocation(double longitude, double latitude, Double accuracy = null) {
        Geolocation geolocation = new Geolocation(longitude, latitude)
        if (accuracy != null) {
            geolocation.setAccuracy(accuracy)
        }
        driver.context.setGeolocation(geolocation)
    }
    void setOffline(boolean offline) { driver.context.setOffline(offline) }
    void setExtraHTTPHeaders(Map<String, String> headers) { driver.context.setExtraHTTPHeaders(headers) }
    void setViewportSize(int width, int height) { driver.page.setViewportSize(width, height) }
    void emulateMedia(String media = null, String colorScheme = null, String reducedMotion = null) {
        def options = new com.microsoft.playwright.Page.EmulateMediaOptions()
        if (media != null) {
            options.setMedia(Media.valueOf(media.toUpperCase()))
        }
        if (colorScheme != null) {
            options.setColorScheme(ColorScheme.valueOf(colorScheme.toUpperCase()))
        }
        if (reducedMotion != null) {
            options.setReducedMotion(ReducedMotion.valueOf(reducedMotion.toUpperCase()))
        }
        driver.page.emulateMedia(options)
    }
    void grantPermissions(List<String> permissions, String origin = null) {
        def options = new com.microsoft.playwright.BrowserContext.GrantPermissionsOptions()
        if (origin != null) {
            options.setOrigin(origin)
        }
        driver.context.grantPermissions(permissions, options)
    }
    void clearPermissions() { driver.context.clearPermissions() }
}
