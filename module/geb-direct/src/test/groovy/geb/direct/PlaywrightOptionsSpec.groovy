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
package geb.direct

import spock.lang.Specification

class PlaywrightOptionsSpec extends Specification {

    def "defaults select headless Chromium"() {
        when:
        def options = new PlaywrightOptions()

        then:
        options.browserType == 'chromium'
        options.headless
        options.slowMo == 0
        !options.recordVideo
        !options.tracing
        options.viewportWidth == 1280
        options.viewportHeight == 720
    }

    def cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(PlaywrightDriver)
    }

    def "config closure applies options when its driver factory is called"() {
        PlaywrightOptions configuredOptions
        PlaywrightDriver.metaClass.'static'.create = { PlaywrightOptions options ->
            configuredOptions = options
            null
        }

        when:
        def driverFactory = PlaywrightDriver.config {
            browserType = 'firefox'
            headless = false
            slowMo = 125
            tracing = true
        }
        driverFactory.call()

        then:
        configuredOptions.browserType == 'firefox'
        !configuredOptions.headless
        configuredOptions.slowMo == 125
        configuredOptions.tracing
    }

    def 'copy preserves values without sharing mutable options'() {
        given:
        def options = new PlaywrightOptions(
            launchArgs: ['--one'],
            permissions: ['geolocation'],
            extraHTTPHeaders: [trace: 'one'],
            geolocation: [longitude: 1, latitude: 2],
            httpCredentials: [username: 'user', password: 'secret'],
            proxy: [server: 'http://proxy']
        )

        when:
        def copy = options.copy()
        copy.launchArgs << '--two'
        copy.permissions << 'notifications'
        copy.extraHTTPHeaders.trace = 'two'
        copy.geolocation.longitude = 3
        copy.httpCredentials.username = 'other'
        copy.proxy.server = 'http://other-proxy'

        then:
        options.launchArgs == ['--one']
        options.permissions == ['geolocation']
        options.extraHTTPHeaders == [trace: 'one']
        options.geolocation.longitude == 1
        options.httpCredentials.username == 'user'
        options.proxy.server == 'http://proxy'
    }
}
