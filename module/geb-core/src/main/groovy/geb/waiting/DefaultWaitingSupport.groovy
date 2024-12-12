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
package geb.waiting

import geb.Configuration

/**
 * A mixin style class that adds support for waiting for different things.
 *
 * This is mixed into {@link geb.Page} and {@link geb.Module}.
 */
class DefaultWaitingSupport implements WaitingSupport {

    private final Configuration config

    DefaultWaitingSupport(Configuration config) {
        this.config = config
    }

    public <T> T waitFor(Map params = [:], String waitPreset, Closure<T> block) {
        doWaitFor(params, config.getWaitPreset(waitPreset), block)
    }

    public <T> T waitFor(Map params = [:], Closure<T> block) {
        doWaitFor(params, config.defaultWait, block)
    }

    public <T> T waitFor(Map params = [:], Number timeout, Closure<T> block) {
        doWaitFor(params, config.getWait(timeout), block)
    }

    public <T> T waitFor(Map params = [:], Number timeout, Number interval, Closure<T> block) {
        doWaitFor(params, new Wait(timeout, interval), block)
    }

    private <T> T doWaitFor(Map params, Wait wait, Closure<T> block) {
        wait.customMessage = params["message"]
        wait.quiet = params["noException"]
        wait.waitFor(block)
    }

}