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

import org.opentest4j.TestAbortedException
import org.openqa.selenium.Dimension
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ System.getProperty('geb.direct.playwright.skip') == 'true' || System.getenv('PLAYWRIGHT_SKIP') == 'true' })
class PlaywrightAlertAndWindowSpec extends Specification {
    PlaywrightWebDriver driver

    def setup() {
        try {
            driver = PlaywrightDriver.create {
                browserType = 'chromium'
                headless = true
            }
        } catch (Exception exception) {
            throw new TestAbortedException("Playwright Chromium is unavailable: ${exception.message}", exception)
        }
    }

    def cleanup() { driver?.quit() }

    def 'accepts a JavaScript alert and exposes its text'() {
        given:
        driver.get('data:text/html,<title>before</title>')

        when:
        driver.executeScript("setTimeout(() => { alert('Hello Geb'); document.title = 'accepted' }, 0)")
        def alert = driver.switchTo().alert()

        then:
        alert.text == 'Hello Geb'

        when:
        alert.accept()

        then:
        driver.title == 'accepted'
    }

    def 'dismisses a JavaScript confirm dialog'() {
        given:
        driver.get('data:text/html,<title>before</title>')

        when:
        driver.executeScript("setTimeout(() => { document.title = confirm('Continue?') ? 'confirmed' : 'dismissed' }, 0)")
        def alert = driver.switchTo().alert()
        alert.dismiss()

        then:
        driver.title == 'dismissed'
    }

    def 'supplies prompt text before accepting a JavaScript prompt'() {
        given:
        driver.get('data:text/html,<title>before</title>')

        when:
        driver.executeScript("setTimeout(() => { document.title = prompt('Name?') }, 0)")
        def alert = driver.switchTo().alert()
        alert.sendKeys('Geb')
        alert.accept()

        then:
        driver.title == 'Geb'
    }

    def 'round trips viewport size through the window manager'() {
        given:
        driver.get('data:text/html,<title>viewport</title>')

        when:
        driver.manage().window().size = new Dimension(640, 480)

        then:
        driver.manage().window().size == new Dimension(640, 480)
    }
}
