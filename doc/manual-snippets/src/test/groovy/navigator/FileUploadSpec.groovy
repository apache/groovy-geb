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
package navigator

import geb.test.GebSpecWithCallbackServer
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.http.entity.ContentType
import spock.lang.TempDir

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FileUploadSpec extends GebSpecWithCallbackServer {

    @TempDir
    File temporaryDir

    def "uploadig a file"() {
        given:
        def uploadedFile = File.createTempFile("upload", null, temporaryDir) << "from file"
        setupServer()

        when:
        go()
        // tag::upload[]
        $("form").csvFile = uploadedFile.absolutePath
        // end::upload[]
        $('form input[type="submit"]').click()

        then:
        $().text() == "from file"
    }

    private void setupServer() {
        responseHtml """
            <html>
                <form method="post" enctype="multipart/form-data">
                    // tag::html[]
                    <input type="file" name="csvFile"/>
                    // end::html[]
                    <input type="submit"/>
                </form>
            </html>
        """
        callbackServer.post = { HttpServletRequest request, HttpServletResponse response ->
            response.contentType = ContentType.TEXT_HTML.toString()
            response.characterEncoding = UTF8
            response.writer << """
                <html>${extractUploadedFileText(request)}</html>
            """
        }
    }

    private String extractUploadedFileText(HttpServletRequest request) {
        def fileItemFactory = new DiskFileItemFactory()
        def files = new ServletFileUpload(fileItemFactory).parseRequest(request)
        files.first().inputStream.text
    }

}
