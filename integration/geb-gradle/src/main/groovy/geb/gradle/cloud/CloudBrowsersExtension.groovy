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

import geb.gradle.ConditionalTaskDependency
import geb.gradle.SystemPropertiesCommandLineArgumentProvider
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

import javax.inject.Inject

abstract class CloudBrowsersExtension {
    protected final TaskContainer tasks
    protected final ObjectFactory objectFactory
    protected final TaskProvider<? extends Task> allTestsLifecycleTask
    protected final TaskProvider<? extends Task> openTunnelInBackgroundTask
    protected final TaskProvider<? extends Task> closeTunnelTask
    protected final String tasksGroup

    @Inject
    CloudBrowsersExtension(
        ObjectFactory objectFactory, TaskContainer tasks, TaskProvider<? extends Task> allTestsLifecycleTask,
        TaskProvider<? extends Task> openTunnelInBackgroundTask, TaskProvider<? extends Task> closeTunnelTask,
        String tasksGroup
    ) {
        this.tasks = tasks
        this.objectFactory = objectFactory
        this.allTestsLifecycleTask = allTestsLifecycleTask
        this.openTunnelInBackgroundTask = openTunnelInBackgroundTask
        this.closeTunnelTask = closeTunnelTask
        this.tasksGroup = tasksGroup

        allTestsLifecycleTask.configure {
            it.group = tasksGroup
        }

        useTunnel.convention(true)

        browsers.configureEach { addTestTask(it) }
    }

    abstract Property<Boolean> getUseTunnel()

    abstract NamedDomainObjectContainer<? extends BrowserSpec> getBrowsers()

    abstract DomainObjectSet<TaskProvider<Test>> getTestTasks()

    void browsers(Action<NamedDomainObjectContainer<BrowserSpec>> action) {
        action.execute(browsers)
    }

    void task(Action<Test> configuration) {
        testTasks.all { TaskProvider taskProvider ->
            taskProvider.configure(configuration)
        }
    }

    void additionalTask(String namePrefix, Action<Test> configuration) {
        browsers.all {
            def task = addTestTask(it, namePrefix)
            task.configure(configuration)
        }
    }

    protected TaskProvider<Test> addTestTask(BrowserSpec browser, String prefix = null) {
        def name = prefix ? "${prefix}${browser.displayName.capitalize()}" : browser.displayName

        def testTask = tasks.register("${name}Test", Test) { Test task ->
            task.group = tasksGroup
            task.dependsOn(new ConditionalTaskDependency(useTunnel.&get, openTunnelInBackgroundTask))
            finalizedBy(new ConditionalTaskDependency(useTunnel.&get, closeTunnelTask))

            def reporting = project.extensions.getByType(ReportingExtension)
            def gebReportsDir = reporting.file("geb/${task.name}")
            outputs.dir(gebReportsDir)
            jvmArgumentProviders.add(
                new SystemPropertiesCommandLineArgumentProvider('geb.build.reportsDir': gebReportsDir.absolutePath)
            )
            doLast(owner.objectFactory.newInstance(DecorateJUnitXmlReportsAction))
        }

        allTestsLifecycleTask.configure {
            it.dependsOn testTask
        }

        browser.addTask(testTask)
        testTasks.add(testTask)

        testTask
    }

    static class DecorateJUnitXmlReportsAction implements Action<Test> {

        private final FileSystemOperations fileSystemOperations

        @Inject
        DecorateJUnitXmlReportsAction(FileSystemOperations fileSystemOperations) {
            this.fileSystemOperations = fileSystemOperations
        }

        @Override
        void execute(Test testTask) {
            def nameForReport = testTask.name[0..-5]
            def originalXmlFiles = testTask.reports.junitXml.outputLocation.asFileTree.matching {
                it.include '**/*.xml'
            }.files

            fileSystemOperations.copy {
                from originalXmlFiles
                into testTask.reports.junitXml.outputLocation
                rename(/^(.*)\.xml$/, '$1-decorated.xml')
                filter { it.replaceAll("(testsuite|testcase) name=\"(.+?)\"", "\$1 name=\"\$2 ($nameForReport)\"") }
            }

            fileSystemOperations.delete {
                delete(originalXmlFiles)
            }
        }
    }
}
