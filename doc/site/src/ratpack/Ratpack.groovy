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


import geb.site.GebishClientErrorHandler
import geb.site.Manuals
import geb.site.Model
import ratpack.error.ClientErrorHandler
import ratpack.groovy.template.TextTemplateModule
import ratpack.handling.Context
import ratpack.http.HttpUrlBuilder
import ratpack.server.PublicAddress

import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY
import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
    serverConfig {
        props(getClass().getResource("/ratpack.properties"))
        require("/manuals", Manuals)
    }
    bindings {
        module(TextTemplateModule) {
            it.staticallyCompile = true
        }
        bind(ClientErrorHandler, GebishClientErrorHandler)
        add Date, new Date()
    }
    handlers {
        all {
            def uri = get(PublicAddress).get()
            def host = uri.host
            if (host != "localhost") {
                if (host != "gebish.org" || uri.scheme != "https") {
                    def redirectUri = HttpUrlBuilder.https()
                            .host("gebish.org")
                            .path(request.path)
                    redirect(MOVED_PERMANENTLY.code(), redirectUri)
                    return
                }
            }
            next()
        }

        files {
            dir("public")
            indexFiles("index.html")
        }

        get { Context context, Date startupTime, Manuals manuals ->
            lastModified(startupTime) {
                render groovyTemplate(Model.get(manuals), 'main.html')
            }
        }
    }
}

