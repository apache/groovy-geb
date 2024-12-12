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
package configuration

import spock.lang.Specification

class TemplateOptionsConfigScriptSpec extends Specification implements InlineConfigurationLoader {

    def "configuring default values for template options"() {
        when:
        configScript """
            // tag::config[]
            templateOptions {
                cache = true
                wait = true
                toWait = true
                waitCondition = { it.displayed }
                required = false
                min = 0
                max = 1
            }
            // end::config[]
        """

        then:
        with(config.templateOptions) {
            cache
            wait == true
            toWait == true
            waitCondition != null
            !required.get()
            min.get() == 0
            max.get() == 1
        }
    }

}
