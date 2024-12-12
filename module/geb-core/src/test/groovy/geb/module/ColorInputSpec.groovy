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
package geb.module

import geb.test.GebSpecWithCallbackServer
import org.openqa.selenium.support.Color

class ColorInputSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
            input(type: 'color')
        }
    }

    ColorInput getInput() {
        $("input").module(ColorInput)
    }

    def 'unset'() {
        expect:
        input.color == new Color(0, 0, 0, 1)
    }

    def 'setting using color'() {
        when:
        input.color = color

        then:
        input.color == color
        input.value() == color.asHex()

        where:
        color = new Color(255, 0, 0, 1)
    }

    def 'setting using hex string'() {
        when:
        input.color = color.asHex()

        then:
        input.color == color
        input.value() == color.asHex()

        where:
        color = new Color(255, 0, 0, 1)
    }

    def 'setting using value'() {
        when:
        input.value(color.asHex())

        then:
        input.value() == color.asHex()
        input.color == color

        where:
        color = new Color(255, 0, 0, 1)
    }

    def 'updating'() {
        when:
        input.color = initialColor

        and:
        input.color = updatedColor

        then:
        input.color == updatedColor

        where:
        initialColor = new Color(255, 0, 0, 1)
        updatedColor = new Color(0, 255, 0, 1)
    }

}
