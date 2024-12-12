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
package geb.navigator.factory

import geb.navigator.Locator
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import java.util.function.Supplier

/**
 * The object that produces Navigator objects.
 *
 * A browser has-a NavigatorFactory, and passes it down to pages/modules etc.
 */
interface NavigatorFactory {

    /**
     * The base navigator for this factory.
     *
     * @return The base navigator for this factory.
     */
    Navigator getBase()

    /**
     * The locator used for all content lookups from this factory.
     *
     * @return The locator used for all content lookups from this factory.
     */
    Locator getLocator()

    /**
     * Create a dynamic navigator, backed by the given web elements supplier.
     *
     * @param elementsSupplier A supplier of web elements to back the navigator.
     * @return The created navigator
     */
    Navigator createDynamic(Supplier<Iterable<WebElement>> elementsSupplier)

    /**
     * Create a navigator, backed by the given web elements.
     *
     * @param elements The web elements to back the navigator.
     * @return The created navigator
     */
    Navigator createFromWebElements(Iterable<WebElement> elements)

    /**
     * Create a navigator, backed by the given navigators.
     * @param navigators The navigators to back the navigator
     * @return The created navigator
     */
    Navigator createFromNavigators(Iterable<Navigator> navigators)

    /**
     * Create a new factory, relative to the given navigator.
     *
     * @param newBase The base to use for the new navigator factory.
     * @return The new navigator factory.
     */
    NavigatorFactory relativeTo(Navigator newBase)
}
