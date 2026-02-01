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

import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.SimpleFileServer
import com.sun.net.httpserver.SimpleFileServer.OutputLevel

import java.nio.file.Path
import java.nio.file.Paths

class TestFileServer {

    static HttpServer server

    TestFileServer() {}

    void start() {
        start(8080)
    }

    void start(int port) {
        println "Starting TestFileServer on port $port..."
        // def staticDir = new File('src/integration-test/resources/static').toPath()
        URL staticDirUrl = getClass().getResource("/static")

        // Convert the URL to a URI and then to a Path
        Path staticDirPath = Paths.get(staticDirUrl.toURI())
        def addr = new InetSocketAddress(port)

        // Use JDK's built-in SimpleFileServer to serve the static content
        server = SimpleFileServer.createFileServer(addr, staticDirPath, OutputLevel.INFO)
        server.start()
        println "TestFileServer started on port 8080"
    }

    void stop() {
        stop(0)
    }



    void stop(int delay) {
        println "Stopping TestFileServer..."
        if (server) {
            server.stop(delay)
            println "TestFileServer stopped"
        } else {
            println "TestFileServer not found!"
        }
    }

}
