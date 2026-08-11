<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# Geb Direct

`geb-direct` is an optional Playwright-backed driver for Geb. It preserves Geb's `Browser`, Page Object, content DSL, modules, `waitFor`, reporting, and WebDriver-oriented test integrations while executing browser work through Playwright.

Selenium remains the default path. `geb-core` does not depend on Playwright, and existing Selenium configuration continues to work until a project adds this module and selects `PlaywrightDriver`.

## Dependency

Use the same Geb version for every Geb module.

```groovy
dependencies {
    testImplementation 'org.apache.groovy.geb:geb-direct:<version>'
}
```

Coordinates: `org.apache.groovy.geb:geb-direct`.

## GebConfig

Configure a Playwright driver factory in `GebConfig.groovy`. `PlaywrightDriver.config` returns a closure, which lets Geb create, cache, and close the driver through its normal lifecycle.

```groovy
import geb.direct.PlaywrightDriver
import geb.direct.report.PlaywrightTraceReporter
import geb.report.CompositeReporter
import geb.report.PageSourceReporter
import geb.report.ScreenshotReporter

driver = PlaywrightDriver.config {
    browserType = 'chromium'
    headless = true
    viewportWidth = 1280
    viewportHeight = 720
    defaultTimeoutMs = 30000
    navigationTimeoutMs = 30000

    // recordVideo = true
    // videoDir = 'build/playwright/video'
    // tracing = true
    // tracesDir = 'build/playwright/traces'
}

// Optional: add Playwright trace archives to regular Geb reports.
// reporter = new CompositeReporter(
//     new PageSourceReporter(),
//     new ScreenshotReporter(),
//     new PlaywrightTraceReporter()
// )
```

For explicit lifecycle management, create the driver yourself and call `quit()` when it is no longer needed.

```groovy
driver = {
    PlaywrightDriver.create {
        browserType = 'firefox'
        headless = false
    }
}
```

## PlaywrightOptions

The configuration closure delegates to `PlaywrightOptions`. Its defaults are suitable for headless Chromium in CI.

| Option | Default | Purpose |
| --- | --- | --- |
| `browserType` | `'chromium'` | Browser engine: `chromium`, `firefox`, or `webkit`. |
| `headless` | `true` | Run without a visible browser window. |
| `slowMo` | `0` | Delay Playwright operations by milliseconds, useful while debugging. |
| `channel` | `null` | Chromium distribution channel, such as `'chrome'`, when supported by the installed browser. |
| `recordVideo` | `false` | Record video for the browser context. |
| `videoDir` | `'build/playwright/video'` | Directory for recorded video files. |
| `tracing` | `false` | Start Playwright tracing as the driver is created. |
| `tracesDir` | `'build/playwright/traces'` | Directory for trace archives. |
| `screenshotsOnTrace` | `true` | Include screenshots in traces. |
| `snapshotsOnTrace` | `true` | Include DOM snapshots in traces. |
| `sourcesOnTrace` | `true` | Include source files in traces. |
| `locale` | `null` | Browser context locale. |
| `timezoneId` | `null` | Browser context time zone identifier. |
| `userAgent` | `null` | Override the browser context user agent. |
| `viewportWidth` | `1280` | Context viewport width in CSS pixels. |
| `viewportHeight` | `720` | Context viewport height in CSS pixels. |
| `ignoreHTTPSErrors` | `false` | Accept HTTPS certificate errors. |
| `baseURL` | `null` | Base URL used by Playwright navigation and request APIs that accept relative URLs. |
| `launchArgs` | `[]` | Additional browser launch arguments. |
| `defaultTimeoutMs` | `30000` | Default Playwright operation timeout in milliseconds. |
| `navigationTimeoutMs` | `30000` | Default navigation timeout in milliseconds. |

## WebDriver adapter

`PlaywrightWebDriver` implements `WebDriver`, `JavascriptExecutor`, `TakesScreenshot`, and `HasCapabilities`. The normal Geb DSL therefore stays the preferred interface.

| Capability | Behavior |
| --- | --- |
| Navigation, title, URL, page source | Maps to the current Playwright page. |
| CSS and supported WebDriver `By` selectors | Resolved through Playwright locators. |
| Element interaction | `click`, `sendKeys`, clear, selected, enabled, text, attributes, and screenshots are adapter-backed. Locator actions use Playwright actionability checks and auto-waiting. |
| JavaScript | `executeScript` and `executeAsyncScript` evaluate in the current frame. |
| Screenshots | Implements `TakesScreenshot`, so Geb's `ScreenshotReporter` works. |
| Cookies and timeouts | Available through `driver.manage()`. Cookies are scoped to the Playwright browser context. |
| Windows, frames, and dialogs | Exposed through `driver.switchTo()` with the notes below. |

Each driver creates one Playwright `BrowserContext`. That context isolates cookies, local storage, session storage, permissions, and related browser state from other driver instances.

## PlaywrightBrowserSupport

Use `PlaywrightBrowserSupport` when a test needs a Playwright feature that does not have a WebDriver equivalent. Pass Geb's `browser` object. These APIs deliberately expose Playwright Java objects, so consult the Playwright Java API for the arguments and return types.

```groovy
import geb.direct.PlaywrightBrowserSupport

def driver = PlaywrightBrowserSupport.driver(browser)
def page = PlaywrightBrowserSupport.page(browser)
def context = PlaywrightBrowserSupport.context(browser)
```

The following table shows where to find each Playwright surface.

| Need | Facade | Examples |
| --- | --- | --- |
| Network routing and interception | `network(browser)` | `route`, `unroute`, `intercept`, `continueRequest`, `abort` |
| Trace recording | `tracing(browser)` | `start()`, `stop()`, `isStarted()` |
| Accessible and test-oriented locators | `locators(browser)` | `getByRole`, `getByText`, `getByTestId`, `getByLabel`, `getByPlaceholder` |
| Navigation and wait states | `page(browser)` | `waitForLoadState`, `waitForURL`, `waitForFunction`, `reload`, `goBack` |
| Keyboard and mouse input | `page(browser)` | `keyboard().press`, `keyboard().type`, `mouse().click`, `mouse().wheel` |
| Cookies and storage state | `context(browser)` | `cookies`, `addCookies`, `clearCookies`, `storageState` |
| Emulation and permissions | `context(browser)` | `setGeolocation`, `grantPermissions`, `clearPermissions`, `setExtraHTTPHeaders` |
| Downloads | `page(browser)` | `waitForDownload`, `onDownload` |
| HAR replay | `context(browser)` | `routeFromHAR`; HAR capture needs Playwright context creation options, which this adapter does not expose |
| Console, page errors, requests, responses | `page(browser)` | `onConsoleMessage`, `onPageError`, `onRequest`, `onResponse` |
| API requests | `context(browser)` | `request()` returns Playwright's `APIRequestContext` |
| JavaScript and CSS coverage | `coverage(browser)` | Not available through Playwright Java 1.61; the helper throws `UnsupportedOperationException` |
| PDF generation | `pdf(browser)` | `page.pdf()` on Chromium pages only |
| Accessibility snapshots | `accessibility(browser)` | `page.ariaSnapshot()` on current Playwright Java |
| Video | `page(browser)` | `video()` after enabling `recordVideo`; save the resulting video after the page or context closes |

The typed helpers preserve the WebDriver element bridge. For example, locator helpers return `List<WebElement>`, so they can be used with ordinary Selenium or Geb code.

```groovy
def network = PlaywrightBrowserSupport.network(browser)
network.route('**/api/**') { route ->
    route.abort()
}

def locators = PlaywrightBrowserSupport.locators(browser)
def submit = locators.getByRole('button', 'Submit').first()
submit.click()
```

## Alerts, windows, and frames

Playwright dialogs are event-driven. The adapter records dialogs observed by the current context so `driver.switchTo().alert()` can return a Selenium `Alert`. Register a Playwright `page.onDialog` or `context.onPage` handler yourself when a test needs exact dialog timing or logic beyond `accept`, `dismiss`, `getText`, and `sendKeys`.

Every Playwright page is represented as a WebDriver window handle. `switchTo().newWindow()` creates a Playwright page, and `switchTo().window(handle)` selects it. Name-based selection is best-effort. Window size, position, fullscreen, and browser-chrome controls are not equivalent to Selenium because Playwright manages pages inside a browser context.

`switchTo().frame(index)`, `frame(nameOrId)`, `frame(element)`, `parentFrame()`, and `defaultContent()` select the current Playwright frame. Use `page.frameLocator(...)` through the raw page facade for Playwright's frame locator API.

## Threading

Playwright Java is not thread-safe. Do not share a `PlaywrightWebDriver`, its `BrowserContext`, or its `Page` between threads. For parallel tests using Geb's implicit driver cache, set `cacheDriverPerThread = true` in `GebConfig.groovy`. Each thread will then receive its own driver and context.

## Test skip flags

The module's browser-backed tests can run without installed browser binaries when either flag is set:

* System property: `-Dgeb.direct.playwright.skip=true`
* Environment variable: `PLAYWRIGHT_SKIP=true`

These flags are for the module test suite. They do not make an application using `geb-direct` work without the required Playwright browsers.

## Limitations compared with full Selenium

Geb Direct is an initial integration focused on the WebDriver paths used by Geb and common CI workloads. It is not a complete replacement for every Selenium driver feature.

* Selenium logging through `driver.manage().logs()` is unsupported. Use Playwright page events for console output and page errors.
* Browser-chrome window management is limited by Playwright's page and context model.
* Alert handling is best-effort because Playwright dialogs are delivered as events.
* Selenium Grid, `RemoteWebDriver`, vendor-specific capabilities, browser profiles, extensions, DevTools APIs, and WebDriver BiDi are not provided by this adapter.
* Use the raw `Page` and `BrowserContext` facades for Playwright-only features. Those calls are not portable to a Selenium-configured Geb run.

## Install browsers

Install the Playwright browser engines needed by the project on every developer machine and CI image before running tests.

```bash
# Node-based Playwright installation
npx playwright install chromium

# Or the Playwright Java CLI, from a project that includes Playwright
java -cp <playwright-jar> com.microsoft.playwright.CLI install chromium
```

Replace `chromium` with `firefox` or `webkit` as needed. Use the same browser installation mechanism in CI, and install operating-system dependencies when the Playwright installation command requires them.
