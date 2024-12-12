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
package geb.junit4

import geb.junit.error.IncompatibleTestClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import spock.lang.Specification
import spock.lang.Unroll

class GebTestUtilSpec extends Specification {

    @Unroll("an informative error is thrown when #scenario is used in a class without a static testManager property")
    def "an informative error is thrown when test manager rules are used in classes without a static testManager property"() {
        when:
        def result = new JUnitCore().run(Request.aClass(test))

        then:
        result.failures*.exception*.class == [IncompatibleTestClass]

        where:
        scenario                      | test
        "GebTestManagerRule"          | UsingRuleWithoutTestManagerTest
        "GebReportingTestManagerRule" | UsingReportingRuleWithoutTestManagerTest
    }
}

@Category(DoNotRunFromGradle)
class UsingRuleWithoutTestManagerTest {
    @Rule
    public GebTestManagerRule gebTestManagerRule = new GebTestManagerRule()

    @Test
    void dummyTest() {
    }
}

@Category(DoNotRunFromGradle)
class UsingReportingRuleWithoutTestManagerTest {
    @ClassRule
    public static GebTestManagerClassRule gebReportingTestManagerRule = new GebTestManagerClassRule()

    @Test
    void dummyTest() {
    }
}
