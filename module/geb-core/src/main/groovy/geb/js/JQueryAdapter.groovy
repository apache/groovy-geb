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
package geb.js

import geb.navigator.DefaultNavigator
import groovy.transform.CompileStatic
import org.openqa.selenium.WebElement
import geb.navigator.Navigator

@CompileStatic
class JQueryAdapter {

    private final Navigator navigator

    JQueryAdapter(Navigator navigator) {
        this.navigator = navigator
    }

    def methodMissing(String name, args) {
        def result = callJQueryMethod(name, args)
        if (result instanceof WebElement) {
            ((DefaultNavigator) navigator).browser.navigatorFactory.createFromWebElements(Collections.singletonList((WebElement) result))
        } else if (result instanceof List) {
            ((DefaultNavigator) navigator).browser.navigatorFactory.createFromWebElements((List<WebElement>) result)
        } else {
            result
        }
    }

    private callJQueryMethod(String name, args) {
        def browser = ((DefaultNavigator) navigator).browser
        def elements = navigator.allElements()

        if (elements) {
            browser.js.exec([elements, "EOE", args, """
                var elements = new Array();
                var callArgs = new Array();
                var collectingElements = true;

                for (j = 0; j < arguments.length; ++j) {
                    var arg = arguments[j];

                    if (collectingElements == true && arg == "EOE") {
                        collectingElements = false;
                    } else if (collectingElements) {
                        elements.push(arg);
                    } else {
                        callArgs.push(arg);
                    }
                }

                var o = jQuery(elements);
                var r = o.${name}.apply(o, callArgs);
                return (r instanceof jQuery) ? r.toArray() : r;
            """].flatten().toArray(new Object[0]))
        } else {
            null
        }
    }

}