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

package org.demo.spock

import org.demo.spock.pages.HomePage

import grails.plugin.geb.ContainerGebConfiguration
import grails.plugin.geb.IContainerGebConfiguration

/**
 * Adaptation of {@link HostNameConfigurationSpec}
 */
class SuperSpec extends ContainerGebSpecWithServer implements IContainerGebConfiguration {
    @Override
    String hostName() {
        'super.example.com'
    }
}

@ContainerGebConfiguration(hostName = 'not.example.com')
class NotSuperSpec extends ContainerGebSpecWithServer {}

class InheritedConfigSpec extends SuperSpec {
    void 'should show the right server name when visiting /serverName'() {
        when: 'visiting the server name controller'
        to(HomePage)

        then: 'the emitted hostname is correct'
        // pageSource.contains('Server name: super.example.com')
        currentUrl == "http://super.example.com:8090/"
    }

    def cleanup() {
        sleep(1000) // give the last video time to copy
    }

}

class NotInheritedConfigSpec extends NotSuperSpec {
    void 'should show the right server name when visiting /serverName'() {
        when: 'visiting the server name controller'
        to(HomePage)

        then: 'the emitted hostname is correct'
        // !pageSource.contains('Server name: not.example.com')
        currentUrl != "http://not.example.com:8090/"
    }

    def cleanup() {
        sleep(1000) // give the last video time to copy
    }
}

class ChildPreferenceInheritedConfigSpec extends SuperSpec {
    @Override
    String hostName() {
        'child.example.com'
    }

    void 'should show the right server name when visiting /serverName'() {
        when: 'visiting the server name controller'
        to(HomePage)

        then: 'the emitted hostname is correct'
        // pageSource.contains('Server name: child.example.com')
        currentUrl == "http://child.example.com:8090/"

        when:
        report('whatever')

        then:
        // geb.test.GebTestManager: "Reporting has not been enabled on this GebTestManager yet report() was called"
        Throwable t = thrown(Exception)
        t.message.contains('not been enabled')
    }

    def cleanup() {
        sleep(1000) // give the last video time to copy
    }
}

// No sane person would do this, but lets test anyway
class SuperSuperInheritedConfigSpec extends SuperSpec {
    @Override
    boolean reporting() {
        true
    }
}

class MultipleInheritanceSpec extends SuperSuperInheritedConfigSpec {
    void 'should show the right server name when visiting /serverName'() {
        when: 'visiting the server name controller'
        to(HomePage)

        then: 'the emitted hostname is correct'
        // pageSource.contains('Server name: super.example.com')
        currentUrl == "http://super.example.com:8090/"
        report('multi inheritance report')
    }

    def cleanup() {
        sleep(1000) // give the last video time to copy
    }
}
