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
package geb.gradle.cloud

import geb.gradle.ToStringProviderValue
import org.gradle.api.DomainObjectSet
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

import javax.inject.Inject

import static com.google.common.base.CaseFormat.LOWER_CAMEL
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE

abstract class BrowserSpec {
    final String name
    final String displayName

    @Inject
    BrowserSpec(String name) {
        this.name = name
        String[] split = name.split("_", 3)

        String browserName = split[0]
        capabilities.put("browserName", browserName)

        String platformName
        if (split.size() > 1) {
            platformName = split[1]
            capabilities.put("platformName", platformName.toUpperCase())
        }

        String browserVersion
        if (split.size() > 2) {
            browserVersion = split[2]
            capabilities.put("browserVersion", browserVersion)
        }

        displayName = "${camelCase(browserName)}${platformName?.capitalize() ?: ""}${browserVersion?.capitalize() ?: ""}"

        setCapabilitiesOnTasks()
    }

    abstract String getCloudProvider()

    abstract DomainObjectSet<TaskProvider<Test>> getTasks()

    abstract MapProperty<String, String> getCapabilities()

    void capability(String capability, String value) {
        capabilities.put(capability, value)
    }

    void capabilities(Map<String, String> capabilities) {
        getCapabilities().putAll(capabilities)
    }

    void addTask(TaskProvider<Test> task) {
        tasks.add(task)
    }

    protected void configureCapabilitiesOnTask(Test task) {
        task.systemProperty "geb.${cloudProvider}.browser", new ToStringProviderValue(capabilitiesStringProvider)
    }

    protected Provider<String> getCapabilitiesStringProvider() {
        capabilities.map { capabilitiesMap ->
            StringWriter writer = new StringWriter()
            new Properties().tap { putAll(capabilitiesMap) }.store(writer, null)

            writer.toString().readLines().findAll {
                !it.startsWith("#")
            }.join(System.lineSeparator())
        }
    }

    protected void setCapabilitiesOnTasks() {
        tasks.all { TaskProvider<Test> taskProvider ->
            taskProvider.configure {
                configureCapabilitiesOnTask(it)
            }
        }
    }

    protected String camelCase(String camelCaseTextWithSpaces) {
        def lowerUnderscoreText = LOWER_CAMEL.to(LOWER_UNDERSCORE, camelCaseTextWithSpaces).replaceAll(' ', '_').toLowerCase()
        LOWER_UNDERSCORE.to(LOWER_CAMEL, lowerUnderscoreText)
    }
}
