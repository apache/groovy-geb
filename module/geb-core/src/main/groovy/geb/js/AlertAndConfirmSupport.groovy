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

interface AlertAndConfirmSupport {

    // tag::alert[]
    def withAlert(Closure actions)
    // end::alert[]

    // tag::alert[]
    def withAlert(Map params, Closure actions)
    // end::alert[]

    // tag::alert[]
    void withNoAlert(Closure actions)
    // end::alert[]

    // tag::confirm[]
    def withConfirm(boolean ok, Closure actions)
    // end::confirm[]

    // tag::confirm[]
    def withConfirm(Closure actions)
    // end::confirm[]

    // tag::confirm[]
    def withConfirm(Map params, Closure actions)
    // end::confirm[]

    // tag::confirm[]
    def withConfirm(Map params, boolean ok, Closure actions)
    // end::confirm[]

    // tag::confirm[]
    void withNoConfirm(Closure actions)
    // end::confirm[]
}
