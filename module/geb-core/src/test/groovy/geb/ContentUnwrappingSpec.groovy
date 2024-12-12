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

import geb.test.GebSpecWithCallbackServer
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

class ContentUnwrappingSpec extends GebSpecWithCallbackServer {

    def setup() {
        to ContentUnwrappingSpecPage
    }

    def "unwrap module to its declared type"() {
        when:
        def module = theModule as ContentUnwrappingSpecModule

        then:
        module instanceof ContentUnwrappingSpecModule
    }

    def "unwrap module to its parent type"() {
        when:
        def module = theModule as Module

        then:
        module instanceof Module
    }

    def "throw relevant exception on unwrap attempt to incorrect type"() {
        when:
        theModule as ContentUnwrappingSpec

        then:
        thrown(GroovyCastException)
    }
}

class ContentUnwrappingSpecPage extends Page {

    static content = {
        theModule { module(ContentUnwrappingSpecModule) }
    }

}

class ContentUnwrappingSpecModule extends Module { }