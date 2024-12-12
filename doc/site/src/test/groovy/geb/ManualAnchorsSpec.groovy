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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.ApplicationUnderTest
import spock.lang.AutoCleanup
import spock.lang.Specification

class ManualAnchorsSpec extends Specification {

    @AutoCleanup
    ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()

    def "manual headers have unique ids"() {
        when:
        def duplicates = parseManual().select("h2, h3, h4, h5, h6")*.attr("id").countBy { it }.findAll { it.value > 1 }

        then:
        duplicates.isEmpty()
    }

    private Document parseManual() {
        Jsoup.parse(aut.httpClient.get("manual/snapshot/").body.text)
    }

}
