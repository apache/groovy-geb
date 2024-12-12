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
package geb.binding

import geb.Browser
import geb.CompositePageEventListener
import geb.Page
import geb.PageEventListener
import geb.PageEventListenerSupport

class BindingUpdater {

    static public final FORWARDED_BROWSER_METHODS = [
            "getPage", "getDriver", "getAugmentedDriver", "getNavigatorFactory", "getConfig", "getSessionStorage", "getLocalStorage", "getBaseUrl", "getCurrentUrl", "getJs",
            "getAvailableWindows", "getCurrentWindow", "getReportGroupDir",
            "setDriver", "setBaseUrl",
            "go", "to", "via",
            "at", "isAt", "verifyAtImplicitly", "checkIfAtAnUnexpectedPage", "createPage",
            "driverAs",
            "pause",
            "withWindow", "withNewWindow",
            "report", "reportGroup", "cleanReportGroupDir",
            "setNetworkLatency", "resetNetworkLatency",
            "clearCookies", "clearCookiesQuietly", "clearWebStorage", "clearWebStorageQuietly",
            "close", "quit"
    ].asImmutable()

    static public final FORWARDED_PAGE_METHODS = [
            "init",
            /$/, "find",
            "module",
            "contains", "iContains", "notContains", "iNotContains", "endsWith", "iEndsWith", "notEndsWith", "iNotEndsWith", "containsWord", "iContainsWord", "notContainsWord", "iNotContainsWord",
            "startsWith", "iStartsWith", "notStartsWith", "iNotStartsWith", "allOf", "anyOf",
            "withFrame",
            "getOwner", "getPageFragment", "getRootContainer", "getContentNames", "getShouldVerifyAtImplicitly", "getAtVerificationResult", "getTitle", "getContentPath", "getContent", "getPageUrl",
            "getBrowser", "getNavigator",
            "onLoad", "onUnload",
            "verifyAt", "verifyAtSafely",
            "waitFor", "refreshWaitFor",
            "focused", "interact", "convertToPath",
            "withAlert", "withNoAlert", "withConfirm", "withNoConfirm",
            "download", "downloadStream", "downloadText", "downloadBytes", "downloadContent"
    ].asImmutable()

    final Browser browser
    final Binding binding

    private final BindingUpdatingPageEventListener pageEventListener

    private PageEventListener originalPageEventListener

    protected BindingUpdater(Binding binding, Browser browser) {
        this.binding = binding
        this.browser = browser
        this.pageEventListener = new BindingUpdatingPageEventListener()
    }

    /**
     * Populates the binding and starts the updater updating the binding as necessary.
     */
    BindingUpdater initialize() {
        binding.browser = browser
        binding.js = browser.js
        binding.config = browser.config

        FORWARDED_BROWSER_METHODS.each {
            binding.setVariable(it, new InvocationForwarding(it, browser))
        }
        originalPageEventListener = browser.config.pageEventListener
        browser.config.pageEventListener = new CompositePageEventListener(originalPageEventListener, pageEventListener)
        pageEventListener.pageWillChange(browser, null, browser.page)

        this
    }

    /**
     * Removes everything from the binding and stops updating it.
     */
    BindingUpdater remove() {
        browser.config.pageEventListener = originalPageEventListener
        pageEventListener.clearBinding()

        binding.variables.remove('browser')
        binding.variables.remove('js')
        binding.variables.remove('config')

        FORWARDED_BROWSER_METHODS.each {
            binding.variables.remove(it)
        }

        this
    }

    private class BindingUpdatingPageEventListener extends PageEventListenerSupport {
        @Override
        void pageWillChange(Browser browser, Page oldPage, Page newPage) {
            binding.setVariable("page", newPage)
            FORWARDED_PAGE_METHODS.each {
                binding.setVariable(it, new InvocationForwarding(it, newPage))
            }
        }

        void clearBinding() {
            binding.variables.remove("page")
            FORWARDED_PAGE_METHODS.each {
                binding.variables.remove(it)
            }
        }
    }

    private static class InvocationForwarding extends Closure {
        private final String methodName
        private final Object target

        InvocationForwarding(String theMethodName, Object theTarget) {
            super(theTarget)

            methodName = theMethodName
            target = theTarget
        }

        protected doCall(Object[] args) {
            target."$methodName"(*args)
        }
    }

}
