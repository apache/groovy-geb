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

import geb.Module
import geb.navigator.Locator
import geb.navigator.Navigator
import geb.navigator.factory.NavigatorFactory
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver.TargetLocator
import org.openqa.selenium.WebElement

class NavigableSupport implements Navigable {

    private final NavigatorFactory navigatorFactory
    private final TargetLocator targetLocator

    NavigableSupport(NavigatorFactory navigatorFactory, TargetLocator targetLocator) {
        this.navigatorFactory = navigatorFactory
        this.targetLocator = targetLocator
    }

    Navigator find() {
        base
    }

    Navigator $() {
        base
    }

    Navigator find(int index) {
        base[index]
    }

    Navigator find(Range<Integer> range) {
        base[range]
    }

    Navigator $(int index) {
        base[index]
    }

    Navigator $(Range<Integer> range) {
        base[range]
    }

    Navigator find(String selector) {
        locator.find(selector)
    }

    Navigator $(String selector) {
        locator.find(selector)
    }

    Navigator find(String selector, int index) {
        locator.find(selector, index)
    }

    Navigator find(String selector, Range<Integer> range) {
        locator.find(selector, range)
    }

    Navigator $(String selector, int index) {
        locator.find(selector, index)
    }

    Navigator $(String selector, Range<Integer> range) {
        locator.find(selector, range)
    }

    @Override
    Navigator $(Map<String, Object> attributes, By bySelector) {
        locator.find(attributes, bySelector)
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector) {
        locator.find(attributes, bySelector)
    }

    @Override
    Navigator $(Map<String, Object> attributes, By bySelector, int index) {
        locator.find(attributes, bySelector, index)
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector, int index) {
        locator.find(attributes, bySelector, index)
    }

    @Override
    Navigator $(Map<String, Object> attributes, By bySelector, Range<Integer> range) {
        locator.find(attributes, bySelector, range)
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector, Range<Integer> range) {
        locator.find(attributes, bySelector, range)
    }

    @Override
    Navigator $(By bySelector) {
        locator.find(bySelector)
    }

    @Override
    Navigator find(By bySelector) {
        locator.find(bySelector)
    }

    @Override
    Navigator $(By bySelector, int index) {
        locator.find(bySelector, index)
    }

    @Override
    Navigator find(By bySelector, int index) {
        locator.find(bySelector, index)
    }

    @Override
    Navigator $(By bySelector, Range<Integer> range) {
        locator.find(bySelector, range)
    }

    @Override
    Navigator find(By bySelector, Range<Integer> range) {
        locator.find(bySelector, range)
    }

    Navigator find(Map<String, Object> attributes) {
        locator.find(attributes)
    }

    Navigator $(Map<String, Object> attributes) {
        locator.find(attributes)
    }

    Navigator find(Map<String, Object> attributes, int index) {
        locator.find(attributes, index)
    }

    Navigator find(Map<String, Object> attributes, Range<Integer> range) {
        locator.find(attributes, range)
    }

    Navigator $(Map<String, Object> attributes, int index) {
        locator.find(attributes, index)
    }

    Navigator $(Map<String, Object> attributes, Range<Integer> range) {
        locator.find(attributes, range)
    }

    Navigator find(Map<String, Object> attributes, String selector) {
        locator.find(attributes, selector)
    }

    Navigator $(Map<String, Object> attributes, String selector) {
        locator.find(attributes, selector)
    }

    Navigator find(Map<String, Object> attributes, String selector, int index) {
        locator.find(attributes, selector, index)
    }

    Navigator find(Map<String, Object> attributes, String selector, Range<Integer> range) {
        locator.find(attributes, selector, range)
    }

    Navigator $(Map<String, Object> attributes, String selector, int index) {
        locator.find(attributes, selector, index)
    }

    Navigator $(Map<String, Object> attributes, String selector, Range<Integer> range) {
        locator.find(attributes, selector, range)
    }

    Navigator $(Navigator[] navigators) {
        navigatorFactory.createFromNavigators(Arrays.asList(navigators))
    }

    Navigator $(WebElement[] elements) {
        navigatorFactory.createFromWebElements(Arrays.asList(elements))
    }

    @Override
    Navigator focused() {
        $(targetLocator.activeElement())
    }

    @Override
    <T extends Module> T module(Class<T> moduleClass) {
        base.module(moduleClass)
    }

    @Override
    <T extends Module> T module(T module) {
        base.module(module)
    }

    private Navigator getBase() {
        navigatorFactory.base
    }

    private Locator getLocator() {
        navigatorFactory.locator
    }

}
