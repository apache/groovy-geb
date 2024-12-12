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
package geb.window

import geb.test.GebSpecWithCallbackServer

abstract class BaseWindowHandlingSpec extends GebSpecWithCallbackServer {
    public final static String MAIN_PAGE_URL = '/main'

    def mainWindow

    def setup() {
        mainWindow = currentWindow
    }

    def cleanup() {
        def newlyOpenedWindows = availableWindows - mainWindow
        newlyOpenedWindows.each {
            switchToWindow(it)
            driver.close()
        }
        switchToWindow(mainWindow)
    }

    def setupSpec() {
        responseHtml { request ->
            def page = (~'/(.*)').matcher(request.requestURI)[0][1]
            head {
                title("Window $page")
            }
            body {
                [1, 2].each {
                    def label = "$page-$it"
                    a(id: label, target: "window-$label", href: "/$label", label)
                }
            }
        }
    }

    protected void openAllWindows() {
        go MAIN_PAGE_URL
        $('a').each { link ->
            withNewWindow({ link.click() }, close: false) {}
        }
        assert availableWindows.size() == 3
    }

    protected boolean isInContextOfMainWindow() {
        title == windowTitle()
    }

    protected String windowTitle(int[] indexes = []) {
        def name = "Window main"
        if (indexes) {
            name += "-" + indexes*.toString().join("-")
        }
        name
    }

    protected String windowName(int[] indexes = []) {
        def name = "window-main"
        if (indexes) {
            name += "-" + indexes*.toString().join("-")
        }
        name
    }

    protected void openWindow(int index) {
        def originalWindowHandle = currentWindow

        $("a", index - 1).click()

        //ensure that we can switch to the new window by name and that the page has loaded, for some drivers (IE, Safari) it's not instant
        waitFor(40) {
            switchToWindow(windowName(index))
            title == windowTitle(index)
            switchToWindow(originalWindowHandle)
        }
    }
}
