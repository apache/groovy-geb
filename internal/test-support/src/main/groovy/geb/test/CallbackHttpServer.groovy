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
package geb.test

import geb.Configuration
import geb.ConfigurationLoader
import groovy.xml.MarkupBuilder
import org.apache.http.entity.ContentType
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.function.Supplier

class CallbackHttpServer extends TestHttpServer {

    protected static final String UTF8 = "utf8"

    Closure get
    Closure post
    Closure put
    Closure delete

    CallbackHttpServer(Configuration configuration = new ConfigurationLoader().conf) {
        super(configuration)
    }

    CallbackHttpServer(Supplier<Configuration> configurationSupplier) {
        super(configurationSupplier)
    }

    void responseHtml(Closure htmlMarkup) {
        get = { HttpServletRequest request, HttpServletResponse response ->
            synchronized (this) { // MarkupBuilder has some static state, so protect
                try {
                    setupResponse(response)
                    def writer = new OutputStreamWriter(response.outputStream, UTF8)
                    writer << "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n"
                    new MarkupBuilder(writer).html {
                        htmlMarkup.delegate = delegate
                        htmlMarkup.resolveStrategy = Closure.DELEGATE_FIRST
                        if (htmlMarkup.maximumNumberOfParameters < 2) {
                            htmlMarkup(request)
                        } else {
                            htmlMarkup(request, response)
                        }
                    }
                    writer.flush()
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    void responseHtml(String html) {
        get = { HttpServletRequest request, HttpServletResponse response ->
            setupResponse(response)
            response.writer << html
        }
    }

    void html(Closure html) {
        responseHtml(html)
    }

    void html(String html) {
        responseHtml(html)
    }

    protected addServlets(ServletContextHandler context) {
        context.addServlet(new ServletHolder(new CallbackServlet(this)), "/*")
    }

    protected setupResponse(HttpServletResponse response) {
        response.contentType = ContentType.TEXT_HTML.toString()
        response.characterEncoding = UTF8
        response.addHeader("Cache-Control", "Cache-Control: private, no-cache, no-store, must-revalidate")
    }
}