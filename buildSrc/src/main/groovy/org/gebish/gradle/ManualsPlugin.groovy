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
package org.gebish.gradle

import groovy.text.SimpleTemplateEngine
import org.gebish.gradle.task.GatherManuals
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.WriteProperties

class ManualsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply("geb.base")
        def baseExtension = project.extensions.getByType(BaseExtension)

        def gatherManualsTask = project.tasks.register("gatherManuals", GatherManuals) {
            into project.layout.buildDirectory.dir("manuals")
        }

        ManualsExtension manualsExtension = project.extensions.create('manuals', ManualsExtension, project)

        configureCurrentManualGathering(project, baseExtension, manualsExtension, gatherManualsTask)
        configureIndexTask(project)
    }

    private void configureCurrentManualGathering(
        Project project, BaseExtension baseExtension, ManualsExtension manualsExtension,
        TaskProvider<GatherManuals> gatherManualsTask
    ) {
        gatherManualsTask.configure {
            if (baseExtension.isSnapshot()) {
                gatherManual('snapshot', manualsExtension.currentManual)
                gatherPublishedManual(manualsExtension.includedManuals.map { it.last() }, 'current')
            } else {
                gatherManual('current', manualsExtension.currentManual)
                gatherManual(project.version.toString(), manualsExtension.currentManual)
            }
        }
    }

    private void configureIndexTask(Project project) {
        project.tasks.register("generateIndex", WriteProperties) {
            destinationFile = project.layout.buildDirectory.file("index.html")
            doLast {
                def baseExtension = project.extensions.getByType(BaseExtension)
                def ext = project.extensions.getByName("manuals")
                List<String> includedManuals = ext.includedManuals.get()
                String currentVersion = baseExtension.isSnapshot() ? includedManuals.last() : project.version
                String snapshot = baseExtension.isSnapshot() ? project.version : ''
                List<String> oldManuals = includedManuals.findAll { v -> v != currentVersion }
                    .collect { v -> SoftwareVersion.of(v)}
                    .sort()
                    .reverse()
                    .collect { sv -> sv.toString()}
                Map<String, String> model = [old: oldManuals, current: currentVersion, snapshot: snapshot]
                String template = ext.indexTemplate.asFile.get().text
                String html = new SimpleTemplateEngine().createTemplate(template).make(model).toString()
                destinationFile.get().asFile.text = html
            }
        }
    }
}
