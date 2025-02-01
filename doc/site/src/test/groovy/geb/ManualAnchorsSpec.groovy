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

import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import spock.lang.AutoCleanup
import spock.lang.Specification

class ManualAnchorsSpec extends Specification {

    @AutoCleanup
    EmbeddedServer aut = ApplicationContext.run(EmbeddedServer)

    def "manual headers have unique ids"() {
        when:
        def duplicates = parseManual().select("h2, h3, h4, h5, h6")*.attr("id").countBy { it }.findAll { it.value > 1 }

        then:
        duplicates.isEmpty()
    }

    private Document parseManual() {
        BlockingHttpClient client = aut.applicationContext.createBean(HttpClient, aut.URL).toBlocking()
        String json = client.retrieve("/manual/snapshot/", String)
        Jsoup.parse(json)
    }

}
