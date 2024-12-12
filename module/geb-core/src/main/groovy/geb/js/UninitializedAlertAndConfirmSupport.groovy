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
package geb.js

import geb.Initializable

class UninitializedAlertAndConfirmSupport implements AlertAndConfirmSupport {
    private final Initializable initializable

    UninitializedAlertAndConfirmSupport(Initializable initializable) {
        this.initializable = initializable
    }

    @Override
    def withAlert(Map params = [:], Closure actions) {
        throw initializable.uninitializedException()
    }

    @Override
    void withNoAlert(Closure actions) {
        throw initializable.uninitializedException()
    }

    @Override
    def withConfirm(boolean ok, Closure actions) {
        throw initializable.uninitializedException()
    }

    @Override
    def withConfirm(Map params = [:], boolean ok = true, Closure actions) {
        throw initializable.uninitializedException()
    }

    @Override
    void withNoConfirm(Closure actions) {
        throw initializable.uninitializedException()
    }
}
