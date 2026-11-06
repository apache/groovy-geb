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
package geb.playwright

import com.microsoft.playwright.PlaywrightException
import geb.playwright.support.PlaywrightSpecSupport
import com.sun.net.httpserver.HttpServer
import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.Keys
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.OutputType
import org.openqa.selenium.interactions.Actions
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.time.Duration

@IgnoreIf({ PlaywrightSpecSupport.skipped() })
class PlaywrightWebDriverCoreSpec extends Specification {
    PlaywrightWebDriver driver

    def cleanup() {
        driver?.quit()
    }

    def 'supports navigation, document state, scripts, screenshots, cookies, timeouts, and capabilities'() {
        given:
        HttpServer server = PlaywrightSpecSupport.server { String path, headers -> '<title>Core</title><p id="message">Ready</p>' }
        driver = PlaywrightSpecSupport.driver()
        driver.get(PlaywrightSpecSupport.url(server))
        def initialTimeouts = driver.manage().timeouts()
        long initialImplicitWaitMillis = initialTimeouts.implicitWaitMillis
        long initialScriptTimeoutMillis = initialTimeouts.scriptTimeoutMillis

        when:
        driver.manage().addCookie(new Cookie('direct', 'yes'))
        driver.manage().addCookie(new Cookie.Builder('domain-cookie', 'value').path('/').isHttpOnly(true).expiresOn(new Date(System.currentTimeMillis() + 60000)).build())
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(50))

        then:
        driver.manage().getCookieNamed('direct').value == 'yes'
        driver.manage().cookies*.name.contains('direct')
        driver.manage().getCookieNamed('domain-cookie').httpOnly

        when:
        driver.navigate().to(new URL(PlaywrightSpecSupport.url(server, '/next')))
        driver.navigate().back()
        driver.navigate().forward()
        driver.navigate().refresh()

        then:
        driver.currentUrl.startsWith('http://localhost')
        driver.title == 'Core'
        driver.pageSource.contains('Ready')
        driver.executeScript('return arguments[0].textContent', driver.findElement(By.id('message'))) == 'Ready'
        driver.executeAsyncScript('arguments[arguments.length - 1](4 + 5)') == 9
        driver.getScreenshotAs(OutputType.BYTES).length > 0
        driver.manage().getCookieNamed('direct').value == 'yes'
        initialImplicitWaitMillis == 0
        initialScriptTimeoutMillis == driver.session.options.defaultTimeoutMs
        driver.alertTimeoutMillis == 1000
        driver.capabilities.browserName == 'playwright-chromium'
        driver.windowHandles.contains(driver.windowHandle)

        when:
        driver.manage().deleteCookieNamed('direct')

        then:
        driver.manage().getCookieNamed('direct') == null
        driver.manage().getCookieNamed('domain-cookie').value == 'value'

        when:
        driver.manage().deleteCookie(driver.manage().getCookieNamed('domain-cookie'))

        then:
        driver.manage().getCookieNamed('domain-cookie') == null

        when:
        driver.manage().addCookie(new Cookie('direct', 'yes'))
        driver.manage().deleteAllCookies()

        then:
        driver.manage().cookies.empty

        cleanup:
        server.stop(0)
    }

    def 'reports Selenium logs as unsupported'() {
        given:
        driver = PlaywrightSpecSupport.driver()

        when:
        driver.manage().logs()

        then:
        thrown(UnsupportedOperationException)
    }

    def 'waits for delayed elements and supports async callbacks, numeric ids, and frame ids'() {
        given:
        driver = PlaywrightSpecSupport.driver()
        driver.get('''data:text/html,
            <p id="123">numeric</p>
            <iframe id="frame-id" srcdoc="<p id='inside'>inside</p>"></iframe>
            <script>setTimeout(() => document.body.insertAdjacentHTML('beforeend', '<p id="late">late</p>'), 25)</script>''')
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1))

        when:
        def delayed = driver.findElement(By.id('late'))
        def delayedElements = driver.findElements(By.id('late'))
        def numeric = driver.findElement(By.id('123'))
        def asyncValue = driver.executeAsyncScript('''
            const callback = arguments[arguments.length - 1]
            setTimeout(() => callback(arguments[0] + 1), 10)
        ''', 4)
        driver.switchTo().frame('frame-id')

        then:
        delayed.text == 'late'
        delayedElements*.text == ['late']
        numeric.text == 'numeric'
        asyncValue == 5
        driver.findElement(By.id('inside')).text == 'inside'
    }

    def 'adapts WebElement interactions and all semantic locator strategies'() {
        given:
        driver = PlaywrightSpecSupport.driver()
        driver.get('''data:text/html,
<title>Elements</title><label for="field">Label</label><input id="field" name="field" placeholder="Placeholder" data-testid="field-id">
<input id="check" type="checkbox"><select><option id="one" value="one" selected>one</option><option id="two" value="two">two</option></select><button id="button" title="Button title">Click me</button>
<div id="outer"><span class="child">Nested</span></div><img alt="Logo" src="x">
<form onsubmit="event.preventDefault(); document.title='submitted'"><button id="submit" type="submit">submit</button></form>''')
        def field = driver.findElement(By.id('field'))

        when:
        field.sendKeys('Geb')
        driver.findElement(By.id('submit')).submit()
        driver.findElement(By.id('check')).click()
        driver.findElement(By.id('two')).click()
        new Actions(driver).moveToElement(field).click().sendKeys(' Direct').perform()

        then:
        driver.title == 'submitted'
        field.getAttribute('value') == 'Geb Direct'
        field.getDomAttribute('data-testid') == 'field-id'
        field.getDomProperty('value') == 'Geb Direct'
        field.tagName == 'input'
        field.displayed
        field.enabled
        field.size.width > 0
        driver.findElement(By.id('check')).selected
        !driver.findElement(By.id('one')).selected
        driver.findElement(By.id('two')).selected
        driver.findElement(By.id('outer')).findElement(By.className('child')).text == 'Nested'
        field.location.x >= 0
        field.rect.width == field.size.width
        field.getCssValue('display')
        field.getScreenshotAs(OutputType.BYTES).length > 0
        driver.switchTo().activeElement().getAttribute('id') == 'field'

        when:
        field.clear()
        driver.findElement(By.id('outer')).findElement(By.className('missing'))

        then:
        thrown(NoSuchElementException)
        field.getAttribute('value') == ''
    }

    def 'sends text around Enter and reports selected options'() {
        given:
        driver = PlaywrightSpecSupport.driver()
        driver.get('''data:text/html,
            <input id="field" onkeydown="if (event.key === 'Enter') this.dataset.enterCount = Number(this.dataset.enterCount || 0) + 1">
            <select id="select"><option id="one" value="one" selected>one</option><option id="two" value="two">two</option></select>''')
        def field = driver.findElement(By.id('field'))

        when:
        field.sendKeys('before', Keys.ENTER, 'after')
        driver.findElement(By.id('two')).click()

        then:
        field.getAttribute('value') == 'beforeafter'
        field.getAttribute('data-enter-count') == '1'
        !driver.findElement(By.id('one')).selected
        driver.findElement(By.id('two')).selected
    }

    def 'keeps Playwright action timeout separate from implicit wait and times out async scripts'() {
        given:
        driver = PlaywrightSpecSupport.driver {
            defaultTimeoutMs = 200
        }
        driver.get('''data:text/html,<button id="blocked" style="pointer-events: none">blocked</button>''')
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(10))
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(25))

        when:
        long clickStartedAt = System.nanoTime()
        driver.findElement(By.id('blocked')).click()

        then:
        thrown(PlaywrightException)
        Duration.ofNanos(System.nanoTime() - clickStartedAt).toMillis() >= 100

        when:
        driver.executeAsyncScript('')

        then:
        def timeout = thrown(PlaywrightException)
        timeout.message.contains('script timeout after 25ms')
    }

    def 'maps supported WebDriver keys from enum values and unicode characters'() {
        expect:
        PlaywrightWebElement.playwrightKey(Keys.ENTER) == 'Enter'
        PlaywrightWebElement.playwrightKey(Keys.TAB) == 'Tab'
        PlaywrightWebElement.playwrightKey(Keys.ESCAPE) == 'Escape'
        PlaywrightWebElement.playwrightKey(Keys.BACK_SPACE) == 'Backspace'
        PlaywrightWebElement.playwrightKey(Keys.DELETE) == 'Delete'
        PlaywrightWebElement.playwrightKey(Keys.SPACE) == ' '
        PlaywrightWebElement.playwrightKey(Keys.ARROW_UP) == 'ArrowUp'
        PlaywrightWebElement.playwrightKey(Keys.CONTROL) == 'Control'
        PlaywrightWebElement.playwrightKey(Keys.NULL) == 'Null'
        Keys.getKeyFromUnicode(Keys.ENTER.toString().charAt(0)) == Keys.ENTER
    }

    def 'sendKeys holds modifiers for chords and types replacement text'() {
        given:
        HttpServer server = PlaywrightSpecSupport.server { String path, headers ->
            '<input id="name" name="name" value="">'
        }
        driver = PlaywrightSpecSupport.driver()
        driver.get(PlaywrightSpecSupport.url(server))
        def input = driver.findElement(By.cssSelector('#name'))
        def metaKey = System.getProperty('os.name').toLowerCase().contains('mac') ? Keys.META : Keys.CONTROL

        when:
        input.sendKeys('hello')
        input.sendKeys(Keys.chord(metaKey, 'a'))
        input.sendKeys(Keys.BACK_SPACE)
        input.sendKeys('Geb')

        then:
        input.getDomProperty('value') == 'Geb' || input.getAttribute('value') == 'Geb'

        cleanup:
        server.stop(0)
    }
}
