# Design Plan: `geb-playwright` — an auto-waiting Playwright backend for Geb

Status: DRAFT (design document, not yet scheduled)
Author: James Daugherty
Date: 2026-07-11

## 1. Summary

Explore and, if the spike validates the required seams, add an optional module
that lets Geb drive browsers through [Playwright for Java](https://playwright.dev/java/)
instead of Selenium WebDriver. The goal is
to give Geb users Playwright's reliability model — auto-waiting, atomic
"check-and-act" interactions, strict re-resolving locators — so that most
hand-written `waitFor {}` blocks become unnecessary, while keeping Geb's
Page/Module/content DSL unchanged.

```groovy
// GebConfig.groovy — the whole user-facing switch
driver = "playwright:chromium"     // advanced configuration uses a geb-playwright builder
```

## 2. Background and a corrected premise

### 2.1 What Playwright actually does

The original idea framed Playwright's
[actionability](https://playwright.dev/docs/actionability) as "reactive
events" vs. Selenium's polling. Having read the Playwright source, the real
architecture is subtler and matters for our design:

- Every action (`click`, `fill`, …) runs an **actionability check loop**
  (visible, stable, receives-events, enabled, editable) **inside the browser**
  via an injected script. "Stable" is measured across consecutive
  `requestAnimationFrame` callbacks — synchronized with the browser's own
  render loop rather than a client-side timer.
- The retry loop lives in the Playwright driver process with progressive
  backoff, and — critically — **the check and the action are atomic within one
  protocol call**. There is no wire round-trip between "element is visible"
  and "click it", which is exactly the race window where Selenium tests flake.
- `Locator` objects are *queries*, re-resolved from the live DOM on every
  action, and strict by default (multiple matches on a single-element action
  throw). Geb's `Navigator`, by contrast, eagerly captures `WebElement`
  handles that can go stale.
- True *event* subscriptions (network, console, page lifecycle, dialogs) exist
  as a separate, additive capability.

So the deliverable is not "events instead of polling" — it is **moving the
wait loop from Geb's JVM-side `Thread.sleep` polling
(`geb.waiting.Wait.waitFor`) into the browser/driver, fused atomically with
the action**. That is what eliminates hard-coded `waitFor` in practice.

### 2.2 Wire protocol reality

Playwright clients speak Playwright's own JSON-RPC protocol to a bundled
**Node.js driver process** (Chromium via CDP; Firefox via the custom "Juggler"
protocol; WebKit via an extended Inspector protocol, both on patched browser
builds). There is no pure-JVM implementation of that server. Consequences:

- `geb-playwright` inherits a Node child-process runtime dependency
  (transparent to users — `com.microsoft.playwright:playwright` bundles it).
- Remote execution uses `BrowserType.connect()` (Playwright protocol) or
  `connectOverCDP()` (reduced fidelity). Selenium Grid interop is
  experimental/Chromium-only and officially at risk — treat as unsupported.
- Code-verified (playwright-java main, July 2026): the JVM client **never
  opens a network connection itself** — `Playwright.create()` spawns
  `node cli.js run-driver` and speaks length-prefixed JSON over its stdio
  (`impl.PipeTransport`/`Connection`); `connect()`/`connectOverCDP()` are
  performed *by the driver* and tunneled back through the same pipe
  (`impl.JsonPipe`). So the local Node process is required even for
  pure-remote use. Message dispatch is **caller-thread pumped**
  (`ChannelOwner.runUntil` → `processOneMessage()` loop): events are only
  delivered while the owning thread is inside a Playwright call — the
  mechanical basis for §6.4's event-loop-pumping Waiter and §6.5's
  thread-confinement rules. There is no public transport/connection SPI
  (pluggable transport declined in playwright-java#1446); the only extension
  point (`playwright.driver.impl` sysprop) controls driver *location*.
  CI/packaging knobs: `PLAYWRIGHT_DRIVER_DIR` (pre-extracted driver, skips
  ~100 MB temp extraction), `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD`,
  `PLAYWRIGHT_BROWSERS_PATH`, `LaunchOptions.setChannel`/`setExecutablePath`
  for system browsers. `connect()` requires matching client/server
  major.minor versions.

### 2.3 Why not WebDriver BiDi inside Selenium instead?

Selenium 4's BiDi gives event subscriptions (console, network, DOM mutation)
but **the spec has no actionability semantics** — no visible/stable checks, no
atomic check-and-act. We would have to reimplement Playwright's injected
script and retry loop ourselves over `script.callFunction`, on APIs Selenium
still marks internal. BiDi is worth tracking (see §10) but does not deliver
the goal today. Selenium's CDP path has the well-known
browser-version-pinning treadmill and is documented as temporary.

### 2.4 Where Geb is coupled to WebDriver today

From an architecture audit of this repo (July 2026):

- 42 files in `geb-core` import `org.openqa.selenium.*`.
- `geb.navigator.Navigator` has **112 methods**; ~21 expose or assume Selenium
  types (`allElements()`, `firstElement()`, `add(By)`, `has(By)`, …).
- `geb.navigator.DefaultNavigator` (~1,140 lines) is the **single interaction
  choke point** — all `click()`/`value()`/`<<` calls dispatch to `WebElement`
  there, already bracketed by `NavigatorEventListener` hooks.
- Element lookup funnels through `SearchContextBasedBasicLocator`
  (`searchContext.findElements(by)`), wired in `BrowserBackedNavigatorFactory`.
- Polling lives in `geb-waiting`'s `geb.waiting.Wait.waitFor` — a
  `while` loop with `Thread.sleep(retryInterval)` — used by explicit
  `waitFor {}`, `atCheckWaiting`, `baseNavigatorWaiting`, and content-template
  `wait:`/`required:` options.
- The documented SPI (`InnerNavigatorFactory`, `navigatorFactory` config) lets
  you swap the `Navigator` *class* but its signatures are hard-typed to
  `Iterable<WebElement>` — insufficient for a non-Selenium backend as-is.
- `geb-waiting` itself has **no Selenium dependency** — good news.
- Prior art elsewhere: [Playwrightium](https://github.com/britka/playwrightium)
  (a Java `WebDriver` facade over Playwright, built for Selenide) proves an
  adapter is feasible and documents the impedance mismatches.

## 3. Goals and non-goals

### Goals

1. A published `geb-playwright` module: suites using the backend-neutral subset
   of Geb switch by changing `driver` config, with no changes to Pages, Modules,
   or content DSL. Selenium escape hatches and documented semantic deltas may
   require migration.
2. Interactions (`click()`, `value()`, `<<`, module form controls) get
   Playwright auto-waiting: no `waitFor` needed for "element not there yet /
   still animating / covered by overlay" cases.
3. `waitFor {}`, `at` checking, and content `wait:`/`required:` keep working
   with identical semantics (they may still poll — see §6.4 — but user code is
   unchanged).
4. Expose Playwright-only value-adds behind Geb-flavored APIs: network
   routing/interception, console/page-error capture, tracing, and built-in
   shadow-DOM piercing.
5. Keep existing Selenium source and runtime behavior backward compatible.
   Selenium remains the default backend; new internal dispatch may be a
   refactor rather than strictly additive.

### Non-goals

- Replacing Selenium. Cloud-grid ecosystems (SauceLabs/BrowserStack/LambdaTest
  via `geb-gradle`) remain Selenium-first; `geb-playwright` targets local +
  containerized + `BrowserType.connect()` remote execution.
- Emulating 100% of the `Navigator` API. Methods whose *signature* is a
  Selenium type (`allElements()`, `add(By)`) are supported only via an opt-in
  interop shim (§6.6) or throw `UnsupportedOperationException` with a clear
  message.
- Supporting Playwright's async patterns. Playwright Java is synchronous;
  so is Geb. Good fit.
- Building on raw CDP or BiDi ourselves (revisit later, §10).

## 4. Architecture decision

Three options were considered:

| | Option A — Playwright-as-WebDriver adapter (Playwrightium-style) | Option B — abstract element type threaded through geb-core | Option C — backend SPI at the NavigatorFactory seam (chosen) |
|---|---|---|---|
| Shape | Implement `WebDriver`/`WebElement` over Playwright; inject via existing `driver` closure | New `GebElement` abstraction replaces `WebElement` throughout `Navigator`/`Locator`/factories | Generalize the *factory and dispatch* seam; ship a parallel `PlaywrightNavigator` implementing the existing `Navigator` interface |
| Core changes | none | very large, breaks public API (`Navigator` leaks `WebElement`) | moderate but cross-cutting; preserves existing public signatures |
| Achieves auto-waiting | **No** — `DefaultNavigator` + `Wait` polling still drive everything; ElementHandle semantics forfeit Locator auto-retry | Yes | Yes |
| Risk | low effort, misses the point | multi-release breaking migration | contained; the ~21 Selenium-typed `Navigator` methods handled by shim/unsupported |

**Provisional choice: Option C**, subject to P0 proving that the common API can
be implemented without pervasive backend branches. Option B's abstraction is
introduced *minimally* where the seams require it, and Option A's shim reused *narrowly*
for the Selenium-typed leftovers of the `Navigator` interface (§6.6). Option B
in full is the right eventual destination for a Geb major version, and Option
C's interfaces are designed to become that migration's first step.

## 5. Module layout

```
module/geb-playwright/                      # new published module
  geb-playwright.gradle                     # applies geb.api-module (+ geb.dockerised-test)
  src/main/groovy/geb/playwright/
    PlaywrightBackendProvider.groovy        # driver-string discovery + typed builder
    PlaywrightBrowserBackend.groovy         # Browser capabilities, lifecycle, ownership
    navigator/
      PlaywrightNavigator.groovy            # implements geb.navigator.Navigator over Locator
      PlaywrightNavigatorFactory.groovy     # implements geb.navigator.factory.NavigatorFactory
      PlaywrightQuery.groovy                # immutable page/frame/locator query plan
      SelectorTranslator.groovy             # Geb $/By/CssSelector -> Playwright selector strings
    waiting/
      PlaywrightWaitingSupport.groovy       # waitFor bridged to PW clocks (§6.4)
    interop/
      WebElementShim.groovy                 # opt-in ElementHandle->WebElement bridge (§6.6)
    events/
      NetworkSupport.groovy, ConsoleSupport.groovy, TracingSupport.groovy
  src/test/...                              # cross-cutting spec suite (§8)
```

`geb-core` gains backend-neutral seams (§6.1). Dependency
direction: `geb-playwright` → `geb-core`; `geb-core` gains **no** Playwright
dependency. Version added to `gradle/libs.versions.toml`
(`com.microsoft.playwright:playwright`, Apache-2.0, currently 1.61.x).

## 6. Design details

### 6.1 New seams in geb-core (Selenium-neutral)

The blocker is broader than `NavigatorFactory`: `Browser`, `Page`, driver
creation/caching, frames, interactions, JavaScript, reporting, downloads, and
navigation call WebDriver directly, while `NavigatorFactory`,
`InnerNavigatorFactory`, `BasicLocator`, and `NavigableSupport` are hard-typed
to `WebElement`/`By`/`SearchContext`/`WebDriver.TargetLocator`. P0 must produce
a call-site inventory and validate the following candidate seams:

1. **`geb.driver.BrowserBackend`** (name TBD): a capability-oriented internal
   interface for navigation, lifecycle, pages/windows, frames, cookies,
   JavaScript, screenshots, interactions, downloads, and active-element
   lookup. Avoid one large WebDriver-shaped interface: optional capabilities
   should fail through one consistent unsupported-capability exception.
   `Browser.getDriver()` keeps returning `WebDriver` for Selenium and throws a
   descriptive error under Playwright; a separate `getBackend()`/`backendAs()`
   escape hatch exposes backend-specific APIs without pretending Playwright is
   WebDriver. Existing `setDriver(WebDriver)` remains unchanged.
2. **Backend discovery and creation**: `Configuration` currently accepts only
   WebDriver instances/factories and knows a fixed set of driver short names.
   Define a small provider SPI loaded with `ServiceLoader` (or an explicit
   equivalent) that claims names such as `playwright:chromium`, creates the
   backend, and supplies its navigator/waiting implementations. Define
   duplicate-provider and unknown-option errors. `geb-core` must not refer to
   Playwright classes. `Configuration.createNavigatorFactory(Browser)` remains
   the navigator injection point, but is not by itself a registration
   mechanism.
3. **`NavigableSupport`**: replace the constructor's
   `WebDriver.TargetLocator` parameter with a small `FocusTracker` interface
   (Selenium impl delegates to `switchTo().activeElement()`).
4. **Waiting seam**: introduce a `geb.waiting.Waiter` interface extracted from
   `Wait` (same contract: `waitFor(Closure)` honoring timeout/interval and
   throwing `WaitTimeoutException`); `Configuration` gains a `waiterFactory`
   so a backend can substitute the implementation (§6.4).

Existing Selenium public signatures and behavior remain unchanged, but the
internal routing needed to reach these seams is a material refactor. P1's
acceptance criterion is behavioral and binary compatibility, verified with
the full suite and a public-API compatibility check, not an “additive-only”
claim. These interfaces are deliberately the seed of the eventual Option-B
refactor.

### 6.2 The Navigator mapping — the heart of the design

`PlaywrightNavigator` implements `geb.navigator.Navigator` backed by an
immutable **query plan**, materialized as a `Locator` for each operation (never
an `ElementHandle`, except at the interop/snapshot boundary). The query plan
must also represent page roots and frame roots because `FrameLocator` is not a
`Locator`. This preserves re-resolution and auto-waiting without forcing
backend types into `geb-core`.

| Geb | Playwright | Notes |
|---|---|---|
| `$("css")`, content lookup | `page.locator(css)` | lazy — **no element resolution at `$` time**, unlike today (§6.3) |
| chained `find` | `locator.locator(css)` | |
| `$("css", 2)`, `first()`, `last()` | `locator.nth(n)/first()/last()` | |
| `filter`/`not`/text predicates | CSS, `locator.filter(...)`, query-plan composition | Compile only predicates Playwright natively supports. Playwright has no arbitrary function-valued locator filter; unsupported regex/closure semantics require a lazy re-query plus JVM-side filtering, or become a documented P0 blocker. |
| `click()` | `locator.click()` | full actionability: visible+stable+receives-events+enabled |
| `value(v)` | `fill()` / `selectOption()` / `setChecked()` by element type | mirrors `DefaultNavigator.setInputValue` dispatch table |
| `<<` (sendKeys) | `pressSequentially()` / `press()` | `geb.Keys` translated |
| `displayed`, `text()`, `attr()` | `isVisible()`, `textContent()/innerText()`, `getAttribute()` | preserve Geb's exact semantics (e.g. Geb `text()` uses WebDriver's visible-text rules — closest is `innerText()`; document deltas) |
| `moduleBase`, frames | query-plan root + `FrameLocator` composition | Frame switching is scoped rather than global; P0 must verify nested frames and restoration semantics. |
| `interact { }` DSL | `page.mouse()` / `page.keyboard()` | port `InteractDelegate` |
| shadow DOM | CSS/text/role engines pierce open shadow roots | XPath does not pierce shadow roots; document the selector-dependent delta. |
| `size()`, `isEmpty()` | `locator.count()` | forces resolution — fine, these are queries |

**Strictness policy**: Geb semantics are collection-like and differ by method.
P0 must derive a method-by-method cardinality table from `DefaultNavigator` and
tests rather than applying one global rule. Compatibility mode applies
`first()` only where Geb currently uses `firstElement()` and snapshots the
current match set for genuinely multi-element operations. An opt-in strict
mode rejects ambiguous single-element operations. The exact behavior and
exception type are conformance-tested.

### 6.3 Laziness is a visible semantic change

Today `$("div")` resolves `WebElement`s eagerly; `PlaywrightNavigator` is a
lazy query. Consequences to spec and document:

- `isEmpty()`/`size()` evaluated at call time, not `$` time — usually what
  users wanted anyway.
- Stale-element errors largely disappear (locators re-resolve); the
  `StaleElementReferenceException` handling paths become dead under this
  backend.
- Do not add `Navigator.snapshot()` until P0 identifies a concrete common-path
  use case and semantics. An `ElementHandle` escape hatch belongs in the
  Playwright-specific API; the interop shim handles Selenium-typed callers.

### 6.4 Waiting semantics

- **`waitFor { ... }` (arbitrary Groovy block)**: cannot be pushed into the
  browser — the condition is JVM-side Groovy. It remains a poll loop, but the
  Playwright `Waiter` implementation must **pump the Playwright event loop**
  between attempts (Playwright Java only dispatches events while its message
  loop runs — `Thread.sleep` would starve page/console/network handlers).
  Implementation: replace the sleep with `page.waitForTimeout(interval)`.
- **Idiomatic replacement**: most `waitFor { thing.displayed }` becomes
  unnecessary because the next action waits. P0 should evaluate a
  backend-neutral `Navigator.waitFor(state)` API against existing Geb waiting
  semantics before adding it; a Playwright-specific form can map directly to
  `Locator.waitFor(VISIBLE/ATTACHED/...)` without prematurely expanding the
  core interface.
- **`atCheckWaiting` / content `wait:` / `required:`**: unchanged contract,
  running on the new `Waiter`. `RequiredPageContentNotPresent` semantics
  preserved.
- **Timeout policy**: the Geb default timeout may initialize Playwright's
  action timeout after seconds-to-milliseconds conversion. `retryInterval`
  applies only to JVM polling. Per-call Geb waits must not mutate a shared
  context default. Navigation timeout remains separate unless explicitly
  configured. Add tests for precedence among Geb defaults, backend config,
  and Playwright per-operation options.

### 6.5 Browser lifecycle and threading

Playwright Java is **not thread-safe**: all objects must be used from the
thread that created the `Playwright` instance. Geb's `GebTestManager` already
holds a `ThreadLocal<Browser>`, so the natural alignment is **one `Playwright`
instance per Geb `Browser`, created lazily on first use and confined to that
thread**. Design rules:

- `PlaywrightBrowserAdapter` records its owning thread and fails fast with a
  clear message on cross-thread access (better than Playwright's own error).
- `CachingDriverFactory`'s cross-test driver caching works per-thread already;
  global (cross-thread) caching mode is disallowed for this backend.
- Spock/JUnit parallel execution: supported at "one browser per thread"
  granularity — same as today. Document that a shared global browser is not
  possible with Playwright.
- Preserve the distinction between `clearCookies()` and browser/test reset:
  cookie clearing calls `BrowserContext.clearCookies()` and must not erase
  local storage. A configured full-isolation reset may dispose and recreate an
  owned context. Externally supplied contexts/pages are never closed unless
  ownership was explicitly transferred. `quit()` closes only resources the
  backend owns; P0 must define ownership for launch, connect, and advanced
  builder modes.

### 6.6 Selenium-typed API surface: the interop shim

~21 `Navigator` methods plus `Browser.getDriver()`/`Page.getDriver()` expose
Selenium types. Policy:

- **Default**: throw `UnsupportedOperationException("<method> requires the
  Selenium backend; under geb-playwright use <alternative>")`. Loud and
  documented.
- **Opt-in shim** (`playwright { seleniumInterop = true }`): wrap Playwright
  `ElementHandle`s in a read-mostly `WebElement` implementation (Playwrightium
  demonstrates feasibility) so `allElements()`, third-party helpers, and
  `add(By)` keep working — with documented loss of auto-retry on those paths.
  Ship in a later phase (P4); it's a compatibility crutch, not the product.
- `By` selectors: translate `By.cssSelector`/`By.id`/`By.name`/`By.xpath` to
  Playwright selector strings in `SelectorTranslator` (Geb already compiles
  attribute maps to CSS via jodd — reuse that path). Exotic `By` subtypes are
  unsupported.

### 6.7 Cross-cutting features

| Geb feature | Playwright mapping |
|---|---|
| `js` object / `JavascriptInterface` | `page.evaluate()` — arg/return marshaling mirrors `JavascriptExecutor` rules |
| Reporting / `report()` screenshots | `page.screenshot()`; add optional full-page mode |
| `withWindow`/`withNewWindow` | `Page` objects per window + `context.waitForPage()` — a *better* fit than window handles |
| Downloads (`DefaultDownloadSupport` does direct HTTP) | `page.waitForDownload()` — real browser downloads, an upgrade; keep API shape |
| Driver-string config | `"playwright:chromium"`, `"playwright:firefox"`, `"playwright:webkit"`; closure form receives a builder for `launch()`/`connect()`/`connectOverCDP()` options |
| New: network interception | `context.route(...)` exposed as `browser.network { route(...) }` (Geb-flavored DSL, phase P5) |
| New: console/page-error capture | auto-attach; surface in test reports on failure |
| New: tracing | `context.tracing()` start/stop around tests, wired into `GebTestManager` reporting hooks |

## 7. Implementation phases

P0 is throwaway. Each subsequent phase is independently mergeable and
CI-green, with no requirement to continue if its exit criteria fail.

**P0 — Architecture spike and decision record (throwaway)**
Prototype the complete vertical slice: backend discovery from
`"playwright:chromium"`, owned lifecycle, navigation, `$`, one nested module,
one frame, `click`, `value`, `text`, `displayed`, JavaScript, screenshot, and
`waitFor`. Run a named representative subset of geb-core specs against it.
Produce a call-site inventory, Navigator method/cardinality matrix, selector
and predicate delta list, lifecycle ownership table, and measured list of core
files changed. Exit criteria: maintainers explicitly accept Option C, or switch
to Option B/stop. No production SPI is merged before this decision.

**P1 — geb-core seams**
Implement only the seams accepted by P0 (`BrowserBackend`, provider discovery,
`FocusTracker`, and `Waiter` are candidates, not pre-approved API). This is a
behavior-preserving refactor for Selenium users. Exit criteria: full existing
suite passes unchanged, a binary/API compatibility check reports no unintended
breaks, unknown/duplicate backend provider errors are tested, and maintainers
review the public surface before P2.

**P2 — geb-playwright core**
Module skeleton, backend provider (driver strings + typed builder),
`PlaywrightNavigator`/`PlaywrightNavigatorFactory`/`SelectorTranslator`,
browser adapter (navigation, title/url/source, cookies, owned lifecycle, js
evaluation, screenshots), threading guards (§6.5). Exit criteria: the
cross-backend conformance suite (§8) passes for navigation, content DSL,
pages, modules, form controls.

**P3 — waiting + at-checking + frames/windows**
Playwright `Waiter` (event-loop-pumping), `atCheckWaiting`, content
`wait:`/`required:`, `withFrame`, `withWindow`/`withNewWindow`, `interact {}`.
Exit criteria: geb-core's waiting and frame spec suites pass under the new
backend (modulo a documented exclusion list).

**P4 — test infrastructure + optional compatibility**
`GebTestManager` integration verified for
spock/junit5/testng modules; containerized test tasks
(`geb.dockerised-test` equivalents — Playwright's own Docker images or
Testcontainers); flaky-page torture fixtures (§8.3). Implement
`WebElementShim` only if P0/P2 usage evidence justifies its maintenance and
semantic cost; it is not a release blocker for an experimental backend.

**P5 — value-adds + docs + release**
Network routing DSL, console capture into reports, tracing hooks. Manual
chapter (new `022-playwright.adoc` beside `021-driver.adoc`), configuration
docs in `060-configuration.adoc`, runnable snippets in `doc/manual-snippets`,
migration guide ("which waitFor calls you can delete"). Release as
incubating/experimental in the next minor version.

## 8. Testing strategy

1. **Cross-backend conformance suite**: extract the behavioral core of
   geb-core's spec suite into shared spec bases (in `internal:test-support`)
   parameterized by backend, so Selenium and Playwright run *the same specs*.
   This is the single most important quality lever and also protects the P1
   refactor.
2. **Delta specs**: explicit tests for documented semantic differences
   (laziness §6.3, strictness, `text()` rules, unsupported methods' error
   messages).
3. **Flakiness torture fixtures**: pages with delayed rendering, CSS
   animations, overlays that disappear, elements detached/re-attached — assert
   the Playwright backend needs no `waitFor` where the Selenium backend does.
   These become the marketing demo, too.
4. **Threading specs**: cross-thread access fails fast; parallel Spock
   execution with per-thread browsers works.
5. **CI**: extend `.github/workflows` with a `geb-playwright` job (Playwright
   installs its own browsers; Linux runners need `playwright install --with-deps`
   or the MS Playwright Docker image). Keep it out of the cloud-browser
   matrices.
6. **Lifecycle and capability specs**: launch/connect ownership, externally
   supplied resource handling, context reset versus cookie clearing, browser
   crash/failed startup cleanup, unsupported-capability errors, and provider
   discovery conflicts.

## 9. Risks and mitigations

| Risk | Mitigation |
|---|---|
| `Navigator` interface too WebDriver-shaped; unsupported methods frustrate users | P0 spike quantifies real usage; interop shim (P4); loud, documented errors with alternatives |
| Playwright single-thread constraint conflicts with a user's harness | fail-fast guards, docs; per-thread model matches `GebTestManager` today |
| `waitFor {}` blocks that call Playwright objects from inside the poll must run on the owning thread | Waiter pumps the PW event loop on the same thread — conditions run inline; add spec coverage |
| Semantic drift between backends silently breaks page objects on switch | conformance suite (§8.1) + published delta list |
| Node child process in restricted CI environments | document; Playwright Docker image path; this is table stakes for every Playwright Java user |
| Playwright protocol requires client/server version match for `connect()` | pin and document; version catalog entry |
| Selenium Grid / cloud vendors unavailable | explicit non-goal; Playwright `connect()` to `launchServer` for remoting |
| Backend routing refactor destabilizes Selenium users | Gate P1 on the full suite, API/binary compatibility check, and separate maintainer review |
| Service/provider discovery is ambiguous or fails in shaded/module-path deployments | Specify classloader behavior; test classpath, module path if supported, duplicate providers, and missing module errors |
| Advanced configuration leaks or double-closes Playwright resources | Typed builder plus an explicit ownership table; lifecycle failure-path specs |
| Some Geb traversal/filter semantics require eager handles and lose auto-waiting | P0 method matrix; document per-method guarantees; treat common-path violations as an architecture stop condition |

## 10. Deferred / future directions

- **Embedded driver via Javet**: `playwright-core` is pure JavaScript, so the
  Playwright driver could run inside an embedded Node runtime
  ([Javet](https://github.com/caoccao/Javet), as previously used to run the
  Sass compiler in the Grails asset-pipeline) instead of the bundled child
  process. This removes only the driver
  process (browsers are separate processes regardless) and does not change the
  auto-waiting behavior; the gains are packaging (no bundled-node extraction in
  locked-down CI) and JVM-tied driver lifecycle. The blocker is that
  playwright-java's `Connection`/`Transport` are internal (constructors
  package-private, nothing public accepts a `Transport`) and a pluggable
  transport was explicitly declined by the maintainers
  (playwright-java#1446, closed not-planned) — its public hooks
  (`playwright.cli.dir`, `PLAYWRIGHT_NODEJS_PATH`) still spawn a child
  process — so an in-memory Java↔V8 transport means forking or
  reimplementing the protocol client, which Microsoft version-locks per
  release. Also stacks a
  second event loop (Javet pumping) on the §6.5 threading constraints, and
  requires Javet's embedded Node LTS to satisfy playwright-core's minimum.
  **Design accommodation now**: P2's `PlaywrightBackendProvider` must isolate
  "obtain a connected `Playwright` instance" behind a single provider
  interface so an embedded-driver transport can slot in later without
  touching the Navigator layer.
- **Full Option B**: retype `Navigator` around a backend-neutral element
  abstraction in a Geb major release; P1's interfaces are the first step.
- **WebDriver BiDi backend**: when the spec gains traction and Selenium's
  BiDi Java API stabilizes (currently marked internal), a `geb-bidi` module
  could add event-driven waits *within* the Selenium ecosystem — potentially
  even retrofitting an injected actionability script. Re-evaluate yearly.
- **Auto-retrying assertions**: Geb-flavored wrapper over
  `PlaywrightAssertions.assertThat(locator)` to complement implicit
  assertions.
- **Playwright test artifacts**: video recording, HAR capture via the
  reporting hooks.

## 11. References

- Actionability: https://playwright.dev/docs/actionability
- Retry/injected-script internals: `packages/playwright-core/src/server/dom.ts`,
  `.../injected/injectedScript.ts` (microsoft/playwright)
- Playwright Java threading: https://playwright.dev/java/docs/multithreading
- BrowserType connect/CDP: https://playwright.dev/java/docs/api/class-browsertype
- Selenium BiDi status: https://www.selenium.dev/documentation/webdriver/bidi/ ;
  internal-API warning: https://www.selenium.dev/documentation/warnings/bidi-implementation/
- Prior art: https://github.com/britka/playwrightium ; Selenide waiting model:
  https://selenide.org/javadoc/current/com/codeborne/selenide/Configuration.html
- Geb seams (this repo): `geb.Configuration#createNavigatorFactory`,
  `geb.navigator.factory.InnerNavigatorFactory`,
  `geb.navigator.DefaultNavigator`, `geb.waiting.Wait#waitFor`,
  `geb.content.NavigableSupport`, `geb.test.GebTestManager`
