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
package geb.download

import geb.Initializable

class UninitializedDownloadSupport implements DownloadSupport {

    private final Initializable initializable

    UninitializedDownloadSupport(Initializable initializable) {
        this.initializable = initializable
    }

    @Override
    HttpURLConnection download(Map options = [:]) {
        throw initializable.uninitializedException()
    }

    @Override
    HttpURLConnection download(String uri) {
        throw initializable.uninitializedException()
    }

    @Override
    InputStream downloadStream(Map options = [:], Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    InputStream downloadStream(String uri, Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    InputStream downloadStream(Closure connectionConfig) {
        throw initializable.uninitializedException()
    }

    @Override
    String downloadText(Map options = [:], Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    String downloadText(String uri, Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    String downloadText(Closure connectionConfig) {
        throw initializable.uninitializedException()
    }

    @Override
    byte[] downloadBytes(Map options = [:], Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    byte[] downloadBytes(Closure connectionConfig) {
        throw initializable.uninitializedException()
    }

    @Override
    byte[] downloadBytes(String uri, Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    Object downloadContent(Map options = [:], Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    Object downloadContent(String uri, Closure connectionConfig = null) {
        throw initializable.uninitializedException()
    }

    @Override
    Object downloadContent(Closure connectionConfig) {
        throw initializable.uninitializedException()
    }
}
