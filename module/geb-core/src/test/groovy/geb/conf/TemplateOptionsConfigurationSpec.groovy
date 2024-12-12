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
package geb.conf

import geb.Page
import geb.error.ContentCountOutOfBoundsException
import geb.error.RequiredPageContentNotPresent
import geb.test.GebSpecWithCallbackServer

class TemplateOptionsConfigurationSpec extends GebSpecWithCallbackServer {

    def "can configure all content to be cached by default"() {
        given:
        html {}
        browser.config.templateCacheOption = true

        when:
        to ValueHoldingPage

        then:
        notExplicitlyCachedValue == 1

        when:
        value = 2

        then:
        notExplicitlyCachedValue == 1
    }

    def "can configure all content to be waited on by default"() {
        given:
        html {
            head {
                script(type: "text/javascript", """
                    setTimeout(function() {
                        var p = document.createElement("p");
                        p.innerHTML = "Dynamic paragraph";
                        p.className = "dynamic";
                        document.body.appendChild(p);
                    }, 75);
                """)
            }
        }

        and:
        browser.config.templateWaitOption = true

        when:
        to PageWithDynamicContent

        then:
        dynamicContent
    }

    def "can configure all content to wait after transition to a page configured using to option"() {
        given:
        html {
            body {
                button(id: "load-content")
                script(type: "text/javascript", """
                    document.getElementById("load-content").addEventListener("click", function() {
                        setTimeout(function() {
                            var p = document.createElement("p");
                            p.setAttribute("id", "async-content");
                            document.body.appendChild(p);
                        }, 75);
                    });
                """)
            }
        }

        and:
        browser.config.templateToWaitOption = true

        when:
        to PageWithToOption
        asyncPageLoadButton.click()

        then:
        at AsyncPage
    }

    def "can configure all content to have a wait condition"() {
        given:
        html {
            head {
                script(type: "text/javascript") {
                    mkp.yieldUnescaped(getClass().getResource("/jquery-1.4.2.min.js").text)
                }
                script(type: "text/javascript", """
                    setTimeout(function() {
                        \$("p").show();
                    }, 100);
                """)
            }
            body {
                p(class: "dynamic", style: "display: none;", "Dynamically shown paragraph")
            }
        }

        and:
        browser.config.templateWaitConditionOption = { it.displayed }

        when:
        to PageWithDynamicContent

        then:
        dynamicContent.displayed
    }

    def "can configure all content not to be required"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = false

        when:
        to PageWithNotFoundContent

        then:
        notFoundContent.empty
    }

    def "explicit required option overrides default required option"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = false

        when:
        to(new PageWithNotFoundContent(options: [required: true]))
        notFoundContent

        then:
        thrown(RequiredPageContentNotPresent)
    }

    def "explicit min option overrides default required option set to true"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = true

        when:
        to(new PageWithNotFoundContent(options: [min: 0]))

        then:
        notFoundContent.empty
    }

    def "explicit min option overrides default required option set to false"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = false

        when:
        to(new PageWithNotFoundContent(options: [min: 1]))
        notFoundContent

        then:
        thrown(RequiredPageContentNotPresent)
    }

    def "explicit max option overrides default required option"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = true

        when:
        to(new PageWithNotFoundContent(options: [max: 0]))

        then:
        notFoundContent.empty
    }

    def "explicit times option overrides default required option set to true"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = true

        when:
        to(new PageWithNotFoundContent(options: [times: 0]))

        then:
        notFoundContent.empty
    }

    def "explicit times option overrides default required option set to false"() {
        given:
        html {}

        and:
        browser.config.templateRequiredOption = false

        when:
        to(new PageWithNotFoundContent(options: [times: 1]))
        notFoundContent

        then:
        thrown(RequiredPageContentNotPresent)
    }

    def "can configure all content to contain a minimum number of elements"() {
        given:
        html {
            p("text")
        }

        and:
        browser.config.templateMinOption = 2

        when:
        to PageWithParagraphs
        paragraphs

        then:
        thrown(ContentCountOutOfBoundsException)
    }

    def "can configure all content not to be required using default min option"() {
        given:
        html {}

        and:
        browser.config.templateMinOption = 0

        when:
        to PageWithNotFoundContent

        then:
        notFoundContent.empty
    }

    def "explicit min option overrides default min option"() {
        given:
        html {
            p("text")
        }

        and:
        browser.config.templateMinOption = 2

        when:
        to(new PageWithParagraphs(options: [min: 1]))

        then:
        paragraphs.size() == 1
    }

    def "explicit times option overrides default min option"() {
        given:
        html {
            p("text")
        }

        and:
        browser.config.templateMinOption = 2

        when:
        to(new PageWithParagraphs(options: [times: 1]))

        then:
        paragraphs.size() == 1
    }

    def "explicit required option overrides default min option"() {
        given:
        html {}

        and:
        browser.config.templateMinOption = 0

        when:
        to(new PageWithNotFoundContent(options: [required: true]))
        notFoundContent

        then:
        thrown(RequiredPageContentNotPresent)
    }

    def "can configure all content to contain a maximum number of elements"() {
        given:
        html {
            p("text")
            p("text")
        }

        and:
        browser.config.templateMaxOption = 1

        when:
        to PageWithParagraphs
        paragraphs

        then:
        thrown(ContentCountOutOfBoundsException)
    }

    def "can configure all content not to be required using default max option"() {
        given:
        html {}

        and:
        browser.config.templateMaxOption = 0

        when:
        to PageWithNotFoundContent

        then:
        notFoundContent.empty
    }

    def "explicit max option overrides default max option"() {
        given:
        html {
            p("text")
            p("text")
        }

        and:
        browser.config.templateMaxOption = 1

        when:
        to(new PageWithParagraphs(options: [max: 2]))

        then:
        paragraphs.size() == 2
    }

    def "explicit times option overrides default max option"() {
        given:
        html {
            p("text")
            p("text")
        }

        and:
        browser.config.templateMaxOption = 1

        when:
        to(new PageWithParagraphs(options: [times: 2]))

        then:
        paragraphs.size() == 2
    }

}

class ValueHoldingPage extends Page {
    static content = {
        notExplicitlyCachedValue { value }
    }
    def value = 1
}

class PageWithDynamicContent extends Page {
    static content = {
        dynamicContent { $("p.dynamic") }
    }
}

class PageWithToOption extends Page {
    static content = {
        asyncPageLoadButton(to: AsyncPage) { $("button#load-content") } //<1>
    }
}

class AsyncPage extends Page {
    static at = { $("#async-content") }
}

class PageWithNotFoundContent extends Page {
    static content = {
        notFoundContent(options) { $('#not-existing-element') }
    }

    def options = [:]
}

class PageWithParagraphs extends Page {
    static content = {
        paragraphs(options) { $('p') }
    }

    def options = [:]
}