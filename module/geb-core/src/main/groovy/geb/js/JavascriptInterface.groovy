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

import geb.Browser
import geb.error.GebException
import geb.navigator.Navigator
import groovy.transform.CompileStatic
import org.openqa.selenium.JavascriptExecutor

@CompileStatic
class JavascriptInterface {

    final Browser browser

    JavascriptInterface(Browser browser) {
        this.browser = browser
    }

    def propertyMissing(String name) {
        execjs("return $name;", new Object[0])
    }

    def methodMissing(String name, Object args) {
        Object[] a = (args instanceof Object[]) ? (Object[]) args : new Object[] { args }
        execjs("return ${name}.apply(window, arguments)", a)
    }

    def exec(Object[] args) {
        if (args.size() == 0) {
            throw new IllegalArgumentException("there must be a least one argument")
        }

        def script
        def jsArgs
        if (args.size() == 1) {
            script = args[0]
            jsArgs = []
        } else {
            script = args.last()
            jsArgs = args[0..(args.size() - 2)]
        }

        if (script instanceof Closure) {
            script = script()
        }
        if (!(script instanceof CharSequence)) {
            throw new IllegalArgumentException("The last argument to the js function must be string-like or a Closure returning a string-like")
        }

        execjs(script.toString(), jsArgs.toArray())
    }

    private static Object normalizeJsArg(Object arg) {
        switch (arg) {
            case GString:
                return arg.toString()
            case Navigator:
                def navigator = (Navigator) arg
                return navigator.size() == 1 ? navigator.singleElement() : navigator.allElements()
            default:
                return arg
        }
    }

    private static Object execjs(JavascriptExecutor js, String script, List<Object> args) {
        js.executeScript(script, args.toArray())
    }

    private Object execjs(String script, Object[] args) {
        def driver = browser.driver

        if (!(driver instanceof JavascriptExecutor)) {
            throw new GebException("driver '$driver' can not execute javascript")
        }

        def normalizedArgs = args.collect { normalizeJsArg(it) }
        execjs((JavascriptExecutor) driver, script, normalizedArgs)
    }
}