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

import geb.Browser
import geb.collection.FilteringIterable
import geb.navigator.Navigator
import jodd.util.collection.CompositeIterator
import org.openqa.selenium.WebElement

import java.util.function.Supplier

abstract class AbstractNavigatorFactory implements NavigatorFactory {

    private final Browser browser
    private final InnerNavigatorFactory innerNavigatorFactory

    AbstractNavigatorFactory(Browser browser, InnerNavigatorFactory innerNavigatorFactory) {
        this.browser = browser
        this.innerNavigatorFactory = innerNavigatorFactory
    }

    Navigator createDynamic(Supplier<Iterable<WebElement>> elementsSupplier) {
        createFromWebElements({ elementsSupplier.get().iterator() } as Iterable<WebElement>)
    }

    Navigator createFromWebElements(Iterable<WebElement> elements) {
        def filtered = new FilteringIterable(elements, { it != null })

        innerNavigatorFactory.createNavigator(browser, filtered)
    }

    Navigator createFromNavigators(Iterable<Navigator> navigators) {
        def notNullNavigators = navigators.findAll { it != null }
        def elements = { new CompositeIterator(notNullNavigators*.elementIterator() as Iterator[]) } as Iterable<WebElement>

        createFromWebElements(elements)
    }

    NavigatorFactory relativeTo(Navigator newBase) {
        new NavigatorBackedNavigatorFactory(newBase, innerNavigatorFactory)
    }

    protected Browser getBrowser() {
        browser
    }

}
