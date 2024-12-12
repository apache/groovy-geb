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
package navigator

import geb.Module
import geb.Page
import geb.test.GebSpecWithCallbackServer

class FormShortcutsSpec extends GebSpecWithCallbackServer {

    def setup() {
        html """
            <html>
                // tag::html[]
                <form>
                    <input type="text" name="geb" value="testing" />
                </form>
                // end::html[]
            </html>
        """
    }

    def "value shortcuts"() {
        expect:
        // tag::value_shortcuts[]
        assert $("form").geb == "testing"
        // end::value_shortcuts[]

        when:
        // tag::value_shortcuts[]
        $("form").geb = "goodness"
        // end::value_shortcuts[]

        then:
        // tag::value_shortcuts[]
        assert $("form").geb == "goodness"
        // end::value_shortcuts[]
    }

    def "value equivalents"() {
        expect:
        // tag::value_equivalents[]
        assert $("form").find("input", name: "geb").value() == "testing"
        // end::value_equivalents[]

        when:
        // tag::value_equivalents[]
        $("form").find("input", name: "geb").value("goodness")
        // end::value_equivalents[]

        then:
        // tag::value_equivalents[]
        assert $("form").find("input", name: "geb").value() == "goodness"
        // end::value_equivalents[]
    }

    def "element shortcut"() {
        expect:
        $("form").geb()
        // tag::element_shortcut[]
        assert $("form").geb() == $("form").find("input", name: "geb")
        // end::element_shortcut[]
    }

    def "content shortcuts"() {
        when:
        // tag::content_shortcuts[]
        page ShortcutPage
        // end::content_shortcuts[]

        then:
        // tag::content_shortcuts[]
        assert geb == "testing"
        // end::content_shortcuts[]

        when:
        // tag::content_shortcuts[]
        geb = "goodness"
        // end::content_shortcuts[]

        then:
        // tag::content_shortcuts[]
        assert geb == "goodness"
        // end::content_shortcuts[]
    }

    def "module shortcuts"() {
        when:
        // tag::module_shortcuts[]
        page ShortcutPage
        // end::module_shortcuts[]

        then:
        // tag::module_shortcuts[]
        assert shortcutModule.geb == "testing"
        // end::module_shortcuts[]

        when:
        // tag::module_shortcuts[]
        shortcutModule.geb = "goodness"
        // end::module_shortcuts[]

        then:
        // tag::module_shortcuts[]
        assert shortcutModule.geb == "goodness"
        // end::module_shortcuts[]
    }
}

// tag::content_definitions[]
class ShortcutModule extends Module {
    static content = {
        geb { $('form').geb() }
    }
}

class ShortcutPage extends Page {
    static content = {
        geb { $('form').geb() }
        shortcutModule { module ShortcutModule }
    }
}
// end::content_definitions[]
