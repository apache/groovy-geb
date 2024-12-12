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
package geb.conf

import geb.Browser
import geb.Configuration
import spock.lang.Specification
import spock.lang.Unroll

class BaseUrlConfigurationSpec extends Specification {

    def getBaseUrl(String constructorBaseUrl, String configBaseUrl) {
        def c = new ConfigObject()
        c.cacheDriver = false
        if (configBaseUrl) {
            c.baseUrl = configBaseUrl
        }

        def args = [:]
        if (constructorBaseUrl) {
            args.baseUrl = constructorBaseUrl
        }

        new Browser(args, new Configuration(c)).baseUrl
    }

    @Unroll("expectedBaseUrl = #expectedBaseUrl with (#constructor, #config)")
    def "calculate base url"() {
        expect:
        getBaseUrl(constructor, config) == expectedBaseUrl

        where:
        constructor | config | expectedBaseUrl
        null        | null   | null
        null        | "abc"  | "abc"
        "abc"       | null   | "abc"
        "abc"       | "def"  | "abc"
    }

    def "can set explicit base on configuration"() {
        given:
        def c = new Configuration()
        c.baseUrl = "abc"

        when:
        def b = new Browser(c)

        then:
        b.baseUrl == "abc"
    }

    def "null base url throws reasonable error message"() {
        given:
        def c = new Configuration()
        c.baseUrl = null
        def b = new Browser(c)

        when:
        b.go("abc")

        then:
        thrown(geb.error.NoBaseUrlDefinedException)
    }

}