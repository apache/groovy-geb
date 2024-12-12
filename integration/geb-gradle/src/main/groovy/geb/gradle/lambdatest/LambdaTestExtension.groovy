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
package geb.gradle.lambdatest

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

abstract class LambdaTestExtension extends CloudBrowsersExtension {

    @Inject
    LambdaTestExtension(
        ObjectFactory objectFactory, TaskContainer tasks, TaskProvider<? extends Task> allTestsLifecycleTask,
        TaskProvider<? extends Task> openTunnelInBackgroundTask, TaskProvider<? extends Task> closeTunnelTask,
        String tasksGroup
    ) {
        super(objectFactory, tasks, allTestsLifecycleTask, openTunnelInBackgroundTask, closeTunnelTask, tasksGroup)

        task { test ->
            test.environment(
                (LambdaTestAccount.USER_ENV_VAR): new ToStringProviderValue(account.username),
                (LambdaTestAccount.ACCESS_KEY_ENV_VAR): new ToStringProviderValue(account.accessKey),
                (LambdaTestTunnelOps.TUNNEL_NAME_ENV_VAR): new ToStringProviderValue(tunnelOps.tunnelName)
            )
        }

        tunnelOps.username.convention(account.username)
        tunnelOps.accessKey.convention(account.accessKey)
    }

    @Nested
    abstract LambdaTestTunnelOps getTunnelOps()

    @Nested
    abstract LambdaTestAccount getAccount()

    abstract NamedDomainObjectContainer<LambdaTestBrowserSpec> getBrowsers()

    void tunnelOps(Action<? super LambdaTestTunnelOps> action) {
        action.execute(tunnelOps)
    }

    void account(Action<? super LambdaTestAccount> action) {
        action.execute(account)
    }
}
