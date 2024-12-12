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

/**
 * A No-op implementation of {@link NavigatorEventListener}.
 * Useful as a base class for listener implementations which only want to be notified of a subset of navigator events.
 */
class NavigatorEventListenerSupport implements NavigatorEventListener {
    @Override
    void beforeClick(Browser browser, Navigator navigator) {
    }

    @Override
    void afterClick(Browser browser, Navigator navigator) {
    }

    @Override
    void beforeValueSet(Browser browser, Navigator navigator, Object value) {
    }

    @Override
    void afterValueSet(Browser browser, Navigator navigator, Object value) {
    }

    @Override
    void beforeSendKeys(Browser browser, Navigator navigator, Object value) {
    }

    @Override
    void afterSendKeys(Browser browser, Navigator navigator, Object value) {
    }
}
