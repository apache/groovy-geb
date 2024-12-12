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
import org.openqa.selenium.NoSuchFrameException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement

import static groovy.lang.Closure.DELEGATE_FIRST

class DefaultFrameSupport implements FrameSupport {

    Browser browser

    DefaultFrameSupport(Browser browser) {
        this.browser = browser
    }

    public <P extends Page, T> T withFrame(frame, @DelegatesTo.Target Class<P> page, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<T> block) {
        withFrame(frame, createPage(page), block)
    }

    public <P extends Page, T> T withFrame(frame, @DelegatesTo.Target P page, @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block) {
        executeWithFrame(frame, page, block)
    }

    public <P extends Page, T> T withFrame(Navigator frameNavigator, @DelegatesTo.Target Class<P> page, @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<T> block) {
        withFrame(frameNavigator, createPage(page), block)
    }

    public <P extends Page, T> T withFrame(Navigator frameNavigator, @DelegatesTo.Target P page, @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> block) {
        executeWithFrame(frameNavigator, page, block)
    }

    public <T> T withFrame(frame, Closure<T> block) {
        executeWithFrame(frame, null, block)
    }

    public <T> T withFrame(Navigator frameNavigator, Closure<T> block) {
        executeWithFrame(frameNavigator, null, block)
    }

    public <T> T withFrame(TemplateDerivedPageContent frame, Closure<T> block) {
        def page = frame.templateParams.page
        page ? withFrame(frame, page, block) : withFrame(frame as Navigator, block)
    }

    private <T> T executeWithFrame(frame, Page page, Closure<T> block) {
        def originalPage = browser.page
        browser.driver.switchTo().frame(frame)
        if (page) {
            browser.verifyAtImplicitly(page)
        }
        try {
            Closure cloned = block.clone()
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

    private <T> T executeWithFrame(Navigator frameNavigator, Page page, Closure<T> block) {
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
