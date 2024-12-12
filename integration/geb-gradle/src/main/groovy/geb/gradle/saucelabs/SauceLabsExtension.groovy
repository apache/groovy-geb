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
package geb.gradle.saucelabs

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

import static geb.gradle.saucelabs.SauceAccount.ACCESS_KEY_ENV_VAR
import static geb.gradle.saucelabs.SauceAccount.USER_ENV_VAR
import static geb.gradle.saucelabs.SauceConnect.TUNNEL_ID_ENV_VAR

abstract class SauceLabsExtension extends CloudBrowsersExtension {

    @Inject
    SauceLabsExtension(
        ObjectFactory objectFactory, TaskContainer tasks, TaskProvider<? extends Task> allTestsLifecycleTask,
        TaskProvider<? extends Task> openTunnelInBackgroundTask, TaskProvider<? extends Task> closeTunnelTask,
        String tasksGroup
    ) {
        super(objectFactory, tasks, allTestsLifecycleTask, openTunnelInBackgroundTask, closeTunnelTask, tasksGroup)

        connect.username.convention(account.username)
        connect.accessKey.convention(account.accessKey)

        task {
            it.environment(
                (USER_ENV_VAR): new ToStringProviderValue(account.username),
                (ACCESS_KEY_ENV_VAR): new ToStringProviderValue(account.accessKey),
                (TUNNEL_ID_ENV_VAR): new ToStringProviderValue(connect.identifier)
            )
        }
    }

    @Nested
    abstract SauceConnect getConnect()

    @Nested
    abstract SauceAccount getAccount()

    abstract NamedDomainObjectContainer<SauceLabsBrowserSpec> getBrowsers()

    void connect(Action<? super SauceConnect> action) {
        action.execute(connect)
    }

    void account(Action<? super SauceAccount> action) {
        action.execute(account)
    }
}
