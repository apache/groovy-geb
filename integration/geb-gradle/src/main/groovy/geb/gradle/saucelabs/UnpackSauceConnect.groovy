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

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static java.nio.file.Files.copy
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS

abstract class UnpackSauceConnect extends DefaultTask {

    protected final FileSystemOperations fileSystemOperations
    protected final ObjectFactory objectFactory

    @Inject
    UnpackSauceConnect(
        FileSystemOperations fileSystemOperations, ObjectFactory objectFactory, ProjectLayout projectLayout
    ) {
        this.fileSystemOperations = fileSystemOperations
        this.objectFactory = objectFactory

        def fileExtension = Os.isFamily(FAMILY_WINDOWS) ? ".exe" : ""
        outputFile.set(projectLayout.buildDirectory.file("sauce-connect/sc${fileExtension}"))
    }

    @InputFiles
    abstract ConfigurableFileCollection getSauceConnect()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @TaskAction
    void unpack() {
        deleteOutputFile()

        def operations = new SauceConnectOperations(sauceConnect)
        def manager = operations.loadSauceConnectFourManagerClass().newInstance()

        manager.extractZipFile(temporaryDir, operations.operatingSystem)

        def unpackedExecutable = objectFactory.fileTree().from(temporaryDir).tap {
            include("${operations.directory}/${operations.operatingSystem.executable}")
        }.singleFile

        copy(unpackedExecutable.toPath(), outputFile.get().asFile.toPath())
    }

    private void deleteOutputFile() {
        fileSystemOperations.delete {
            delete(outputFile)
        }
    }
}
