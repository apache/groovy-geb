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

import javax.servlet.http.HttpServletRequest

class PropertiesInModuleContentSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        responseHtml { HttpServletRequest request ->
            switch (request.requestURI) {
                case '/source':
                    body {
                        a('link', href: 'destination')
                    }
                    break
                case '/destination':
                    head {
                        title 'Destination Page'
                    }
                    break
            }
        }
    }

    def 'module properties can be used in module content block'() {
        given:
        to SourcePropertiesInModuleContentPage

        when:
        moduleWithPropertyInContent.link.click()

        then:
        at DestinationPropertiesInModuleContentPage
    }
}

class ModuleWithPropertyInContent extends Module {
    static content = {
        link(to: destinationPage) { $('a') }
    }

    Class<? extends Page> destinationPage
}

class SourcePropertiesInModuleContentPage extends Page {
    static url = 'source'

    static content = {
        moduleWithPropertyInContent { module new ModuleWithPropertyInContent(destinationPage: DestinationPropertiesInModuleContentPage) }
    }
}

class DestinationPropertiesInModuleContentPage extends Page {
    static at = { title == 'Destination Page' }
}
