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

import geb.js.JavascriptInterface

class SessionStorage implements WebStorage {

    private final JavascriptInterface js

    SessionStorage(JavascriptInterface js) {
        this.js = js
    }

    @Override
    String getAt(String key) {
        js.exec(key, 'return window.sessionStorage.getItem(arguments[0]);')
    }

    @Override
    void putAt(String key, String value) {
        js.exec(key, value, 'window.sessionStorage.setItem(arguments[0], arguments[1]);')
    }

    @Override
    void remove(String key) {
        js.exec(key, 'window.sessionStorage.removeItem(arguments[0]);')
    }

    @Override
    Set<String> keySet() {
        js.exec('''
            var out = [];
            for (var i = 0; i < window.sessionStorage.length; i++) {
                out.push(window.sessionStorage.key(i));
            }
            return out;
        ''') as Set<String>
    }

    @Override
    int size() {
        (js.exec('return window.sessionStorage.length;') as Number).intValue()
    }

    @Override
    void clear() {
        js.exec('window.sessionStorage.clear();')
    }

}
