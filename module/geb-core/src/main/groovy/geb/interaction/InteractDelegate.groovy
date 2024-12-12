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
package geb.interaction

import geb.navigator.Navigator
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.interactions.Actions

import java.time.Duration

class InteractDelegate {

    Actions actions

    InteractDelegate(WebDriver driver) {
        actions = new Actions(driver)
    }

    /**
     * @see Actions#keyDown(org.openqa.selenium.Keys)
     */
    InteractDelegate keyDown(Keys theKey) {
        actions.keyDown(theKey)
        this
    }

    /**
     * @see Actions#keyDown(org.openqa.selenium.WebElement, org.openqa.selenium.Keys)
     */
    InteractDelegate keyDown(Navigator navigator, Keys theKey) {
        actions.keyDown(navigator.singleElement(), theKey)
        this
    }

    /**
     * @see Actions#keyUp(org.openqa.selenium.Keys)
     */
    InteractDelegate keyUp(Keys theKey) {
        actions.keyUp(theKey)
        this
    }

    /**
     * @see Actions#keyUp(org.openqa.selenium.WebElement, org.openqa.selenium.Keys)
     */
    InteractDelegate keyUp(Navigator navigator, Keys theKey) {
        actions.keyUp(navigator.singleElement(), theKey)
        this
    }

    /**
     * @see Actions#sendKeys(java.lang.CharSequence ...)
     */
    InteractDelegate sendKeys(CharSequence... keysToSend) {
        actions.sendKeys(keysToSend)
        this
    }

    /**
     * @see Actions#sendKeys(org.openqa.selenium.WebElement, java.lang.CharSequence ...)
     */
    InteractDelegate sendKeys(Navigator navigator, CharSequence... keysToSend) {
        actions.sendKeys(navigator.singleElement(), keysToSend)
        this
    }

    /**
     * @see Actions#clickAndHold(org.openqa.selenium.WebElement)
     */
    InteractDelegate clickAndHold(Navigator navigator) {
        actions.clickAndHold(navigator.singleElement())
        this
    }

    /**
     * @see Actions#clickAndHold()
     */
    InteractDelegate clickAndHold() {
        actions.clickAndHold()
        this
    }

    /**
     * @see Actions#release(org.openqa.selenium.WebElement)
     */
    InteractDelegate release(Navigator navigator) {
        actions.release(navigator.singleElement())
        this
    }

    /**
     * @see Actions#release()
     */
    InteractDelegate release() {
        actions.release()
        this
    }

    /**
     * @see Actions#click(org.openqa.selenium.WebElement)
     */
    InteractDelegate click(Navigator navigator) {
        actions.click(navigator.singleElement())
        this
    }

    /**
     * @see Actions#click()
     */
    InteractDelegate click() {
        actions.click()
        this
    }

    /**
     * @see Actions#doubleClick(org.openqa.selenium.WebElement)
     */
    InteractDelegate doubleClick(Navigator navigator) {
        actions.doubleClick(navigator.singleElement())
        this
    }

    /**
     * @see Actions#doubleClick()
     */
    InteractDelegate doubleClick() {
        actions.doubleClick()
        this
    }

    /**
     * @see Actions#moveToElement(org.openqa.selenium.WebElement)
     */
    InteractDelegate moveToElement(Navigator navigator) {
        actions.moveToElement(navigator.singleElement())
        this
    }

    /**
     * @see Actions#moveToElement(org.openqa.selenium.WebElement, int, int)
     */
    InteractDelegate moveToElement(Navigator navigator, int xOffset, int yOffset) {
        actions.moveToElement(navigator.singleElement(), xOffset, yOffset)
        this
    }

    /**
     * @see Actions#moveByOffset(int, int)
     */
    InteractDelegate moveByOffset(int xOffset, int yOffset) {
        actions.moveByOffset(xOffset, yOffset)
        this
    }

    /**
     * @see Actions#contextClick(org.openqa.selenium.WebElement)
     */
    InteractDelegate contextClick(Navigator navigator) {
        actions.contextClick(navigator.singleElement())
        this
    }

    /**
     * @see Actions#contextClick()
     */
    InteractDelegate contextClick() {
        actions.contextClick()
        this
    }

    /**
     * @see Actions#dragAndDrop(org.openqa.selenium.WebElement, org.openqa.selenium.WebElement)
     */
    InteractDelegate dragAndDrop(Navigator source, Navigator target) {
        actions.dragAndDrop(source.singleElement(), target.singleElement())
        this
    }

    /**
     * @see Actions#dragAndDropBy(org.openqa.selenium.WebElement, int, int)
     */
    InteractDelegate dragAndDropBy(Navigator source, int xOffset, int yOffset) {
        actions.dragAndDropBy(source.singleElement(), xOffset, yOffset)
        this
    }

    /**
     * @see Actions#pause(long)
     */
    InteractDelegate pause(long pause) {
        actions.pause(pause)
        this
    }

    /**
     * @see Actions#pause(java.time.Duration)
     */
    InteractDelegate pause(Duration duration) {
        actions.pause(duration)
        this
    }

    void perform() {
        actions.perform()
    }

}
