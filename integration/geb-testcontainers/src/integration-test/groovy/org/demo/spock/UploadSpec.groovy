/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.demo.spock

import geb.module.FileInput
import org.demo.spock.pages.UploadSuccessPage

import grails.plugin.geb.ContainerGebSpec
import org.demo.spock.pages.UploadPage
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.TempDir
import spock.lang.Title

import java.nio.file.Path

@Title("should be able to upload files")
class UploadSpec extends ContainerGebSpecWithServer {

    @TempDir
    Path tempDir

    @Requires({ os.windows })
    def "upload files on a Windows host"() {
        given:
        File fileInContainer = createFileInputSource(
                'src/integration-test/resources/assets/upload-test.txt',
                '/tmp/upload-test.txt'
        )
        and:
        def uploadPage = to(UploadPage)

        when:
        container.copyFileFromContainer("/tmp/upload-test.txt", tempDir.toString() + "/upload-test.txt")
        and:
        File fileFromContainer = new File(tempDir.toString(), "upload-test.txt")

        then:
        fileFromContainer.bytes.length == 26

        when:
        uploadPage.fileInput.file = fileInContainer
        and:
        uploadPage.submitBtn.click()

        then:
        at(UploadSuccessPage)
    }

    @IgnoreIf({ os.windows })
    def "upload files on a non-Windows host"() {
        given:
        File fileInContainer = createFileInputSource(
                'src/integration-test/resources/assets/upload-test.txt',
                '/tmp/upload-test.txt'
        )
        and:
        def uploadPage = to(UploadPage)

        when:
        container.copyFileFromContainer("/tmp/upload-test.txt", tempDir.toString() + "/upload-test.txt")
        and:
        File fileFromContainer = new File(tempDir.toString(), "upload-test.txt")

        then:
        fileFromContainer.bytes.length == 26

        when:
        uploadPage.fileInput.file = fileInContainer
        and:
        uploadPage.submitBtn.click()

        then:
        at(UploadSuccessPage)
    }

    def "upload file to external site"() {
        given:
        go("https://the-internet.herokuapp.com/upload")
        File uploadFile = new File('src/integration-test/resources/assets/upload-test.txt')
        $('input', id: 'file-upload').module(FileInput).file = uploadFile

        when:
        $("input", id: "file-submit").click()

        then:
        $("div.example").$("h3").text() == "File Uploaded!"
        $("div.example").$("div", id: "uploaded-files").text() == "upload-test.txt"

    }

    def cleanup() {
        sleep(1000) // give the last video time to copy
        container.execInContainer("rm /tmp/upload-test.txt")
    }


}