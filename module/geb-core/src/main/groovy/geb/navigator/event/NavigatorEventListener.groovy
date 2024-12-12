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
 * Implementations of this interface can be registered with {@link geb.Configuration} to listen to certain {@link Navigator} events.
 *
 * @see NavigatorEventListenerSupport
 */
interface NavigatorEventListener {

    /**
     * Called before {@code click()} is called on all of the elements of a {@link Navigator}
     *
     * @param browser The {@link Browser} instance used for creating the {@link Navigator} for which the event occurred
     * @param navigator The {@link Navigator} for which the event occurred
     */
    void beforeClick(Browser browser, Navigator navigator)

    /**
     * Called after {@code click()} is called on all of the elements of a {@link Navigator}
     *
     * @param browser The {@link Browser} instance used for creating the {@link Navigator} for which the event occurred
     * @param navigator The {@link Navigator} for which the event occurred
     */
    void afterClick(Browser browser, Navigator navigator)

    /**
     * Called before setting value of a {@link Navigator}, that is at the beginning of {@link Navigator#value(Object)}
     *
     * @param browser The {@link Browser} instance used for creating the {@link Navigator} for which the event occurred
     * @param navigator The {@link Navigator} for which the event occurred
     * @param value The value that will be set on the {@link Navigator}
     */
    void beforeValueSet(Browser browser, Navigator navigator, Object value)

    /**
     * Called after setting value of a {@link Navigator}, that is at the end of {@link Navigator#value(Object)}
     *
     * @param browser The {@link Browser} instance used for creating the {@link Navigator} for which the event occurred
     * @param navigator The {@link Navigator} for which the event occurred
     * @param value The value that was set on the {@link Navigator}
     */
    void afterValueSet(Browser browser, Navigator navigator, Object value)

    /**
     * Called before {@code sendKeys()} is called on all of the elements of a {@link Navigator}, as part of {@link Navigator#leftShift(Object)}
     *
     * @param browser The {@link Browser} instance used for creating the {@link Navigator} for which the event occurred
     * @param navigator The {@link Navigator} for which the event occurred
     * @param value The value that was passed to {@link Navigator#leftShift(Object)}
     */
    void beforeSendKeys(Browser browser, Navigator navigator, Object value)

    /**
     * Called after {@code sendKeys()} is called on all of the elements of a {@link Navigator}, as part of {@link Navigator#leftShift(Object)}
     *
     * @param browser The {@link Browser} instance used for creating the {@link Navigator} for which the event occurred
     * @param navigator The {@link Navigator} for which the event occurred
     * @param value The value that was passed to {@link Navigator#leftShift(Object)}
     */
    void afterSendKeys(Browser browser, Navigator navigator, Object value)
}