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

import geb.error.InvalidPageContent
import geb.test.GebSpecWithCallbackServer
import spock.lang.Unroll

class PageContentNameSpec extends GebSpecWithCallbackServer {

    def setup() {
        html {
        }
    }

    @Unroll("using '#contentName' as page content name causes an exception")
    def "using page content names that will result in the content being shadowed causes an exception"() {
        when:
        to(new DynamicContentNamePage(contentName: contentName))

        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template '$contentName' of '${DynamicContentNamePage.name}' uses a not allowed content name: '$contentName'. Please use another name."

        where:
        contentName << ["title", "at", "url", "content", "owner", "rootContainer", "contentPath"]
    }

    @Unroll("can use '#contentName' as page content")
    def "using page content names that are shadowed only for modules does not cause an exception"() {
        when:
        to(new DynamicContentNamePage(contentName: contentName))

        then:
        noExceptionThrown()

        where:
        contentName << ["x", "focused", "base"]
    }

    @Unroll("using '#contentName' as module content name causes an exception")
    def "using module content names that will result in the content being shadowed causes an exception"() {
        when:
        $().module(new DynamicContentNameModule(contentName: contentName))

        then:
        InvalidPageContent e = thrown()
        e.message == "Definition of content template '$contentName' of '${DynamicContentNameModule.name}' uses a not allowed content name: '$contentName'. Please use another name."

        where:
        contentName << ["x", "focused", "at", "base", "content", "owner", "rootContainer", "contentPath"]
    }

    @Unroll("can use '#contentName' as module content")
    def "using module content names that are shadowed only for pages does not cause an exception"() {
        when:
        $().module(new DynamicContentNameModule(contentName: contentName))

        then:
        noExceptionThrown()

        where:
        contentName << ["title", "url"]
    }

}

class DynamicContentNamePage extends Page {
    static content = {
        delegate."$contentName" { $() }
    }

    String contentName
}

class DynamicContentNameModule extends Module {
    static content = {
        delegate."$contentName" { $() }
    }

    String contentName
}
