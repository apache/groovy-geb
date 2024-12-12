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
package geb.test

import geb.Browser
import geb.Configuration
import geb.ConfigurationLoader
import geb.spock.GebReportingSpec
import geb.spock.RetryFixtures
import geb.spock.SpockGebTestManagerBuilder
import geb.transform.DynamicallyDispatchesToBrowser
import groovy.transform.InheritConstructors
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.spockframework.runtime.extension.builtin.RetryConditionContext
import spock.lang.Retry

import java.util.function.Predicate
import java.util.function.Supplier

import static spock.lang.Retry.Mode.SETUP_FEATURE_CLEANUP

@DynamicallyDispatchesToBrowser
//Work around https://github.com/SeleniumHQ/selenium/issues/9528
@Retry(
    mode = SETUP_FEATURE_CLEANUP,
    condition = GebSpec.RetryCondition
)
@RetryFixtures(
    condition = GebSpec.RetryCondition
)
class GebSpec extends GebReportingSpec {

    private static final GebTestManager TEST_MANAGER = managerBuilder()
        .withBrowserCreator(configToBrowserSupplier { new ConfigurationLoader().conf })
        .build()

    @Override
    @Delegate(includes = ["getBrowser", "report"])
    GebTestManager getTestManager() {
        TEST_MANAGER
    }

    protected static Supplier<Browser> configToBrowserSupplier(Supplier<Configuration> configSupplier) {
        { ->
            def browser = new Browser(configSupplier.get())
            if (browser.driver instanceof HtmlUnitDriver) {
                browser.driver.javascriptEnabled = true
            }
            browser
        }
    }

    protected static GebTestManagerBuilder managerBuilder() {
        new SpockGebTestManagerBuilder()
            .withReportingEnabled(true)
    }

    @InheritConstructors
    static class RetryCondition extends Closure<Boolean> {
        boolean doCall() {
            def failure = (delegate as RetryConditionContext).failure

            isCausedByTimeout(failure) || isCausedByEmptyResponse(failure)
        }

        protected boolean isCausedByTimeout(Throwable throwable) {
            anyCauseMatches(throwable) { it instanceof TimeoutException }
        }

        protected boolean isCausedByEmptyResponse(Throwable throwable) {
            anyCauseMatches(throwable) {
                it instanceof WebDriverException &&
                    it.message.startsWith("Expected to read a START_MAP but instead have: END.")
            }
        }

        private boolean anyCauseMatches(Throwable rootThrowable, Predicate<Throwable> predicate) {
            def causeRecurringPredicate = new Predicate<Throwable>() {
                @Override
                boolean test(Throwable throwable) {
                    predicate.test(throwable) || (throwable.cause && test(throwable.cause))
                }
            }

            causeRecurringPredicate.test(rootThrowable)
        }
    }
}