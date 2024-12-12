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
package geb.gradle.browserstack

import geb.gradle.ToStringProviderValue
import geb.gradle.cloud.CloudBrowsersExtension
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

import static BrowserStackLocal.LOCAL_ID_ENV_VAR
import static geb.gradle.browserstack.BrowserStackAccount.ACCESS_KEY_ENV_VAR
import static geb.gradle.browserstack.BrowserStackAccount.USER_ENV_VAR

abstract class BrowserStackExtension extends CloudBrowsersExtension {

    @Inject
    BrowserStackExtension(
        ObjectFactory objectFactory, TaskContainer tasks, TaskProvider<? extends Task> allTestsLifecycleTask,
        TaskProvider<? extends Task> openTunnelInBackgroundTask, TaskProvider<? extends Task> closeTunnelTask,
        String tasksGroup
    ) {
        super(objectFactory, tasks, allTestsLifecycleTask, openTunnelInBackgroundTask, closeTunnelTask, tasksGroup)

        task { test ->
            test.environment(
                (USER_ENV_VAR): new ToStringProviderValue(account.username),
                (ACCESS_KEY_ENV_VAR): new ToStringProviderValue(account.accessKey),
                (LOCAL_ID_ENV_VAR): local.identifier.get()
            )
        }

        local.accessKey.convention(account.accessKey)
    }

    @Nested
    abstract BrowserStackLocal getLocal()

    @Nested
    abstract BrowserStackAccount getAccount()

    abstract NamedDomainObjectContainer<BrowserStackBrowserSpec> getBrowsers()

    void local(Action<? super BrowserStackLocal> action) {
        action.execute(local)
    }

    void account(Action<? super BrowserStackAccount> action) {
        action.execute(account)
    }

    void application(String... urls) {
        local.applicationUrls.addAll(urls.collect { new URL(it) })
    }

    void application(URL... urls) {
        local.applicationUrls.addAll(urls)
    }
}
