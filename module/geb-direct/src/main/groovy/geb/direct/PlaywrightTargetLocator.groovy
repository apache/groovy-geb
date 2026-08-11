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

import com.microsoft.playwright.Dialog
import org.openqa.selenium.*

class PlaywrightTargetLocator implements WebDriver.TargetLocator {
    final PlaywrightWebDriver driver
    PlaywrightTargetLocator(PlaywrightWebDriver driver) { this.driver = driver }

    WebDriver frame(int index) { driver.currentFrame = driver.frame.childFrames()[index]; driver }
    WebDriver frame(String nameOrId) {
        driver.currentFrame = driver.frame.childFrames().find { frame ->
            frame.name() == nameOrId || frame.frameElement()?.getAttribute('id') == nameOrId
        }
        if (!driver.currentFrame) {
            throw new NoSuchFrameException(nameOrId)
        }
        driver
    }
    WebDriver frame(WebElement frameElement) {
        driver.currentFrame = ((PlaywrightWebElement) frameElement).handle().contentFrame()
        if (!driver.currentFrame) {
            throw new NoSuchFrameException('Element is not a frame')
        }
        driver
    }
    WebDriver parentFrame() { driver.currentFrame = driver.frame.parentFrame(); driver }
    WebDriver defaultContent() { driver.currentFrame = null; driver }
    WebElement activeElement() { new PlaywrightWebElement(driver, driver.frame.locator(':focus').first()) }
    Alert alert() {
        Dialog dialog = driver.session.waitForDialog(driver.page, driver.alertTimeoutMillis)
        if (!dialog) {
            throw new NoAlertPresentException()
        }
        new PlaywrightAlert(this, dialog)
    }
    WebDriver window(String nameOrHandle) {
        def page = driver.pageFor(nameOrHandle) ?: driver.context.pages().find { it.evaluate('() => window.name') == nameOrHandle }
        if (!page) {
            throw new NoSuchWindowException(nameOrHandle)
        }
        driver.currentPage = page
        driver
    }
    @SuppressWarnings('UnusedMethodParameter')
    WebDriver newWindow(WindowType typeHint) { driver.currentPage = driver.context.newPage(); driver }

    private static class PlaywrightAlert implements Alert {
        final PlaywrightTargetLocator targetLocator
        final Dialog dialog
        private String promptText
        PlaywrightAlert(PlaywrightTargetLocator targetLocator, Dialog dialog) { this.targetLocator = targetLocator; this.dialog = dialog }
        void dismiss() { dialog.dismiss() }
        void accept() { promptText == null ? dialog.accept() : dialog.accept(promptText) }
        String getText() { dialog.message() }
        void sendKeys(String keysToSend) { promptText = keysToSend }
    }
}
