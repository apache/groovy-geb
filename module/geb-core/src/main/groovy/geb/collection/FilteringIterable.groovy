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
package geb.collection

import java.util.function.Predicate

class FilteringIterable<T> implements Iterable<T> {

    private final Iterable<T> iterable
    private final Predicate<T> predicate

    FilteringIterable(Iterable<T> iterable, Predicate<T> predicate) {
        this.iterable = iterable
        this.predicate = predicate
    }

    @Override
    Iterator<T> iterator() {
        new FilteringIterator<T>(iterable.iterator(), predicate)
    }
}
