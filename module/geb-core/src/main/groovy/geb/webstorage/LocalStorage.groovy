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
package geb.webstorage

import org.openqa.selenium.html5.WebStorage as SeleniumWebStorage
import org.openqa.selenium.html5.LocalStorage as SeleniumLocalStorage

class LocalStorage implements WebStorage {

    private final SeleniumWebStorage webStorage

    LocalStorage(SeleniumWebStorage webStorage) {
        this.webStorage = webStorage
    }

    @Override
    String getAt(String key) {
        localStorage.getItem(key)
    }

    @Override
    void putAt(String key, String value) {
        localStorage.setItem(key, value)
    }

    @Override
    void remove(String key) {
        localStorage.removeItem(key)
    }

    @Override
    Set<String> keySet() {
        localStorage.keySet()
    }

    @Override
    int size() {
        localStorage.size()
    }

    @Override
    void clear() {
        localStorage.clear()
    }

    private SeleniumLocalStorage getLocalStorage() {
        webStorage.localStorage
    }

}
