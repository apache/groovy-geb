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
package geb.navigator.event

import geb.Browser
import geb.navigator.Navigator

import java.util.function.Supplier

class DelegatingNavigatorEventListener implements NavigatorEventListener {

    private final Supplier<NavigatorEventListener> delegateSupplier
    private final Navigator source

    DelegatingNavigatorEventListener(Supplier<NavigatorEventListener> delegateSupplier, Navigator source) {
        this.delegateSupplier = delegateSupplier
        this.source = source
    }

    DelegatingNavigatorEventListener(NavigatorEventListener delegate, Navigator source) {
        this({ delegate } as Supplier<NavigatorEventListener>, source)
    }

    @Override
    void beforeClick(Browser browser, Navigator navigator) {
        delegateSupplier.get()?.beforeClick(browser, source)
    }

    @Override
    void afterClick(Browser browser, Navigator navigator) {
        delegateSupplier.get()?.afterClick(browser, source)
    }

    @Override
    void beforeValueSet(Browser browser, Navigator navigator, Object value) {
        delegateSupplier.get()?.beforeValueSet(browser, source, value)
    }

    @Override
    void afterValueSet(Browser browser, Navigator navigator, Object value) {
        delegateSupplier.get()?.afterValueSet(browser, source, value)
    }

    @Override
    void beforeSendKeys(Browser browser, Navigator navigator, Object value) {
        delegateSupplier.get()?.beforeSendKeys(browser, source, value)
    }

    @Override
    void afterSendKeys(Browser browser, Navigator navigator, Object value) {
        delegateSupplier.get()?.afterSendKeys(browser, source, value)
    }
}
