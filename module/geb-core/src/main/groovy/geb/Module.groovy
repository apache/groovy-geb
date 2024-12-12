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
package geb

import geb.content.*
import geb.download.DefaultDownloadSupport
import geb.download.DownloadSupport
import geb.download.UninitializedDownloadSupport
import geb.error.GebException
import geb.error.ModuleInstanceNotInitializedException
import geb.frame.DefaultFrameSupport
import geb.frame.FrameSupport
import geb.frame.UninitializedFrameSupport
import geb.interaction.DefaultInteractionsSupport
import geb.interaction.InteractionsSupport
import geb.interaction.UninitializedInteractionSupport
import geb.js.AlertAndConfirmSupport
import geb.js.DefaultAlertAndConfirmSupport
import geb.js.JavascriptInterface
import geb.js.UninitializedAlertAndConfirmSupport
import geb.navigator.event.BrowserConfigurationDelegatingNavigatorEventListener
import geb.navigator.Navigator
import geb.navigator.event.DelegatingNavigatorEventListener
import geb.navigator.event.NavigatorEventListener
import geb.navigator.factory.NavigatorFactory
import geb.textmatching.TextMatchingSupport
import geb.waiting.DefaultWaitingSupport
import geb.waiting.UninitializedWaitingSupport
import geb.waiting.WaitingSupport

class Module implements Navigator, PageContentContainer, Initializable, WaitingSupport {

    static base = null

    @Delegate
    private PageContentSupport pageContentSupport = new UninitializedPageContentSupport(this)

    @Delegate
    private DownloadSupport downloadSupport = new UninitializedDownloadSupport(this)

    private WaitingSupport waitingSupport = new UninitializedWaitingSupport(this)

    @Delegate(parameterAnnotations = true)
    private FrameSupport frameSupport = new UninitializedFrameSupport(this)

    @Delegate
    private TextMatchingSupport textMatchingSupport = new TextMatchingSupport()

    @Delegate
    private AlertAndConfirmSupport alertAndConfirmSupport = new UninitializedAlertAndConfirmSupport(this)

    @Delegate(parameterAnnotations = true)
    private InteractionsSupport interactionsSupport = new UninitializedInteractionSupport(this)

    private JavascriptInterface js

    protected Navigator navigator

    protected Browser browser

    private StringRepresentationProvider stringRepresentationProvider = this

    private PageContentTemplate template

    void init(Browser browser, NavigatorFactory navigatorFactory) {
        this.browser = browser
        this.navigator = navigatorFactory.base
        this.navigator.eventListener = new BrowserConfigurationDelegatingNavigatorEventListener(browser, this)
        Map<String, PageContentTemplate> contentTemplates = PageContentTemplateBuilder.build(browser, this, navigatorFactory, 'content', this.class, Module)
        this.pageContentSupport = new DefaultPageContentSupport(this, contentTemplates, navigatorFactory, this.navigator)
        this.downloadSupport = new DefaultDownloadSupport(browser)
        this.waitingSupport = new DefaultWaitingSupport(browser.config)
        this.frameSupport = new DefaultFrameSupport(browser)
        this.js = browser.js
        this.alertAndConfirmSupport = new DefaultAlertAndConfirmSupport({ js }, browser.config)
        this.interactionsSupport = new DefaultInteractionsSupport(browser)
        initialized()
    }

    void init(PageContentTemplate template, Object[] args) {
        this.template = template
        stringRepresentationProvider = new TemplateDerivedContentStringRepresentationProvider(template, args, this)
    }

    JavascriptInterface getJs() {
        if (js == null) {
            throw uninitializedException()
        }
        js
    }

    def methodMissing(String name, args) {
        pageContentSupport.methodMissing(name, args)
    }

    def propertyMissing(String name) {
        pageContentSupport.propertyMissing(name)
    }

    def propertyMissing(String name, val) {
        pageContentSupport.propertyMissing(name, val)
    }

    boolean asBoolean() {
        getInitializedNavigator().asBoolean()
    }

    @Override
    def <T> T waitFor(Map params = [:], String waitPreset, Closure<T> block) {
        waitingSupport.waitFor(params, waitPreset, block)
    }

    @Override
    def <T> T waitFor(Map params = [:], Closure<T> block) {
        waitingSupport.waitFor(params, block)
    }

    @Override
    def <T> T waitFor(Map params = [:], Number timeout, Closure<T> block) {
        waitingSupport.waitFor(params, timeout, block)
    }

    @Override
    def <T> T waitFor(Map params = [:], Number timeout, Number interval, Closure<T> block) {
        waitingSupport.waitFor(params, timeout, interval, block)
    }

    GebException uninitializedException() {
        def message = "Instance of module ${getClass()} has not been initialized. Please pass it to Navigable.module() or Navigator.module() before using it."
        throw new ModuleInstanceNotInitializedException(message)
    }

    String getStringRepresentation() {
        getClass().name
    }

    @Override
    void setEventListener(NavigatorEventListener listener) {
        getInitializedNavigator().eventListener = new DelegatingNavigatorEventListener(listener, this)
    }

    @Override
    String toString() {
        stringRepresentationProvider.stringRepresentation
    }

    @Override
    int hashCode() {
        allElements().hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof Navigator) {
            allElements() == obj.allElements()
        }
    }

    @Override
    PageContentContainer getRootContainer() {
        hasOwner ? template.owner.rootContainer : this
    }

    @Override
    List<String> getContentPath() {
        hasOwner ? template.owner.contentPath + template.name : []
    }

    protected boolean getHasOwner() {
        template?.owner
    }

    @SuppressWarnings("EmptyMethod")
    protected void initialized() {
    }

    @Delegate(allNames = true)
    protected Navigator getInitializedNavigator() {
        if (navigator == null) {
            throw uninitializedException()
        }
        navigator
    }

}