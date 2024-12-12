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

import geb.js.JavascriptInterface
import geb.test.GebSpecWithCallbackServer
import geb.test.GebTestManager
import geb.waiting.Wait
import groovy.transform.InheritConstructors
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver

class PauseSpec extends GebSpecWithCallbackServer {

    private static final GebTestManager TEST_MANAGER = managerBuilder()
            .withBrowserCreator(configToBrowserSupplier { new ThreadSafeScriptExecutionDriverConfigurationLoader().conf })
            .build()

    @Override
    GebTestManager getTestManager() {
        TEST_MANAGER
    }

    def 'pausing triggered using pause() can be cancelled by setting geb.unpause js variable to true'() {
        given:
        html {}

        and:
        def browserPausingThread = new BrowserPausingThread(browser)
        browserPausingThread.start()

        when:
        js.exec 'geb.unpause = true;'

        then:
        browserPausingThread.awaitCompletion()
        !browserPausingThread.error
    }

    @InheritConstructors
    private static class ThreadSafeScriptExecutionDriverConfiguration extends Configuration {
        @Override
        protected WebDriver createDriver() {
            def driver = super.createDriver()
            driver.javascriptEnabled = true
            ThreadSafeScriptExecutionDriver.of(driver)
        }
    }

    @InheritConstructors
    private static class ThreadSafeScriptExecutionDriverConfigurationLoader extends ConfigurationLoader {
        @Override
        protected createConf(ConfigObject rawConfig, GroovyClassLoader classLoader) {
            new ThreadSafeScriptExecutionDriverConfiguration(rawConfig, properties, createBuildAdapter(classLoader), classLoader)
        }
    }

    /**
     * The surrounding specification is using a WebDriver instance from multiple threads which is not safe.
     * This class synchronises access to the executeScript() method which is the only method used concurrently.
     */
    private static class ThreadSafeScriptExecutionDriver implements WebDriver, JavascriptExecutor {

        private final Object executeScriptMutex = new Object()

        @Delegate
        private WebDriver driver

        @Delegate
        private JavascriptExecutor javascriptExecutor

        private ThreadSafeScriptExecutionDriver(WebDriver driver, JavascriptExecutor javascriptExecutor) {
            this.driver = driver
            this.javascriptExecutor = javascriptExecutor
        }

        static <T extends WebDriver & JavascriptExecutor> ThreadSafeScriptExecutionDriver of(T driver) {
            new ThreadSafeScriptExecutionDriver(driver, driver)
        }

        @Override
        Object executeScript(String script, Object... args) {
            synchronized (executeScriptMutex) {
                javascriptExecutor.executeScript(script, args)
            }
        }
    }

    private static class BrowserPausingThread {

        private final Wait waiting = new Wait()
        private final JavascriptInterface js
        private final Thread thread

        private Throwable error

        BrowserPausingThread(Browser browser) {
            this.js = browser.js

            this.thread = new Thread({ browser.pause() })
            this.thread.uncaughtExceptionHandler = { Thread t, Throwable e ->
                error = e
            }
        }

        void start() {
            thread.start()
            waiting.waitFor {
                js.exec 'return geb.unpause !== undefined;'
            }
        }

        void awaitCompletion() {
            waiting.waitFor {
                !thread.alive
            }
        }
    }

}
