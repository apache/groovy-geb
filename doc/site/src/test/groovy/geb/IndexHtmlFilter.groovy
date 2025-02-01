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
package geb

import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher

/**
 * Redirects from /manual/5.0/ to /manual/5.0/index.html
 */
@Filter("/manual/**")
class IndexHtmlFilter implements HttpServerFilter {
    private static final String SLASH = "/"
    private static final String SLASH_JS = SLASH + "js"
    private static final String SLASH_CSS = SLASH + "css"
    private static final String SLASH_IMAGES = SLASH + "images"
    private static final List<String> ASSETS = [SLASH_CSS, SLASH_IMAGES, SLASH_JS]

    @Override
    Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String path = request.path
        if (path.endsWith(SLASH) && ASSETS.stream().noneMatch { prefix ->  path.contains(prefix) }) {
            String newPath = "${path}index.html"
            return Publishers.just(HttpResponse.seeOther(URI.create(newPath)))
        }
        chain.proceed(request)
    }
}
