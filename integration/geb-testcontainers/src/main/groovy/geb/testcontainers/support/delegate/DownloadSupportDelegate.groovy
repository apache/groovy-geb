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
package geb.testcontainers.support.delegate

import geb.download.DownloadSupport
import geb.testcontainers.ContainerGebSpec
import groovy.transform.CompileStatic
import groovy.transform.SelfType

/**
 * Handles delegation to the DownloadSupport instance so that the Geb API can be used directly in the test.
 * <p>
 * As method parameter names are not available in the Geb artifacts we are delegating manually to
 * get the best possible IDE support and user experience.
 *
 * @author Mattias Reichel
 * @since 4.2
 */
@CompileStatic
@SelfType(ContainerGebSpec)
trait DownloadSupportDelegate implements DownloadSupport {

    @Override
    HttpURLConnection download() {
        ContainerGebSpec.downloadSupport.download()
    }

    @Override
    HttpURLConnection download(Map options) {
        ContainerGebSpec.downloadSupport.download(options)
    }

    @Override
    HttpURLConnection download(String uri) {
        ContainerGebSpec.downloadSupport.download(uri)
    }

    @Override
    InputStream downloadStream() {
        ContainerGebSpec.downloadSupport.downloadStream()
    }

    @Override
    InputStream downloadStream(Map options) {
        ContainerGebSpec.downloadSupport.downloadStream(options)
    }

    @Override
    InputStream downloadStream(Map options, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadStream(options, connectionConfig)
    }

    @Override
    InputStream downloadStream(String uri) {
        ContainerGebSpec.downloadSupport.downloadStream(uri)
    }

    @Override
    InputStream downloadStream(String uri, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadStream(uri, connectionConfig)
    }

    @Override
    InputStream downloadStream(Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadStream(connectionConfig)
    }

    @Override
    String downloadText() {
        ContainerGebSpec.downloadSupport.downloadText()
    }

    @Override
    String downloadText(Map options) {
        ContainerGebSpec.downloadSupport.downloadText(options)
    }

    @Override
    String downloadText(Map options, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadText(options, connectionConfig)
    }

    @Override
    String downloadText(String uri) {
        ContainerGebSpec.downloadSupport.downloadText(uri)
    }

    @Override
    String downloadText(String uri, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadText(uri, connectionConfig)
    }

    @Override
    String downloadText(Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadText(connectionConfig)
    }

    @Override
    byte[] downloadBytes() {
        ContainerGebSpec.downloadSupport.downloadBytes()
    }

    @Override
    byte[] downloadBytes(Map options) {
        ContainerGebSpec.downloadSupport.downloadBytes(options)
    }

    @Override
    byte[] downloadBytes(Map options, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadBytes(options, connectionConfig)
    }

    @Override
    byte[] downloadBytes(Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadBytes(connectionConfig)
    }

    @Override
    byte[] downloadBytes(String uri) {
        ContainerGebSpec.downloadSupport.downloadBytes(uri)
    }

    @Override
    byte[] downloadBytes(String uri, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadBytes(uri, connectionConfig)
    }

    @Override
    Object downloadContent() {
        ContainerGebSpec.downloadSupport.downloadContent()
    }

    @Override
    Object downloadContent(Map options) {
        ContainerGebSpec.downloadSupport.downloadContent(options)
    }

    @Override
    Object downloadContent(Map options, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadContent(options, connectionConfig)
    }

    @Override
    Object downloadContent(String uri) {
        ContainerGebSpec.downloadSupport.downloadContent(uri)
    }

    @Override
    Object downloadContent(String uri, Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadContent(uri, connectionConfig)
    }

    @Override
    Object downloadContent(Closure connectionConfig) {
        ContainerGebSpec.downloadSupport.downloadContent(connectionConfig)
    }
}