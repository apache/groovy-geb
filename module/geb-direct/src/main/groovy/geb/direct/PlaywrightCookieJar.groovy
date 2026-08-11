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

import org.openqa.selenium.Cookie

/**
 * Cookie jar scoped to the current document host, matching Selenium WebDriver semantics.
 */
@SuppressWarnings([
    'UnnecessarySetter',
    'DuplicateStringLiteral',
    'StaticMethodsBeforeInstanceMethods'
])
class PlaywrightCookieJar {
    final PlaywrightWebDriver driver

    PlaywrightCookieJar(PlaywrightWebDriver driver) {
        this.driver = driver
    }

    Set<Cookie> getCookies() {
        currentDocumentCookies().collect { cookie -> toSeleniumCookie(cookie) } as LinkedHashSet
    }

    Cookie getCookieNamed(String name) {
        cookies.find { it.name == name }
    }

    void addCookie(Cookie cookie) {
        com.microsoft.playwright.options.Cookie value = new com.microsoft.playwright.options.Cookie(cookie.name, cookie.value)
            .setSecure(cookie.secure)
            .setHttpOnly(cookie.httpOnly)
        if (cookie.domain) {
            value.setDomain(cookie.domain).setPath(cookie.path ?: '/')
        } else {
            value.setUrl(driver.currentUrl)
        }
        if (cookie.expiry) {
            value.setExpires(cookie.expiry.time / 1000d)
        }
        driver.context.addCookies([value])
    }

    void deleteCookieNamed(String name) {
        currentDocumentCookies().findAll { it.name == name }.each { cookie ->
            clearCookie(cookie)
        }
    }

    void deleteCookie(Cookie cookie) {
        if (cookie.domain) {
            driver.context.clearCookies(
                new com.microsoft.playwright.BrowserContext.ClearCookiesOptions()
                    .setName(cookie.name)
                    .setDomain(cookie.domain)
                    .setPath(cookie.path ?: '/')
            )
        } else {
            deleteCookieNamed(cookie.name)
        }
    }

    void deleteAllCookies() {
        currentDocumentCookies().each { cookie -> clearCookie(cookie) }
    }

    private List<com.microsoft.playwright.options.Cookie> currentDocumentCookies() {
        String url = driver.currentUrl
        if (!url || url == 'about:blank') {
            return []
        }
        String host = hostFromUrl(url)
        if (!host) {
            return []
        }
        List<com.microsoft.playwright.options.Cookie> byUrl = driver.context.cookies(url)
        if (byUrl) {
            return byUrl
        }
        driver.context.cookies().findAll { cookie ->
            !cookie.domain || domainMatchesHost(cookie.domain, host)
        }
    }

    private void clearCookie(com.microsoft.playwright.options.Cookie cookie) {
        driver.context.clearCookies(
            new com.microsoft.playwright.BrowserContext.ClearCookiesOptions()
                .setName(cookie.name)
                .setDomain(cookie.domain)
                .setPath(cookie.path ?: '/')
        )
    }

    private static Cookie toSeleniumCookie(com.microsoft.playwright.options.Cookie cookie) {
        Cookie.Builder builder = new Cookie.Builder(cookie.name, cookie.value)
            .domain(cookie.domain)
            .path(cookie.path)
            .isSecure(cookie.secure)
            .isHttpOnly(cookie.httpOnly)
        if (cookie.expires > 0) {
            builder.expiresOn(new Date((long) (cookie.expires * 1000)))
        }
        builder.build()
    }

    private static boolean domainMatchesHost(String domain, String host) {
        if (!domain || !host) {
            return false
        }
        String normalized = domain.startsWith('.') ? domain.substring(1) : domain
        host.equalsIgnoreCase(normalized) || host.toLowerCase(Locale.ROOT).endsWith('.' + normalized.toLowerCase(Locale.ROOT))
    }

    private static String hostFromUrl(String url) {
        try {
            return URI.create(url).host
        } catch (Exception ignored) {
            return null
        }
    }
}
