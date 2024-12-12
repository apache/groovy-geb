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
import spock.lang.Unroll

class ContentAccessShortcutsSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml {
            form {
                input(type: 'text', id: 'text', value: 'Initial text')
                input(type: 'checkbox', id: 'checked-box', value: '123', checked: 'checked')
                input(type: 'checkbox', id: 'unchecked-box', value: '123')
                input(type: 'radio', name: 'radio', value: 'first')
                input(type: 'radio', name: 'radio', value: 'second', checked: 'checked')
                input(type: 'radio', name: 'radio', value: 'third')
                select(id: 'select') {
                    option('Option #1', value: '1')
                    option('Option #2', value: '2', selected: 'selected')
                }
                select(id: 'multiselect', multiple: 'multiple') {
                    option('Option #1', value: '1')
                    option('Option #2', value: '2', selected: 'selected')
                    option('Option #3', value: '3')
                    option('Option #4', value: '4', selected: 'selected')
                }
            }
        }
    }

    @Unroll
    def "content values can be read using equals"() {
        when:
        to ContentAccessShortcutsPage
        def container = contentContainerFactory.call()

        then:
        container.textInput == 'Initial text'
        container.checkedBox == '123'
        container.radioGroup == 'second'
        container.select == '2'
        container.multiselect == ['2', '4']

        where:
        contentContainerFactory << [{ page }, { wholePageModule }]
    }

    @Unroll
    def "content values can be set using assignment"() {
        given:
        to ContentAccessShortcutsPage
        def container = contentContainerFactory.call()

        when:
        container.textInput = 'New text'
        then:
        container.textInput.value() == 'New text'

        when:
        container.checkedBox = false
        then:
        !container.checkedBox.value()

        when:
        container.uncheckedBox = true
        then:
        container.uncheckedBox.value() == '123'

        when:
        container.radioGroup = 'third'
        then:
        container.radioGroup*.value().findAll { it } == ['third']

        when:
        container.select = '1'
        then:
        container.select.value() == '1'

        when:
        container.multiselect = ['1', '3']
        then:
        container.multiselect.value() == ['1', '3']

        where:
        contentContainerFactory << [{ page }, { wholePageModule }]
    }
}

class ContentAccessShortcutsPage extends Page {
    final static Closure CONTENT_CLOSURE = {
        textInput { $('#text') }
        checkedBox { $('#checked-box') }
        uncheckedBox { $('#unchecked-box') }
        radioGroup { $('input', name: 'radio') }
        select { $('#select') }
        multiselect { $('#multiselect') }
    }

    static content = {
        Closure baseContent = CONTENT_CLOSURE.clone()
        baseContent.delegate = delegate
        baseContent.call()
        wholePageModule { module WholePageModule }
    }
}

class WholePageModule extends Module {
    static content = ContentAccessShortcutsPage.CONTENT_CLOSURE.clone()
}
