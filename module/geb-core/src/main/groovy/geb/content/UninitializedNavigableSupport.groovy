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

import geb.Initializable
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class UninitializedNavigableSupport implements Navigable {

    private final Initializable initializable

    UninitializedNavigableSupport(Initializable initializable) {
        this.initializable = initializable
    }

    @Override
    Navigator find() {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $() {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Navigator[] navigators) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(WebElement[] elements) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(String selector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(String selector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(String selector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(String selector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(String selector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(String selector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, By bySelector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, By bySelector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, By bySelector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, By bySelector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(By bySelector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(By bySelector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(By bySelector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(By bySelector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(By bySelector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(By bySelector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, String selector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, String selector) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, String selector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator find(Map<String, Object> attributes, String selector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, String selector, int index) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator $(Map<String, Object> attributes, String selector, Range<Integer> range) {
        throw initializable.uninitializedException()
    }

    @Override
    Navigator focused() {
        throw initializable.uninitializedException()
    }

    @Override
    <T extends Module> T module(Class<T> moduleClass) {
        throw initializable.uninitializedException()
    }

    @Override
    <T extends Module> T module(T module) {
        throw initializable.uninitializedException()
    }
}
