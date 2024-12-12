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

/**
 * Represents a web storage type, that is either local or session storage.
 */
interface WebStorage {

    /**
     * Returns a value stored under the given {@code key} in the storage.
     * Adds support for using subscript operator for reading values.
     */
    String getAt(String key)

    /**
     * Stores the {@code value} under the the given {@code key} in the storage.
     * Adds support for using subscript operator for writing values values.
     */
    void putAt(String key, String value)

    /**
     * Removes the value stored under the given {@code key}.
     */
    void remove(String key)

    /**
     * Returns a set of all keys for which values are stored.
     */
    Set<String> keySet()

    /**
     * Returns the number of keys for which values are stored.
     */
    int size()

    /**
     * Removes all data from the store.
     */
    void clear()
}