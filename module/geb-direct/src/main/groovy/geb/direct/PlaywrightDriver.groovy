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

final class PlaywrightDriver {
    private PlaywrightDriver() { }
    static Closure config(@DelegatesTo(PlaywrightOptions) Closure closure) {
        PlaywrightOptions options = new PlaywrightOptions()
        Closure configured = closure.rehydrate(options, closure.owner, closure.thisObject)
        configured.resolveStrategy = Closure.DELEGATE_FIRST
        configured.call()
        return { create(options.copy()) }
    }
    static PlaywrightWebDriver create(PlaywrightOptions options = new PlaywrightOptions()) { new PlaywrightWebDriver(options) }
    static PlaywrightWebDriver create(@DelegatesTo(PlaywrightOptions) Closure closure) {
        PlaywrightOptions options = new PlaywrightOptions()
        Closure configured = closure.rehydrate(options, closure.owner, closure.thisObject)
        configured.resolveStrategy = Closure.DELEGATE_FIRST
        configured.call()
        create(options)
    }
}
