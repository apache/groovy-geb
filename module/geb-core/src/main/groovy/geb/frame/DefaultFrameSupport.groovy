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
package geb.frame

import geb.Browser
import geb.Page
import geb.content.TemplateDerivedPageContent
import geb.navigator.Navigator
import groovy.transform.CompileStatic
import org.openqa.selenium.NoSuchFrameException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class DefaultFrameSupport implements FrameSupport {

    Browser browser

    DefaultFrameSupport(Browser browser) {
        this.browser = browser
    }

    <P extends Page, T> T withFrame(frame, @DelegatesTo.Target Class<P> page, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<T> block) {
        withFrame(frame, createPage(page), block)
    }

    <P extends Page, T> T withFrame(frame, @DelegatesTo.Target P page, @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block) {
        executeWithFrame(frame, page, block)
    }

    <P extends Page, T> T withFrame(Navigator frameNavigator, @DelegatesTo.Target Class<P> page, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<T> block) {
        withFrame(frameNavigator, createPage(page), block)
    }

    <P extends Page, T> T withFrame(Navigator frameNavigator, @DelegatesTo.Target P page, @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block) {
        executeWithFrame(frameNavigator, page, block)
    }

    <T> T withFrame(
        Object frame,
        @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block
    ) {
        executeWithFrame(frame, null, block)
    }

    <T> T withFrame(
        Navigator frameNavigator,
        @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block
    ) {
        executeWithFrame(frameNavigator, null, block)
    }

    <T> T withFrame(
        TemplateDerivedPageContent frame,
        @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block
    ) {
        def page = frame.templateParams.page
        page ? withFrame(frame, page, block) : withFrame(frame as Navigator, block)
    }

    private <T> T executeWithFrame(
        Object frame,
        Page page,
        @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block
    ) {
        def originalPage = browser.page
        switch (frame) {
            case Integer:
                browser.driver.switchTo().frame((Integer) frame)
                break
            case String:
                browser.driver.switchTo().frame((String) frame)
                break
            case WebElement:
                browser.driver.switchTo().frame((WebElement) frame)
                break
            default:
                throw new IllegalArgumentException("Unsupported frame reference: $frame")
        }
        if (page) {
            browser.verifyAtImplicitly(page)
        }
        try {
            def cloned = (Closure) block.clone()
            cloned.delegate = browser
            cloned.resolveStrategy = DELEGATE_FIRST
            cloned.call()
        } finally {
            browser.page(originalPage)
            def targetLocator = browser.driver.switchTo()
            try {
                targetLocator.parentFrame()
            } catch (WebDriverException e) {
                if (e.message.startsWith("Command not found") || e.message.startsWith("Unknown command")) {
                    targetLocator.defaultContent()
                } else {
                    throw e
                }
            }
        }
    }

    private <T> T executeWithFrame(
        Navigator frameNavigator,
        Page page,
        @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block
    ) {
        WebElement element = frameNavigator.firstElement()
        if (element == null) {
            throw new NoSuchFrameException("No elements for given content: ${frameNavigator}")
        }
        executeWithFrame(element, page, block)
    }

    private Page createPage(Class<? extends Page> page) {
        page ? browser.createPage(page) : null
    }
}
