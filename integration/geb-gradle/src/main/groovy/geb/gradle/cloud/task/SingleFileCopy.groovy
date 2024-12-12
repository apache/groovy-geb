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
package geb.gradle.cloud.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static java.nio.file.Files.copy

abstract class SingleFileCopy extends DefaultTask {

    private final FileSystemOperations fileSystemOperations

    @InputFiles
    abstract ConfigurableFileCollection getSource()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @Inject
    SingleFileCopy(FileSystemOperations fileSystemOperations) {
        this.fileSystemOperations = fileSystemOperations
    }

    @TaskAction
    void copy() {
        deleteOutputFile()
        copy(source.asFileTree.singleFile.toPath(), outputFile.get().asFile.toPath())
    }

    private void deleteOutputFile() {
        fileSystemOperations.delete {
            delete(outputFile)
        }
    }
}
