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

import geb.buildadapter.SystemPropertiesBuildAdapter
import geb.driver.*
import geb.error.InvalidGebConfiguration
import geb.navigator.event.NavigatorEventListener
import geb.navigator.factory.*
import geb.report.*
import geb.waiting.Wait
import org.openqa.selenium.WebDriver

/**
 * Represents a particular configuration of Geb.
 */
class Configuration {

    private final static PageEventListener NOOP_PAGE_EVENT_LISTENER = new PageEventListenerSupport()

    final ClassLoader classLoader
    final ConfigObject rawConfig
    final Properties properties
    final BuildAdapter buildAdapter

    private WebDriver driver

    Configuration(Map rawConfig) {
        this(toConfigObject(rawConfig), null, null, null)
    }

    Configuration(ConfigObject rawConfig = null, Properties properties = null, BuildAdapter buildAdapter = null, ClassLoader classLoader = null) {
        this.classLoader = classLoader ?: new GroovyClassLoader()
        this.properties = properties == null ? System.properties : properties
        this.buildAdapter = buildAdapter ?: new SystemPropertiesBuildAdapter()
        this.rawConfig = rawConfig ?: new ConfigObject()
    }

    /**
     * Updates a {@code waiting.preset} config entry for a given preset name.
     */
    void setWaitPreset(String name, Number presetTimeout, Number presetRetryInterval) {
        rawConfig.waiting.presets[name].with {
            timeout = presetTimeout
            retryInterval = presetRetryInterval
        }
    }

    Wait getWaitPreset(String name) {
        def preset = rawConfig.waiting.presets[name]
        def timeout = readValue(preset, 'timeout', getDefaultWaitTimeout())
        def retryInterval = readValue(preset, 'retryInterval', getDefaultWaitRetryInterval())

        new Wait(timeout, retryInterval, getIncludeCauseInWaitTimeoutExceptionMessage())
    }

    Wait getDefaultWait() {
        new Wait(getDefaultWaitTimeout(), getDefaultWaitRetryInterval(), getIncludeCauseInWaitTimeoutExceptionMessage())
    }

    Wait getWait(Number timeout) {
        new Wait(timeout, getDefaultWaitRetryInterval(), getIncludeCauseInWaitTimeoutExceptionMessage())
    }

    Wait getWaitForParam(waitingParam) {
        if (waitingParam == true) {
            defaultWait
        } else if (waitingParam instanceof CharSequence) {
            getWaitPreset(waitingParam.toString())
        } else if (waitingParam instanceof Number && waitingParam > 0) {
            getWait(waitingParam)
        } else if (waitingParam instanceof Collection) {
            if (waitingParam.size() == 2) {
                def timeout = waitingParam[0]
                def retryInterval = waitingParam[1]

                if (timeout instanceof Number && retryInterval instanceof Number) {
                    new Wait(timeout, retryInterval, getIncludeCauseInWaitTimeoutExceptionMessage())
                } else {
                    throw new IllegalArgumentException("'wait' param has illegal value '$waitingParam' (collection elements must be numbers)")
                }
            } else {
                throw new IllegalArgumentException("'wait' param for content template ${this} has illegal value '$waitingParam' (collection must have 2 elements)")
            }
        } else {
            null
        }
    }

    /**
     * Updates the {@code waiting.timeout} config entry.
     *
     * @see #getDefaultWaitTimeout()
     */
    void setDefaultWaitTimeout(Number defaultWaitTimeout) {
        rawConfig.waiting.timeout = defaultWaitTimeout
    }

    /**
     * The default {@code timeout} value to use for waiting (i.e. if unspecified).
     * <p>
     * Either the value at config path {@code waiting.timeout} or {@link geb.waiting.Wait#DEFAULT_TIMEOUT 5}.
     */
    Number getDefaultWaitTimeout() {
        readValue(rawConfig.waiting, 'timeout', Wait.DEFAULT_TIMEOUT) as Number
    }

    /**
     * Returns Either the value at config path {@code waiting.includeCauseInMessage} or {@code false} if there is none.
     * <p>
     * Determines if the message of {@link geb.waiting.WaitTimeoutException} should contain a string representation of its cause.
     */
    boolean getIncludeCauseInWaitTimeoutExceptionMessage() {
        readValue(rawConfig.waiting, 'includeCauseInMessage', false)
    }

    /**
     * Updates the {@code waiting.includeCauseInMessage} config entry.
     *
     * @see #getIncludeCauseInWaitTimeoutExceptionMessage()
     */
    void setIncludeCauseInWaitTimeoutExceptionMessage(boolean include) {
        rawConfig.waiting.includeCauseInMessage = include
    }

    /**
     * Updates the {@code waiting.retryInterval} config entry.
     *
     * @see #getDefaultWaitRetryInterval()
     */
    void setDefaultWaitRetryInterval(Number defaultWaitRetryInterval) {
        rawConfig.waiting.retryInterval = defaultWaitRetryInterval
    }

    /**
     * The default {@code retryInterval} value to use for waiting (i.e. if unspecified).
     * <p>
     * Either the value at config path {@code waiting.retryInterval} or {@link geb.waiting.Wait#DEFAULT_RETRY_INTERVAL 0.1}.
     */
    Number getDefaultWaitRetryInterval() {
        readValue(rawConfig.waiting, 'retryInterval', Wait.DEFAULT_RETRY_INTERVAL)
    }

    Wait getAtCheckWaiting() {
        getWaitForParam(rawConfig.atCheckWaiting)
    }

    void setAtCheckWaiting(Object waitForParam) {
        rawConfig.atCheckWaiting = waitForParam
    }

    Wait getBaseNavigatorWaiting() {
        getWaitForParam(rawConfig.baseNavigatorWaiting)
    }

    void setBaseNavigatorWaiting(Object waitForParam) {
        rawConfig.baseNavigatorWaiting = waitForParam
    }

    Collection<Class<? extends Page>> getUnexpectedPages() {
        def unexpectedPages = rawConfig.unexpectedPages ?: []
        def isCollectionContainingOnlyPageClasses = unexpectedPages instanceof Collection && unexpectedPages.every { it instanceof Class && Page.isAssignableFrom(it) }
        if (!isCollectionContainingOnlyPageClasses) {
            def message = "Unexpected pages configuration has to be a collection of classes that extend ${Page.name} but found \"$unexpectedPages\". " +
                    "Did you forget to include some imports in your config file?"
            throw new InvalidGebConfiguration(message)
        }
        unexpectedPages
    }

    void setUnexpectedPages(Collection<Class<? extends Page>> pages) {
        rawConfig.unexpectedPages = pages
    }

    /**
     * Should the created driver be cached if there is no existing cached driver, of if there
     * is a cached driver should it be used instead of creating a new one.
     * <p>
     * The value is the config entry {@code cacheDriver}, which defaults to {@code true}.
     */
    boolean isCacheDriver() {
        readValue('cacheDriver', true)
    }

    /**
     * Updates the {@code cacheDriver} config entry.
     *
     * @see #isCacheDriver()
     */
    void setCacheDriver(boolean flag) {
        rawConfig.cacheDriver = flag
    }

    /**
     * If the driver is to be cached, this setting controls whether or not the driver is cached per thread or
     * globally for all threads.
     * <p>
     * The value is the config entry {@code cacheDriverPerThread}, which defaults to {@code false}.
     */
    boolean isCacheDriverPerThread() {
        readValue('cacheDriverPerThread', false)
    }

    /**
     * Updates the {@code cacheDriverPerThread} config entry.
     *
     * @see #isCacheDriverPerThread()
     */
    void setCacheDriverPerThread(boolean flag) {
        rawConfig.cacheDriverPerThread = flag
    }

    /**
     * If a cached driver is being used, should it be automatically quit when the JVM exits.
     * <p>
     * The value is the config entry {@code quitCachedDriverOnShutdown}, which defaults to {@code true}.
     */
    boolean isQuitCachedDriverOnShutdown() {
        readValue('quitCachedDriverOnShutdown', true)
    }

    /**
     * Sets whether or not the cached driver should be quit when the JVM shuts down.
     */
    void setQuitCacheDriverOnShutdown(boolean flag) {
        rawConfig.quitCachedDriverOnShutdown = flag
    }

    void setQuitDriverOnBrowserReset(boolean flag) {
        rawConfig.quitDriverOnBrowserReset = flag
    }

    boolean isQuitDriverOnBrowserReset() {
        readValue('quitDriverOnBrowserReset', !cacheDriver && !cacheDriverPerThread)
    }

    /**
     * Sets the driver configuration value.
     * <p>
     * This may be the class name of a driver implementation, a driver short name or a closure
     * that when invoked with no arguments returns a driver implementation.
     *
     * @see #getDriver()
     */
    void setDriverConf(value) {
        rawConfig.driver = value
    }

    /**
     * Returns the configuration value for the driver.
     * <p>
     * This may be the class name of a driver implementation, a short name, or a closure
     * that when invoked returns an actual driver.
     *
     * @see #getDriver()
     */
    def getDriverConf() {
        def value = properties.getProperty("geb.driver") ?: readValue("driver", null)
        if (value instanceof WebDriver) {
            throw new IllegalStateException(
                    "The 'driver' config value is an instance of WebDriver. " +
                            "You need to wrap the driver instance in a closure."
            )
        }
        value
    }

    /**
     * Returns the config value {@code baseUrl}, or {@link geb.BuildAdapter#getBaseUrl()}.
     */
    String getBaseUrl() {
        readValue("baseUrl", buildAdapter.baseUrl)
    }

    void setBaseUrl(baseUrl) {
        rawConfig.baseUrl = baseUrl == null ? null : baseUrl.toString()
    }

    /**
     * Returns the config value {@code reportsDir}, or {@link geb.BuildAdapter#getReportsDir()}.
     */
    File getReportsDir() {
        def reportsDir = readValue("reportsDir", buildAdapter.reportsDir)
        if (reportsDir == null) {
            null
        } else if (reportsDir instanceof File) {
            reportsDir
        } else {
            new File(reportsDir.toString())
        }
    }

    def setReportOnTestFailureOnly(boolean value) {
        rawConfig.reportOnTestFailureOnly = value
    }

    boolean isReportOnTestFailureOnly() {
        readValue("reportOnTestFailureOnly", true)
    }

    void setReportsDir(File reportsDir) {
        rawConfig.reportsDir = reportsDir
    }

    /**
     * Returns the reporter implementation to use for taking snapshots of the browser's state.
     * <p>
     * Returns the config value {@code reporter}, or reporter that records page source and screen shots if not explicitly set.
     */
    Reporter getReporter() {
        def reporter = readValue("reporter", null)
        if (reporter == null) {
            reporter = createDefaultReporter()
            this.reporter = reporter
        } else if (!(reporter instanceof Reporter)) {
            throw new InvalidGebConfiguration("The specified reporter ($reporter) is not an implementation of ${Reporter.name}")
        }

        def typedReporter = reporter as Reporter

        def reportingListener = getReportingListener()
        if (reportingListener) {
            // Adding is idempotent
            typedReporter.addListener(reportingListener)
        }

        typedReporter
    }

    /**
     * Updates the {@code reporter} config entry.
     *
     * @see #getReporter()
     */
    void setReporter(Reporter reporter) {
        rawConfig.reporter = reporter
    }

    void setReportingListener(ReportingListener reportingListener) {
        rawConfig.reportingListener = reportingListener
    }

    ReportingListener getReportingListener() {
        readValue("reportingListener", null)
    }

    NavigatorEventListener getNavigatorEventListener() {
        readValue("navigatorEventListener", null)
    }

    void setNavigatorEventListener(NavigatorEventListener navigatorEventListener) {
        rawConfig.navigatorEventListener = navigatorEventListener
    }

    PageEventListener getPageEventListener() {
        readValue("pageEventListener", NOOP_PAGE_EVENT_LISTENER)
    }

    void setPageEventListener(PageEventListener pageEventListener) {
        rawConfig.pageEventListener = pageEventListener
    }

    WebDriver getDriver() {
        if (driver == null) {
            driver = createDriver()
        }

        driver
    }

    void setDriver(WebDriver driver) {
        this.driver = driver
    }

    /**
     * Whether or not to automatically clear the browser's cookies.
     * <p>
     * Different integrations inspect this property at different times.
     * <p>
     * @return the config value for {@code autoClearCookies}, defaulting to {@code true} if not set.
     */
    boolean isAutoClearCookies() {
        readValue('autoClearCookies', true)
    }

    /**
     * Sets the auto clear cookies flag explicitly, overwriting any value from the config script.
     */
    void setAutoClearCookies(boolean flag) {
        rawConfig.autoClearCookies = flag
    }

    /**
     * Whether or not to automatically clear the browser's web storage, that is both local and session storage.
     * <p>
     * Different integrations inspect this property at different times.
     * <p>
     * @return the config value for {@code autoClearWebStorage}, defaulting to {@code false} if not set.
     */
    boolean isAutoClearWebStorage() {
        readValue('autoClearWebStorage', false)
    }

    /**
     * Sets the auto clear web storage flag explicitly, overwriting any value from the config script.
     */
    void setAutoClearWebStorage(boolean flag) {
        rawConfig.autoClearWebStorage = flag
    }

    /**
     * Creates the navigator factory to be used.
     *
     * Returns {@link BrowserBackedNavigatorFactory} by default.
     * <p>
     * Override by setting the 'navigatorFactory' to a closure that takes a single {@link Browser} argument
     * and returns an instance of {@link NavigatorFactory}
     *
     * @param browser The browser to use as the basis of the navigatory factory.
     */
    NavigatorFactory createNavigatorFactory(Browser browser) {
        def navigatorFactory = readValue("navigatorFactory", null)
        if (navigatorFactory == null) {
            new BrowserBackedNavigatorFactory(browser, getInnerNavigatorFactory())
        } else {
            if (navigatorFactory instanceof Closure) {
                def result = navigatorFactory.call(browser)
                if (result instanceof NavigatorFactory) {
                    return result
                }
                throw new InvalidGebConfiguration("navigatorFactory returned '${result}', it should be a NavigatorFactory implementation")
            } else {
                throw new InvalidGebConfiguration("navigatorFactory is '${navigatorFactory}', it should be a Closure that returns a NavigatorFactory implementation")
            }
        }
    }

    /**
     * Returns the inner navigatory factory, that turns WebElements into Navigators.
     *
     * Returns {@link DefaultInnerNavigatorFactory} instances by default.
     * <p>
     * To override, set 'innerNavigatorFactory' to:
     * <ul>
     * <li>An instance of {@link InnerNavigatorFactory}
     * <li>A Closure, that has the signature ({@link Browser}, List<{@link org.openqa.selenium.WebElement}>)
     * </ul>
     *
     * @return The inner navigator factory.
     */
    InnerNavigatorFactory getInnerNavigatorFactory() {
        def innerNavigatorFactory = readValue("innerNavigatorFactory", null)
        switch (innerNavigatorFactory) {
            case null:
                return new DefaultInnerNavigatorFactory()
            case InnerNavigatorFactory:
                return innerNavigatorFactory
            case Closure:
                return new ClosureInnerNavigatorFactory(innerNavigatorFactory)
            default:
                throw new InvalidGebConfiguration("innerNavigatorFactory is '${innerNavigatorFactory}', it should be a Closure or InnerNavigatorFactory implementation")
        }
    }

    /**
     * Sets the inner navigator factory.
     *
     * Only effectual before the browser calls {@link #createNavigatorFactory(Browser)} initially.
     */
    void setInnerNavigatorFactory(InnerNavigatorFactory innerNavigatorFactory) {
        this.rawConfig.innerNavigatorFactory = innerNavigatorFactory
    }

    /**
     * Returns the default configuration closure to be applied before the user-
     * supplied config closure when using the download support.
     */
    @SuppressWarnings("ClosureAsLastMethodParameter")
    Closure getDownloadConfig() {
        readValue("defaultDownloadConfig", { HttpURLConnection con -> })
    }

    void setDownloadConfig(Closure config) {
        rawConfig.defaultDownloadConfig = config
    }

    /**
     * Updates the {@code templateOptions.cache} config entry.
     */
    void setTemplateCacheOption(boolean cache) {
        rawConfig.templateOptions.cache = cache
    }

    /**
     * Updates the {@code templateOptions.wait} config entry.
     */
    void setTemplateWaitOption(wait) {
        rawConfig.templateOptions.wait = wait
    }

    /**
     * Updates the {@code templateOptions.toWait} config entry.
     */
    void setTemplateToWaitOption(toWait) {
        rawConfig.templateOptions.toWait = toWait
    }

    /**
     * Updates the {@code templateOptions.waitCondition} config entry.
     */
    void setTemplateWaitConditionOption(Closure<?> waitCondition) {
        rawConfig.templateOptions.waitCondition = waitCondition
    }

    /**
     * Updates the {@code templateOptions.required} config entry.
     */
    void setTemplateRequiredOption(boolean required) {
        rawConfig.templateOptions.required = required
    }

    /**
     * Updates the {@code templateOptions.min} config entry.
     */
    void setTemplateMinOption(int min) {
        rawConfig.templateOptions.min = min
    }

    /**
     * Updates the {@code templateOptions.max} config entry.
     */
    void setTemplateMaxOption(int max) {
        rawConfig.templateOptions.max = max
    }

    /**
     * Returns default values used for some of the content DSL template options.
     */
    TemplateOptionsConfiguration getTemplateOptions() {
        def raw = rawConfig.templateOptions
        def configuration = TemplateOptionsConfiguration.builder()
                .cache(raw.cache as boolean)
                .wait(readValue(raw, 'wait', null))
                .toWait(readValue(raw, 'toWait', null))
                .waitCondition(extractWaitCondition(raw))
                .required(readOptionalBooleanValue(raw, 'required'))
                .min(readOptionalNonNegativeIntegerValue(raw, 'min', 'min template option'))
                .max(readOptionalNonNegativeIntegerValue(raw, 'max', 'max template option'))
                .build()
        validate(configuration)
        configuration
    }

    /**
     * Updates the {@code withWindow.close} config entry.
     */
    void setWithWindowCloseOption(boolean close) {
        rawConfig.withWindow.close = close
    }

    WithWindowConfiguration getWithWindowConfig() {
        WithWindowConfiguration.builder()
                .close(rawConfig.withWindow.close as boolean)
                .build()
    }

    /**
     * Updates the {@code withNewWindow.close} config entry.
     */
    void setWithNewWindowCloseOption(boolean close) {
        rawConfig.withNewWindow.close = close
    }

    /**
     * Updates the {@code withWindow.wait} config entry.
     */
    void setWithNewWindowWaitOption(wait) {
        rawConfig.withNewWindow.wait = wait
    }

    /**
     * Sets the {@code requirePageAtCheckers} flag explicitly, overwriting any value from the config script.
     */
    void setRequirePageAtCheckers(boolean requirePageAtCheckers) {
        rawConfig.requirePageAtCheckers = requirePageAtCheckers
    }

    /**
     * Whether or not to throw an exception when implicit "at checks" are being performed and the checked page does not define an "at check".
     *
     * @return the config value for {@code requirePageAtCheckers}, defaulting to {@code false} if not set.
     */
    boolean getRequirePageAtCheckers() {
        rawConfig.requirePageAtCheckers
    }

    WithNewWindowConfiguration getWithNewWindowConfig() {
        def raw = rawConfig.withNewWindow
        WithNewWindowConfiguration.builder()
                .close(readOptionalBooleanValue(raw, 'close'))
                .wait(raw.wait)
                .build()
    }

    void validate(TemplateOptionsConfiguration configuration) {
        def required = configuration.required
        def min = configuration.min
        def max = configuration.max
        if (required.present) {
            if (min.present) {
                if ((required.get() && min.get() == 0) || (!required.get() && min.get() != 0)) {
                    boundsAndRequiredConflicting()
                }
            }
            if (max.present && required.get() && max.get() == 0) {
                boundsAndRequiredConflicting()
            }
        }
        if (max.present && min.present && max.get() < min.get()) {
            throw new InvalidGebConfiguration("Configuration contains 'max' template option that is lower than the 'min' template option")
        }
    }

    Configuration merge(Configuration other) {
        rawConfig.merge(other.rawConfig)
        this
    }

    protected readValue(String name, defaultValue) {
        readValue(rawConfig, name, defaultValue)
    }

    protected readValue(ConfigObject config, String name, defaultValue) {
        if (config.containsKey(name)) {
            config[name]
        } else {
            defaultValue
        }
    }

    protected Optional<Boolean> readOptionalBooleanValue(ConfigObject config, String name) {
        if (config.containsKey(name)) {
            Optional.of(config[name] as boolean)
        } else {
            Optional.empty()
        }
    }

    protected Optional<Integer> readOptionalNonNegativeIntegerValue(ConfigObject config, String name, String errorName) {
        if (config.containsKey(name)) {
            def value = config[name]
            if (value instanceof Integer && value >= 0) {
                Optional.of(value)
            } else {
                throw new InvalidGebConfiguration("Configuration for $errorName should be a non-negative integer but found \"$value\"")
            }
        } else {
            Optional.empty()
        }
    }

    protected Reporter createDefaultReporter() {
        new CompositeReporter(new PageSourceReporter(), new ScreenshotReporter())
    }

    private static toConfigObject(Map rawConfig) {
        def configObject = new ConfigObject()
        configObject.putAll(rawConfig)
        configObject
    }

    private Closure<?> extractWaitCondition(ConfigObject config) {
        def waitCondition = config.waitCondition
        if (waitCondition) {
            if (waitCondition instanceof Closure) {
                waitCondition
            } else {
                throw new InvalidGebConfiguration("Configuration for waitCondition template option should be a closure but found \"$waitCondition\"")
            }
        }
    }

    private void boundsAndRequiredConflicting() {
        throw new InvalidGebConfiguration("Configuration for bounds and 'required' template options is conflicting")
    }

    protected WebDriver createDriver() {
        wrapDriverFactoryInCachingIfNeeded(getDriverFactory(getDriverConf())).driver
    }

    protected DriverFactory getDriverFactory(driverValue) {
        switch (driverValue) {
            case CharSequence:
                return new NameBasedDriverFactory(classLoader, driverValue.toString())
            case Closure:
                return new CallbackDriverFactory(driverValue)
            case null:
                return new DefaultDriverFactory(classLoader)
            default:
                throw new DriverCreationException("Unable to determine factory for 'driver' config value '$driverValue'")
        }
    }

    protected DriverFactory wrapDriverFactoryInCachingIfNeeded(DriverFactory factory) {
        if (isCacheDriver()) {
            isCacheDriverPerThread() ? CachingDriverFactory.perThread(factory, isQuitCachedDriverOnShutdown()) : CachingDriverFactory.global(factory, isQuitCachedDriverOnShutdown())
        } else {
            factory
        }
    }
}
