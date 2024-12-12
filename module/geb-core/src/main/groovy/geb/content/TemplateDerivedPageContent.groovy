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
package geb.content

import geb.Browser
import geb.error.ContentCountOutOfBoundsException
import geb.error.RequiredPageContentNotPresent
import geb.navigator.event.BrowserConfigurationDelegatingNavigatorEventListener
import geb.navigator.Navigator
import geb.navigator.event.DelegatingNavigatorEventListener
import geb.navigator.event.NavigatorEventListener
import org.openqa.selenium.WebDriver

@SuppressWarnings("FieldName")
class TemplateDerivedPageContent implements Navigator {

    private PageContentTemplate _template
    private Object[] _args
    private Browser _browser
    private StringRepresentationProvider _stringRepresentationProvider

    @Delegate(allNames = true)
    protected Navigator _navigator

    TemplateDerivedPageContent(Browser browser, PageContentTemplate template, Navigator navigator, Object[] args) {
        this._browser = browser
        this._template = template
        this._navigator = navigator
        this._navigator.eventListener = new BrowserConfigurationDelegatingNavigatorEventListener(browser, this)
        this._args = args
        this._stringRepresentationProvider = new TemplateDerivedContentStringRepresentationProvider(template, args, navigator)
    }

    String toString() {
        _stringRepresentationProvider.stringRepresentation
    }

    Browser getBrowser() {
        _browser
    }

    WebDriver getDriver() {
        getBrowser().driver
    }

    boolean isPresent() {
        !_navigator.empty
    }

    void require() {
        if (!isPresent()) {
            throw new RequiredPageContentNotPresent(this)
        }
        this
    }

    void ensureWithinBounds(int min, int max) {
        def count = size()
        if (count > max) {
            throw new ContentCountOutOfBoundsException(this, "at most ${formatItems(max)}", formatItems(count))
        }
        if (count < min) {
            throw new ContentCountOutOfBoundsException(this, "at least ${formatItems(min)}", formatItems(count))
        }
    }

    Navigator click() {
        def to = templateParams.toSingle ?: templateParams.toList
        if (to) {
            def toWait = _template.config.getWaitForParam(templateParams.toWait)
            _navigator.click(to, toWait)
        } else {
            _navigator.click()
        }
    }

    PageContentTemplateParams getTemplateParams() {
        _template.params
    }

    boolean asBoolean() {
        _navigator.asBoolean()
    }

    def methodMissing(String name, args) {
        _navigator."$name"(*args)
    }

    def propertyMissing(String name) {
        _navigator[name]
    }

    @Override
    void setEventListener(NavigatorEventListener listener) {
        _navigator.eventListener = new DelegatingNavigatorEventListener(listener, this)
    }

    def propertyMissing(String name, val) {
        _navigator[name] = val
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof Navigator) {
            allElements() == o.allElements()
        } else {
            def values = iterator()*.value().findAll { it != null }
            def value
            switch (values.size()) {
                case 0:
                    value = null
                    break
                case 1:
                    value = values.first()
                    break
                default:
                    value = values
            }
            value == o
        }
    }

    @Override
    int hashCode() {
        allElements().hashCode()
    }

    Object asType(Class type) {
        _navigator.class in type ? _navigator : super.asType(type)
    }

    PageContentContainer getRootContainer() {
        _template.owner.rootContainer
    }

    List<String> getContentPath() {
        _template.owner.contentPath + _template.name
    }

    private String formatItems(int count) {
        count == 1 ? "1 element" : "$count elements"
    }

}