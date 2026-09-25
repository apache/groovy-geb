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

class PlaywrightDriverSpec extends Specification {

    def cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(PlaywrightDriver)
    }

    def 'config returns an independent Closure-backed driver factory'() {
        given:
        List<PlaywrightOptions> created = []
        PlaywrightDriver.metaClass.'static'.create = { PlaywrightOptions options -> created << options; null }

        when:
        Closure factory = PlaywrightDriver.config {
            browserType = 'webkit'
            launchArgs = ['--first']
        }
        factory.call()
        factory.call()

        then:
        created*.browserType == ['webkit', 'webkit']
        !created[0].is(created[1])
        !created[0].launchArgs.is(created[1].launchArgs)
    }
}
