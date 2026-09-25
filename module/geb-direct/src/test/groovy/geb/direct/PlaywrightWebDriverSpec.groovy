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

import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import geb.direct.support.PlaywrightSpecSupport
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ PlaywrightSpecSupport.skipped() })
class PlaywrightWebDriverSpec extends Specification {
    PlaywrightWebDriver driver

    def cleanup() { driver?.quit() }

    def 'supports WebDriver navigation, element actions, JavaScript, and screenshots'() {
        given:
        driver = PlaywrightDriver.create { headless = true }

        when:
        driver.get('data:text/html,<title>Direct</title><button id="button">Go</button><input name="value" data-testid="value-input" placeholder="Enter value">')
        driver.findElement(By.id('button')).click()
        driver.findElement(By.name('value')).sendKeys('Geb')

        then:
        driver.title == 'Direct'
        driver.findElement(By.name('value')).getAttribute('value') == 'Geb'
        driver.executeScript('return arguments[0].value', driver.findElement(By.name('value'))) == 'Geb'
        driver.getScreenshotAs(OutputType.BYTES).length > 0
        driver.locators.getByRole('button', 'Go').size() == 1
        driver.locators.getByText('Go').size() == 1
        driver.locators.getByTestId('value-input').size() == 1
        driver.locators.getByPlaceholder('Enter value').size() == 1
    }
}
