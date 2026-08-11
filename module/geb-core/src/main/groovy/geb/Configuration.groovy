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
import geb.navigator.Navigator
import geb.report.*
import geb.waiting.Wait
import groovy.transform.CompileStatic
import org.openqa.selenium.WebDriver

/**
 * Represents a particular configuration of Geb.
 */
@CompileStatic
class Configuration {

    private final static PageEventListener NOOP_PAGE_EVENT_LISTENER = new PageEventListenerSupport()

    final ClassLoader classLoader
    final ConfigObject rawConfig
    final Properties properties
    final BuildAdapter buildAdapter

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
        writeValue("waiting.presets.${name}.timeout", presetTimeout)
        writeValue("waiting.presets.${name}.retryInterval", presetRetryInterval)
    }

    Wait getWaitPreset(String name) {
        new Wait(
            readValue("waiting.presets.${name}.timeout", defaultWaitTimeout),
            readValue("waiting.presets.${name}.retryInterval", defaultWaitRetryInterval),
            includeCauseInWaitTimeoutExceptionMessage
        )
    }

    Wait getDefaultWait() {
        new Wait(defaultWaitTimeout, defaultWaitRetryInterval, includeCauseInWaitTimeoutExceptionMessage)
    }

    Wait getWait(Number timeout) {
        new Wait(timeout, defaultWaitRetryInterval, includeCauseInWaitTimeoutExceptionMessage)
    }

    Wait getWaitForParam(Object waitingParam) {
        if (waitingParam == true) {
            defaultWait
        } else if (waitingParam instanceof CharSequence) {
            getWaitPreset(waitingParam.toString())
        } else if (waitingParam instanceof Number && ((Number) waitingParam) > 0) {
            getWait((Number) waitingParam)
        } else if (waitingParam instanceof Collection) {
            def list = ((Collection<?>) waitingParam).asList()
            if (list.size() == 2) {
                def timeout = list[0]
                def retryInterval = list[1]
                if (timeout instanceof Number && retryInterval instanceof Number) {
                    new Wait((Number) timeout, (Number) retryInterval, includeCauseInWaitTimeoutExceptionMessage)
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
        writeValue('waiting.timeout', defaultWaitTimeout)
    }

    /**
     * The default {@code timeout} value to use for waiting (i.e. if unspecified).
     * <p>
     * Either the value at config path {@code waiting.timeout} or {@link geb.waiting.Wait#DEFAULT_TIMEOUT 5}.
     */
    Number getDefaultWaitTimeout() {
        readValue('waiting.timeout', Wait.DEFAULT_TIMEOUT)
    }

    /**
     * Returns Either the value at config path {@code waiting.includeCauseInMessage} or {@code false} if there is none.
     * <p>
     * Determines if the message of {@link geb.waiting.WaitTimeoutException} should contain a string representation of its cause.
     */
    boolean getIncludeCauseInWaitTimeoutExceptionMessage() {
        readValue('waiting.includeCauseInMessage', false)
    }

    /**
     * Updates the {@code waiting.includeCauseInMessage} config entry.
     *
     * @see #getIncludeCauseInWaitTimeoutExceptionMessage()
     */
    void setIncludeCauseInWaitTimeoutExceptionMessage(boolean include) {
        writeValue('waiting.includeCauseInMessage', include)
    }

    /**
     * Updates the {@code waiting.retryInterval} config entry.
     *
     * @see #getDefaultWaitRetryInterval()
     */
    void setDefaultWaitRetryInterval(Number defaultWaitRetryInterval) {
        writeValue('waiting.retryInterval', defaultWaitRetryInterval)
    }

    Number getDefaultWaitRetryInterval() {
        readValue('waiting.retryInterval', Wait.DEFAULT_RETRY_INTERVAL)
    }

    Wait getAtCheckWaiting() {
        getWaitForParam(lookupConfig('atCheckWaiting'))
    }

    void setAtCheckWaiting(Object waitForParam) {
        writeValue('atCheckWaiting', waitForParam)
    }

    Wait getBaseNavigatorWaiting() {
        getWaitForParam(lookupConfig('baseNavigatorWaiting'))
    }

    void setBaseNavigatorWaiting(Object waitForParam) {
        writeValue('baseNavigatorWaiting', waitForParam)
    }

    Collection<Class<? extends Page>> getUnexpectedPages() {
        def obj = lookupConfig('unexpectedPages')
        if (obj == null) {
            return Collections.<Class<? extends Page>>emptyList()
        }
        if (!(obj instanceof Collection)) {
            throwInvalidUnexpectedPages(obj)
        }
        def items = (Collection<?>) obj
        for (def item : items) {
            if (!(item instanceof Class && Page.isAssignableFrom(item))) {
                throwInvalidUnexpectedPages(obj)
            }
        }
        (Collection<Class<? extends Page>>) items
    }

    void setUnexpectedPages(Collection<Class<? extends Page>> pages) {
        writeValue('unexpectedPages', pages)
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
        writeValue('cacheDriver', flag)
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
        writeValue('cacheDriverPerThread', flag)
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
        writeValue('quitCachedDriverOnShutdown', flag)
    }

    void setQuitDriverOnBrowserReset(boolean flag) {
        writeValue('quitDriverOnBrowserReset', flag)
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
     * @see #createDriver()
     */
    void setDriverConf(Object value) {
        writeValue('driver', value)
    }

    /**
     * Returns the configuration value for the driver.
     * <p>
     * This may be the class name of a driver implementation, a short name, or a closure
     * that when invoked returns an actual driver.
     *
     * @see #createDriver()
     */
    Object getDriverConf() {
        def value = properties.getProperty('geb.driver') ?: readValue('driver', null)
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
        readValue('baseUrl', buildAdapter.baseUrl)
    }

    void setBaseUrl(Object baseUrl) {
        writeValue('baseUrl', baseUrl?.toString())
    }

    /**
     * Returns the config value {@code reportsDir}, or {@link geb.BuildAdapter#getReportsDir()}.
     */
    File getReportsDir() {
        def reportsDir = lookupConfig('reportsDir')
        switch (reportsDir) {
            case null:
                return buildAdapter.reportsDir
            case File:
                return (File) reportsDir
            default:
                return new File(reportsDir.toString())
        }
    }

    void setReportOnTestFailureOnly(boolean value) {
        writeValue('reportOnTestFailureOnly', value)
    }

    boolean isReportOnTestFailureOnly() {
        readValue('reportOnTestFailureOnly', true)
    }

    void setReportsDir(File reportsDir) {
        writeValue('reportsDir', reportsDir)
    }

    /**
     * Returns the reporter implementation to use for taking snapshots of the browser's state.
     * <p>
     * Returns the config value {@code reporter}, or reporter that records page source and screen shots if not explicitly set.
     */
    Reporter getReporter() {
        def resolved = lookupConfig('reporter')
        if (!resolved) {
            resolved = createDefaultReporter()
            this.reporter = resolved
        } else if (!(resolved instanceof Reporter)) {
            throw new InvalidGebConfiguration(
                "The specified reporter ($resolved) is not an implementation of ${Reporter.name}"
            )
        }

        def typedReporter = (Reporter) resolved

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
        writeValue('reporter', reporter)
    }

    void setReportingListener(ReportingListener reportingListener) {
        writeValue('reportingListener', reportingListener)
    }

    ReportingListener getReportingListener() {
        lookupConfig('reportingListener') as ReportingListener
    }

    NavigatorEventListener getNavigatorEventListener() {
        lookupConfig('navigatorEventListener') as NavigatorEventListener
    }

    void setNavigatorEventListener(NavigatorEventListener navigatorEventListener) {
        writeValue('navigatorEventListener', navigatorEventListener)
    }

    PageEventListener getPageEventListener() {
        def pageEventListener = lookupConfig('pageEventListener')
        pageEventListener == null ? NOOP_PAGE_EVENT_LISTENER : (pageEventListener as PageEventListener)
    }

    void setPageEventListener(PageEventListener pageEventListener) {
        writeValue('pageEventListener', pageEventListener)
    }

    WebDriver createDriver() {
        wrapDriverFactoryInCachingIfNeeded(getDriverFactory(driverConf)).driver
    }

    /**
     * @deprecated As of 8.0, replaced by {@link #createDriver()}, the configuration does
     *             no longer carry a driver instance.
     */
    @Deprecated
    WebDriver getDriver() {
        createDriver()
    }

    /**
     * @deprecated As of 8.0, the configuration does no longer carry a driver
     *             instance, but only create new driver instances, the driver
     *             instance used is stored in the browser instance.
     */
    @Deprecated
    void setDriver(WebDriver ignored) {
        // does nothing anymore
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
        writeValue('autoClearCookies', flag)
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
        writeValue('autoClearWebStorage', flag)
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
        def navigatorFactory = lookupConfig('navigatorFactory')
        if (navigatorFactory == null) {
            new BrowserBackedNavigatorFactory(browser, getInnerNavigatorFactory())
        } else if (navigatorFactory instanceof Closure) {
            def result = ((Closure<?>) navigatorFactory).call(browser)
            if (result instanceof NavigatorFactory) {
                return (NavigatorFactory) result
            }
            throw new InvalidGebConfiguration(
                "navigatorFactory returned '$result', " +
                    'it should be a NavigatorFactory implementation'
            )
        } else {
            throw new InvalidGebConfiguration(
                "navigatorFactory is '$navigatorFactory', " +
                    'it should be a Closure that returns a NavigatorFactory implementation'
            )
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
        def innerNavigatorFactory = lookupConfig('innerNavigatorFactory')
        switch (innerNavigatorFactory) {
            case null:
                return new DefaultInnerNavigatorFactory()
            case InnerNavigatorFactory:
                return (InnerNavigatorFactory) innerNavigatorFactory
            case Closure:
                return new ClosureInnerNavigatorFactory((Closure<Navigator>) innerNavigatorFactory)
            default:
                throw new InvalidGebConfiguration(
                    "innerNavigatorFactory is '$innerNavigatorFactory', " +
                        'it should be a Closure or InnerNavigatorFactory implementation'
                )
        }
    }

    /**
     * Sets the inner navigator factory.
     *
     * Only effectual before the browser calls {@link #createNavigatorFactory(Browser)} initially.
     */
    void setInnerNavigatorFactory(InnerNavigatorFactory innerNavigatorFactory) {
        writeValue('innerNavigatorFactory', innerNavigatorFactory)
    }

    /**
     * Returns the default configuration closure to be applied before the user-
     * supplied config closure when using the download support.
     */
    @SuppressWarnings('ClosureAsLastMethodParameter')
    Closure<?> getDownloadConfig() {
        def defaultConfig = { HttpURLConnection con -> }
        def downloadConfig = lookupConfig('defaultDownloadConfig')
        downloadConfig == null ? defaultConfig : (downloadConfig as Closure<?>)
    }

    void setDownloadConfig(Closure config) {
        writeValue('defaultDownloadConfig', config)
    }

    /**
     * Updates the {@code templateOptions.cache} config entry.
     */
    void setTemplateCacheOption(boolean cache) {
        writeValue('templateOptions.cache', cache)
    }

    /**
     * Updates the {@code templateOptions.wait} config entry.
     */
    void setTemplateWaitOption(Object wait) {
        writeValue('templateOptions.wait', wait)
    }

    /**
     * Updates the {@code templateOptions.toWait} config entry.
     */
    void setTemplateToWaitOption(Object toWait) {
        writeValue('templateOptions.toWait', toWait)
    }

    /**
     * Updates the {@code templateOptions.waitCondition} config entry.
     */
    void setTemplateWaitConditionOption(Closure<?> waitCondition) {
        writeValue('templateOptions.waitCondition', waitCondition)
    }

    /**
     * Updates the {@code templateOptions.required} config entry.
     */
    void setTemplateRequiredOption(boolean required) {
        writeValue('templateOptions.required', required)
    }

    /**
     * Updates the {@code templateOptions.min} config entry.
     */
    void setTemplateMinOption(int min) {
        writeValue('templateOptions.min', min)
    }

    /**
     * Updates the {@code templateOptions.max} config entry.
     */
    void setTemplateMaxOption(int max) {
        writeValue('templateOptions.max', max)
    }

    TemplateOptionsConfiguration getTemplateOptions() {
        def cacheValue = lookupConfig('templateOptions.cache')
        def cache = cacheValue == null ? false : (cacheValue as boolean)
        def wait = lookupConfig('templateOptions.wait')
        def toWait = lookupConfig('templateOptions.toWait')
        def waitCondition = extractWaitCondition(lookupConfig('templateOptions.waitCondition'))
        def required = optionalBooleanFrom(lookupConfig('templateOptions.required'))
        def min = optionalNonNegativeIntegerFrom(lookupConfig('templateOptions.min'), 'min template option')
        def max = optionalNonNegativeIntegerFrom(lookupConfig('templateOptions.max'), 'max template option')
        def configuration = TemplateOptionsConfiguration.builder()
                .cache(cache)
                .wait(wait)
                .toWait(toWait)
                .waitCondition(waitCondition)
                .required(required)
                .min(min)
                .max(max)
                .build()
        validate(configuration)
        configuration
    }

    /**
     * Updates the {@code withWindow.close} config entry.
     */
    void setWithWindowCloseOption(boolean close) {
        writeValue('withWindow.close', close)
    }

    WithWindowConfiguration getWithWindowConfig() {
        def close = readValue('withWindow.close', false)
        WithWindowConfiguration.builder()
                .close(close)
                .build()
    }

    /**
     * Updates the {@code withNewWindow.close} config entry.
     */
    void setWithNewWindowCloseOption(boolean close) {
        writeValue('withNewWindow.close', close)
    }

    void setWithNewWindowWaitOption(Object wait) {
        writeValue('withNewWindow.wait', wait)
    }

    /**
     * Sets the {@code requirePageAtCheckers} flag explicitly, overwriting any value from the config script.
     */
    void setRequirePageAtCheckers(boolean requirePageAtCheckers) {
        writeValue('requirePageAtCheckers', requirePageAtCheckers)
    }

    boolean getRequirePageAtCheckers() {
        readValue('requirePageAtCheckers', false)
    }

    WithNewWindowConfiguration getWithNewWindowConfig() {
        def close = optionalBooleanFrom(lookupConfig('withNewWindow.close'))
        def wait = lookupConfig('withNewWindow.wait')
        WithNewWindowConfiguration.builder()
                .close(close)
                .wait(wait)
                .build()
    }

    void validate(TemplateOptionsConfiguration configuration) {
        def required = configuration.required
        def min = configuration.min
        def max = configuration.max
        if (required.present) {
            if (min.present) {
                if ((required.get() && min.get() == 0) || (!required.get() && min.get() != 0)) {
                    throwBoundsAndRequiredConflicting()
                }
            }
            if (max.present && required.get() && max.get() == 0) {
                throwBoundsAndRequiredConflicting()
            }
        }
        if (max.present && min.present && max.get() < min.get()) {
            throw new InvalidGebConfiguration(
                'Configuration contains \'max\' template option that is lower than the \'min\' template option'
            )
        }
    }

    Configuration merge(Configuration other) {
        rawConfig.merge(other.rawConfig)
        this
    }

    protected <T> T readValue(String path, T defaultValue) {
        readValue(rawConfig, path, defaultValue)
    }

    protected <T> T readValue(ConfigObject config, String name, T defaultValue) {
        def value = lookupConfig(config, name)
        value == null ? defaultValue : (T) value
    }

    protected Reporter createDefaultReporter() {
        new CompositeReporter(new PageSourceReporter(), new ScreenshotReporter())
    }

    protected DriverFactory getDriverFactory(Object driverValue) {
        switch (driverValue) {
            case null:
                return new DefaultDriverFactory(classLoader)
            case CharSequence:
                return new NameBasedDriverFactory(classLoader, driverValue.toString())
            case Closure:
                return new CallbackDriverFactory((Closure<?>) driverValue)
            default:
                throw new DriverCreationException(
                    "Unable to determine factory for 'driver' config value '$driverValue'"
                )
        }
    }

    protected DriverFactory wrapDriverFactoryInCachingIfNeeded(DriverFactory factory) {
        if (isCacheDriver()) {
            isCacheDriverPerThread() ?
                CachingDriverFactory.perThread(factory, isQuitCachedDriverOnShutdown()) :
                CachingDriverFactory.global(factory, isQuitCachedDriverOnShutdown())
        } else {
            factory
        }
    }

    private static ConfigObject toConfigObject(Map<?, ?> rawConfig) {
        def config = new ConfigObject()
        config.putAll(rawConfig)
        config
    }

    private void writeValue(String path, Object value) {
        if (path == null) {
            throw new InvalidGebConfiguration('Configuration path cannot be null')
        }
        def separatorIndex = path.lastIndexOf('.')
        if (separatorIndex == -1) {
            rawConfig[path] = value
        } else {
            def parentPath = path.substring(0, separatorIndex)
            def key = path.substring(separatorIndex + 1)
            resolveConfig(parentPath)[key] = value
        }
    }

    private Closure<?> extractWaitCondition(Object waitCondition) {
        if (waitCondition == null) {
            return null
        }
        if (waitCondition instanceof Closure) {
            return (Closure<?>) waitCondition
        }
        throw new InvalidGebConfiguration(
            "Configuration for waitCondition template option should be a closure but found \"$waitCondition\""
        )
    }

    private Optional<Boolean> optionalBooleanFrom(Object value) {
        if (value == null) {
            Optional.empty()
        } else {
            Optional.of(value as Boolean)
        }
    }

    private Optional<Integer> optionalNonNegativeIntegerFrom(Object value, String errorName) {
        if (value == null) {
            Optional.empty()
        } else if (value instanceof Integer && ((Integer) value) >= 0) {
            Optional.of((Integer) value)
        } else {
            throw new InvalidGebConfiguration(
                "Configuration for $errorName should be a non-negative integer but found \"$value\""
            )
        }
    }

    private ConfigObject resolveConfig(String path) {
        resolveConfig(rawConfig, path)
    }

    private ConfigObject resolveConfig(ConfigObject root, String path) {
        if (root == null || path == null) {
            throw new InvalidGebConfiguration('Configuration path cannot be null')
        }
        def current = root
        def parts = path.tokenize('.')
        for (def part : parts) {
            def next = current.get(part)
            switch (next) {
                case ConfigObject:
                    current = (ConfigObject) next
                    break
                case Map:
                    def created = new ConfigObject()
                    created.putAll((Map<?, ?>) next)
                    current[part] = created
                    current = created
                    break
                case null:
                    def created = new ConfigObject()
                    current[part] = created
                    current = created
                    break
                default:
                    throw new InvalidGebConfiguration("Configuration for '$part' should be a map but found \"$next\"")
            }
        }
        current
    }

    private Object lookupConfig(String path) {
        lookupConfig(rawConfig, path)
    }

    private Object lookupConfig(ConfigObject root, String path) {
        if (root == null || path == null) {
            return null
        }
        def current = (Object) root
        def parts = path.tokenize('.')
        for (def part : parts) {
            if (!(current instanceof ConfigObject)) {
                return null
            }
            def currentConfig = (ConfigObject) current
            if (!currentConfig.containsKey(part)) {
                return null
            }
            current = currentConfig.get(part)
        }
        current
    }

    private void throwBoundsAndRequiredConflicting() {
        throw new InvalidGebConfiguration(
            'Configuration for bounds and \'required\' template options is conflicting'
        )
    }

    private void throwInvalidUnexpectedPages(Object value) {
        throw new InvalidGebConfiguration(
            "Unexpected pages configuration has to be a collection of classes that extend ${Page.name} but found \"$value\". " +
                'Did you forget to include some imports in your config file?'
        )
    }

}
