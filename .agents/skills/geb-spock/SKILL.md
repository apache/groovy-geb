<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
---
name: geb-spock
description: >
  Write Geb browser automation tests using the Spock framework with best practices.
  Use this skill whenever the user asks to write, generate, fix, or review Geb test specs,
  mentions Geb, GebSpec, GebReportingSpec, or asks about browser automation with Groovy/Spock.
  Also trigger for requests involving Page Objects in a Groovy/Gradle project, `at` checkers,
  Geb content DSL, or `waitFor` usage. Even if the user just says "write me a Geb test" or
  "help me test this page", use this skill.
---

# Geb Testing Skill

Generates and reviews Geb + Spock browser automation specs following intermediate-level best practices.

Geb documentation is at https://groovy.apache.org/geb/manual/current/.

## Core Principles

1. **Always use Page Objects** — never inline selectors in specs.
2. **Use `at` checkers** — every Page Object must declare `static at`.
3. **Prefer CSS selectors** over XPath; avoid brittle nth-child selectors.
4. **Use `waitFor` explicitly** for dynamic content; never `Thread.sleep`.
    1. Strongly prefer to put `waitFor` method calls into methods inside classes that extend `geb.Page`, `geb.Module`, or other shared test fixtures. If clicking or interacting with some object on a page requires waiting for some response, it's best to encapsulate that knowledge into a method on the pageObject (e.g. `deleteTableRow` or `submitForm`) that then waits for the signal that the operation is complete. That will make the knowledge of your page more reusable and avoid repeated `waitFor`.
5. **Use `GebReportingSpec`** (not `GebSpec`) so screenshots are captured on failure.
6. **Keep specs in `given/when/then` blocks** — one behaviour per feature method.
    1. Caveat: if a behaviour requires multiple steps, it's okay to have multiple `when/then` block pairs within one test method, so long as each intermediary step is integral to the journey under test.
7. **Use Modules** for repeated UI components (nav bars, modals, form rows).
8. Avoid writing flaky tests by stress-testing your Geb spec before committing:
    1. Try [introducing network latency](https://groovy.apache.org/geb/manual/current/#controlling-network-conditions) to verify that a test will pass on a slow network.
    2. Try re-running tests many times in succession locally to ensure they aren't flaky

---

## Spec File Structure

```groovy
package com.example.specs

import geb.spock.GebReportingSpec
import com.example.pages.LoginPage
import com.example.pages.DashboardPage
import spock.lang.Stepwise   // only when steps truly depend on each other

@Stepwise                    // omit if steps are independent
class LoginSpec extends GebReportingSpec {

    def "successful login redirects to dashboard"() {
        given: "user is on the login page"
        to LoginPage

        when: "valid credentials are submitted"
        loginForm.username = "user@example.com"
        loginForm.password = "secret"
        loginForm.submit()

        then: "the dashboard is displayed"
        at DashboardPage
        welcomeMessage.text() == "Welcome, user@example.com"
    }

    def "login fails with invalid credentials"() {
        given:
        to LoginPage

        when:
        loginForm.username = "bad@example.com"
        loginForm.password = "wrong"
        loginForm.submit()

        then:
        at LoginPage
        errorMessage.displayed
        errorMessage.text().contains("Invalid credentials")
    }
}
```

---

## Page Object Structure

```groovy
package com.example.pages

import geb.Page
import com.example.modules.LoginFormModule

class LoginPage extends Page {

    // `at` checker — verified when `to` or `at` is called
    static at = { title == "Login | MyApp" }

    // URL for `to LoginPage`
    static url = "/login"

    static content = {
        // Lazy by default; set required: false for optional elements
        loginForm    { module(LoginFormModule) }
        errorMessage(required: false) { $(".alert-error") }
    }
}
```

```groovy
class DashboardPage extends Page {
    static at = { $("h1.dashboard-title").displayed }
    static url = "/dashboard"

    static content = {
        welcomeMessage { $(".welcome-msg") }
        navBar         { module(NavBarModule) }
    }
}
```

---

## Module Structure

Use modules for components that repeat across pages (nav, modal, form row, table row).

```groovy
package com.example.modules

import geb.Module

class LoginFormModule extends Module {

    static content = {
        username { $("input[name='username']") }
        password { $("input[name='password']") }
        submitBtn { $("button[type='submit']") }
    }

    void submit() {
        submitBtn.click()
    }
}
```

---

## `waitFor` Patterns

```groovy
// Wait for element to appear (default timeout from GebConfig)
waitFor { $(".spinner").not(".active") }

// Wait with custom timeout (seconds)
waitFor(10) { successBanner.displayed }

// Wait and return the element
def result = waitFor { $(".result-row") }

// Use in content DSL for elements that load dynamically
static content = {
    // Wait for this element every time it's accessed
    asyncTable(wait: true) { $("table.results") }
}
```

> **Never use `Thread.sleep`**. If `waitFor` keeps timing out, make sure your assertion is actually eventually true (you may be relying on an unreliable side effect), ensure that your page is completing its work as quickly as it can, or increase the timeout in `GebConfig.groovy` rather than sleeping.

---

## Selector Best Practices

| ✅ Prefer                      | ❌ Avoid                                          |
|-------------------------------|--------------------------------------------------|
| `$("input[name='email']")`    | `$("div > div:nth-child(2) > input")`            |
| `$(".submit-btn")`            | `$("button", text: "Submit")` (fragile for i18n) |
| `$("table.results tbody tr")` | XPath selectors                                  |
| `data-testid` attributes      | Position-based selectors                         |

If the app doesn't have `data-testid` attrs, use stable semantic selectors: `name`, `id`, `aria-label`, `role`.

---

## Common Patterns

### Checking a list of items
```groovy
def rows = 
rows.size() == 3
rows[0].find("td.name").text() == "Alice"
```

### Filling a form
```groovy
$("input[name='firstName']").value("Jane")
$("select[name='country']").value("US")
$("input[type='checkbox'][name='agree']").value(true)
```

### Interacting with dropdowns (non-native)
```groovy
// For custom JS dropdowns (not <select>)
$(".dropdown-trigger").click()
waitFor { $(".dropdown-menu").displayed }
$(".dropdown-menu li", text: "Option B").click()
```

### Asserting navigation
```groovy
// Checks URL and `at` checker
at ConfirmationPage
currentUrl.contains("/confirmation")
```

---

## Anti-Patterns to Avoid

- **`@Stepwise` overuse** — only use when steps share state (e.g., a multi-step wizard). Independent scenarios should be separate specs or use `setup()`/`cleanup()`.
- **Assertions inside `when:` blocks** — lines after `then:` or `expect:` are automatically asserted - no need to even supply the `assert` keyword!
- **`sleep()`** anywhere in test code.
- **Hard-coded absolute URLs** — use relative paths; base URL lives in `GebConfig.groovy`.
- **Accessing the DOM directly in specs** — all `$()` calls belong in Page Objects or Modules.
- **Overusing `required: false`** on optional content - the only time you really want to mark a page element as `required: false` is when your spec *needs* to try to interact with it when it's absent (for example, to assert that it isn't present, `!page.buttons.sometimesThereButton`). If the button may or may not be there, but you never test the case where it isn't there, you should just leave it as required. Remember, throwing an exception when something _exceptional_ happens is okay, especially in tests!

### Avoiding creating flaky tests

Browser testing frameworks like Geb sometimes get a bad reputation for being flaky, but with some diligence you can keep your Geb tests more reliable!

#### Introduce Network latency
Adding even a small amount of [network latency](https://groovy.apache.org/geb/manual/current/#controlling-network-conditions) to your test can expose places where you probably want to add a `waitFor` statement to your page object. Drop some code like this in your test's `setup()` method and run it a few times to make sure it consistently works:
```groovy
def networkLatency = java.time.Duration.ofMillis(500)
browser.networkLatency = networkLatency
```

#### Introduce reruns
While developing your tests, try doing a pass of re-running the test locally to make sure it consistently passes. If each of your tests only fails 1 time out of a hundred, but you have ten tests, your build pipelines are going to be flakier than your team can tolerate.

If you're using `geb-spock`, use the [RepeatUntilFailure](https://spockframework.org/spock/docs/2.4/all_in_one.html#_repeat_until_failure) annotation with a `maxAttempts` value to run the test locally. You probably won't want to **commit** that annotation in most cases, but it's a good tool to verify your test is consistent before making it a blocker for someone else's PR.
