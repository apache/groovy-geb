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
package geb.direct

class PlaywrightStorage {
    private static final String LOCAL_STORAGE = 'localStorage'
    private static final String SESSION_STORAGE = 'sessionStorage'
    final PlaywrightWebDriver driver
    PlaywrightStorage(PlaywrightWebDriver driver) { this.driver = driver }
    String local(String key) { value(LOCAL_STORAGE, key) }
    void local(String key, String value) { set(LOCAL_STORAGE, key, value) }
    void clearLocal() { clear(LOCAL_STORAGE) }
    List<String> localKeys() { keys(LOCAL_STORAGE) }
    String session(String key) { value(SESSION_STORAGE, key) }
    void session(String key, String value) { set(SESSION_STORAGE, key, value) }
    void clearSession() { clear(SESSION_STORAGE) }
    List<String> sessionKeys() { keys(SESSION_STORAGE) }

    private String value(String storage, String key) { driver.page.evaluate("([storage, key]) => window[storage].getItem(key)", [storage, key]) as String }
    private void set(String storage, String key, String value) { driver.page.evaluate("([storage, key, value]) => window[storage].setItem(key, value)", [storage, key, value]) }
    private void clear(String storage) { driver.page.evaluate("storage => window[storage].clear()", storage) }
    private List<String> keys(String storage) { driver.page.evaluate("storage => Object.keys(window[storage])", storage) as List<String> }
}
