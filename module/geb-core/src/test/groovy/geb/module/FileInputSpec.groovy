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
package geb.module

import geb.test.GebSpecWithCallbackServer
import spock.lang.TempDir

class FileInputSpec extends GebSpecWithCallbackServer {

    @TempDir
    File temporaryDir

    def setup() {
        html {
            input(type: "file")
        }
    }

    FileInput getInput() {
        $("input").module(FileInput)
    }

    def "getting and setting file"() {
        given:
        def file = new File(temporaryDir, "example.txt")

        when:
        input.file = file

        then:
        input.value().endsWith(file.name)
    }

    def "can get and set a file on an empty navigator based file input module"() {
        given:
        def input = $("#i-dont-exist").module(FileInput)

        when:
        input.file = new File(temporaryDir, "example.txt")

        then:
        noExceptionThrown()
        input.value() == null
    }
}
