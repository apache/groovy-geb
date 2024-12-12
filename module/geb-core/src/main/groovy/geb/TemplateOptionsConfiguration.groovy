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
package geb

import groovy.transform.builder.Builder

class TemplateOptionsConfiguration {

    final boolean cache
    final wait
    final toWait
    final Closure<?> waitCondition
    final Optional<Boolean> required
    final Optional<Integer> min
    final Optional<Integer> max

    @Builder
    TemplateOptionsConfiguration(boolean cache, wait, toWait, Closure<?> waitCondition, Optional<Boolean> required, Optional<Integer> min, Optional<Integer> max) {
        this.cache = cache
        this.wait = wait
        this.toWait = toWait
        this.waitCondition = waitCondition
        this.required = required
        this.min = min
        this.max = max
    }
}
