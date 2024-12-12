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
package geb.transform.implicitassertions

import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

class ImplicitAssertionsTransformationSpec extends Specification {

    @Unroll("expression '#closureBody' is asserted and fails")
    def "various falsy expressions are asserted and fail"() {
        when:
        getTransformedInstanceWithClosureBody(closureBody).runWaitFor()

        then:
        PowerAssertionError error = thrown()
        error.message.contains(closureBody)

        where:
        closureBody << ['false', 'null', 'booleanMethod(false)', '1 == 2', 'booleanMethod(false) == true']
    }

    def "transformation is applied to multiple lines of the closure"() {
        when:
        getTransformedInstanceWithClosureBody(
                'true',
                'false'
        ).runWaitFor()

        then:
        PowerAssertionError error = thrown()
        error.message.contains('false')
    }

    @Unroll("expression '#closureBody' passes")
    def "various truly expressions pass"() {
        when:
        def returnValue = getTransformedInstanceWithClosureBody(closureBody).runWaitFor()

        then:
        noExceptionThrown()

        and:
        returnValue == expectedReturnValue

        where:
        closureBody                     | expectedReturnValue
        'true'                          | true
        '1'                             | 1
        'booleanMethod(true)'           | true
        'booleanMethod(false) == false' | true
    }

    @Unroll("expression '#closureBody' is ignored")
    def "various ignored expressions pass"() {
        when:
        getTransformedInstanceWithClosureBody(closureBody).runWaitFor()

        then:
        noExceptionThrown()

        where:
        closureBody << ['def a = false', 'assert true', 'voidMethod()', 'staticVoidMethod()']
    }

    @Unroll("compilation error is reported when not allowed statements are found")
    def "compilation error is reported on assignment statements in waitFor closure body"() {
        when:
        getTransformedInstanceWithClosureBody(closureBody)

        then:
        MultipleCompilationErrorsException exception = thrown()
        exception.message.contains(message)

        where:
        closureBody        | message
        'a = 2'            | "Expected a condition, but found an assignment. Did you intend to write '==' ?"
        'spreadCall(*foo)' | 'Spread expressions are not allowed here'
    }

    def "waitFor closure returns true if all assertions pass"() {
        expect:
        getTransformedInstanceWithClosureBody('true').runWaitFor() == true
    }

    def "implicit assertions in waitFor closure can be disabled"() {
        expect:
        getTransformedInstanceWithClosureBody('false').runWaitForWithDisabledImplicitAssertions() == false
    }

    def "transform is also applied to at closures"() {
        when:
        getTransformedInstanceWithClosureBody('false').at()

        then:
        PowerAssertionError error = thrown()
        error.message.contains('false')
    }

    def "transform is also applied to refreshWaitFor calls"() {
        when:
        getTransformedInstanceWithClosureBody('false').runRefreshWaitFor()

        then:
        PowerAssertionError error = thrown()
        error.message.contains('false')
    }

    def "implicit assertions in runRefreshWaitFor closure can be disabled"() {
        expect:
        getTransformedInstanceWithClosureBody('false').runRefreshWaitForWithDisabledImplicitAssertions() == false
    }

    def "transform is also applied to at() calls"() {
        when:
        getTransformedInstanceWithClosureBody('false').runAt()

        then:
        PowerAssertionError error = thrown()
        error.message.contains('false')
    }

    @Issue("https://github.com/geb/issues/issues/462")
    @Unroll("at closures return true even if the last method call is to #closureBody")
    def "at closures return true even if the last method call is to a void method"() {
        expect:
        getTransformedInstanceWithClosureBody(closureBody).verifyAt() == true

        where:
        closureBody << ['voidMethod()', 'staticVoidMethod()']
    }

    @Issue("https://github.com/geb/issues/issues/462")
    @Unroll("return value from waitFor block is unchanged if the last method call is to #closureBody")
    def "return value from waitFor block is unchanged if the last method call is to a void method"() {
        expect:
        getTransformedInstanceWithClosureBody(closureBody).runWaitFor() == null

        where:
        closureBody << ['voidMethod()', 'staticVoidMethod()']
    }

    @Issue("https://github.com/geb/issues/issues/398")
    def "can transform statements that contain method call expressions on null values"() {
        when:
        getTransformedInstanceWithClosureBody('nullReturningMethod()?.contains("foo")').runWaitFor()

        then:
        thrown(PowerAssertionError)
    }

    private String makeCodeTemplate(String... closureBody) {
        def resource = getClass().classLoader.getResource('TransformedClass.template')
        def template = new SimpleTemplateEngine().createTemplate(resource)
        template.make([closureBody: closureBody.join('\n')]).toString()
    }

    private Class getTransformedClassWithClosureBody(String... code) {
        new GroovyClassLoader().parseClass(makeCodeTemplate(code))
    }

    private getTransformedInstanceWithClosureBody(String... code) {
        getTransformedClassWithClosureBody(code).newInstance()
    }

}
