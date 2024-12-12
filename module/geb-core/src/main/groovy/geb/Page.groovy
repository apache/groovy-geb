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

import geb.content.*
import geb.download.DefaultDownloadSupport
import geb.download.DownloadSupport
import geb.download.UninitializedDownloadSupport
import geb.error.GebException
import geb.error.PageInstanceNotInitializedException
import geb.error.UndefinedAtCheckerException
import geb.error.UnexpectedPageException
import geb.frame.DefaultFrameSupport
import geb.frame.FrameSupport
import geb.frame.UninitializedFrameSupport
import geb.interaction.DefaultInteractionsSupport
import geb.interaction.InteractionsSupport
import geb.interaction.UninitializedInteractionSupport
import geb.js.AlertAndConfirmSupport
import geb.js.DefaultAlertAndConfirmSupport
import geb.js.JavascriptInterface
import geb.js.UninitializedAlertAndConfirmSupport
import geb.url.UrlFragment
import geb.textmatching.TextMatchingSupport
import geb.waiting.DefaultWaitingSupport
import geb.waiting.PotentiallyWaitingExecutor
import geb.waiting.UninitializedWaitingSupport
import geb.waiting.Wait
import geb.waiting.WaitingSupport
import org.openqa.selenium.WebDriver

/**
 * The Page type is the basis of the Page Object pattern in Geb.
 * <p>
 * This implementation is a generic model of every page. Subclasses add methods and
 * content definitions that model specific pages.
 * <p>
 * This class (or subclasses) should not be instantiated directly.
 * <p>
 * The following classes are also mixed in to this class:
 * <ul>
 * <li>{@link geb.content.PageContentSupport}
 * <li>{@link geb.download.DownloadSupport}
 * <li>{@link geb.waiting.WaitingSupport}
 * <li>{@link geb.textmatching.TextMatchingSupport}
 * <li>{@link geb.js.AlertAndConfirmSupport}
 * </ul>
 * <p>
 * See the chapter in the Geb manual on pages for more information on writing subclasses.
 */
class Page implements Navigable, PageContentContainer, Initializable, WaitingSupport {

    /**
     * The "at checker" for this page.
     * <p>
     * Subclasses should define a closure here that verifies that the browser is at this page.
     * <p>
     * This implementation does not have an at checker (i.e. this property is {@code null})
     */
    static at = null

    /**
     * Defines the url for this page to be used when navigating directly to this page.
     * <p>
     * Subclasses can specify either an absolute url, or one relative to the browser's base url.
     * <p>
     * This implementation returns an empty string.
     *
     * @see #to(java.util.Map, geb.url.UrlFragment, java.lang.Object)
     */
    static url = ""

    /**
     * The wait time configuration for 'at' checking specific to this page.
     * <p>
     * Subclasses can specify atCheckWaiting value, value specified in page takes priority over the global atCheckWaiting setting.
     * <p>
     * Possible values for the atCheckWaiting option are consistent with the ones for wait option of content definitions.
     * <p>
     * This implementation does not have any value for atCheckWaiting (i.e. this property is {@code null}).
     */
    static atCheckWaiting = null

    /**
     * Defines the url fragment for this page to be used when navigating directly to this page.
     * <p>
     * Subclasses can specify either a {@code String} which will be used as is or a {@code Map} which will be translated into an application/x-www-form-urlencoded {@code String}.
     * The value used will be escaped appropriately so there is no need to escape it yourself.
     * <p>
     * This implementation does not define a page fragment (i.e. this property is {@code null})
     *
     * @see #to(java.util.Map, geb.url.UrlFragment, java.lang.Object)
     */
    static fragment = null

    private Browser browser

    @Delegate
    private PageContentSupport pageContentSupport = new UninitializedPageContentSupport(this)

    @Delegate
    private DownloadSupport downloadSupport = new UninitializedDownloadSupport(this)

    @Delegate
    private WaitingSupport waitingSupport = new UninitializedWaitingSupport(this)

    @Delegate(parameterAnnotations = true)
    private FrameSupport frameSupport = new UninitializedFrameSupport(this)

    @Delegate(parameterAnnotations = true)
    private InteractionsSupport interactionsSupport = new UninitializedInteractionSupport(this)

    @Delegate
    private final TextMatchingSupport textMatchingSupport = new TextMatchingSupport()

    @Delegate
    private AlertAndConfirmSupport alertAndConfirmSupport = new UninitializedAlertAndConfirmSupport(this)

    @Delegate(allNames = true)
    private Navigable navigableSupport = new UninitializedNavigableSupport(this)

    /**
     * Initialises this page instance, connecting it to the browser.
     * <p>
     * <b>This method is called internally, and should not be called by users of Geb.</b>
     */
    Page init(Browser browser) {
        this.browser = browser
        def contentTemplates = PageContentTemplateBuilder.build(browser, this, browser.navigatorFactory, 'content', this.class, Page)
        pageContentSupport = new DefaultPageContentSupport(this, contentTemplates, browser.navigatorFactory)
        navigableSupport = new NavigableSupport(browser.navigatorFactory, browser.driver.switchTo())
        downloadSupport = new DefaultDownloadSupport(browser)
        waitingSupport = new DefaultWaitingSupport(browser.config)
        frameSupport = new DefaultFrameSupport(browser)
        interactionsSupport = new DefaultInteractionsSupport(browser)
        alertAndConfirmSupport = new DefaultAlertAndConfirmSupport({ js }, browser.config)
        this
    }

    /**
     * The browser that the page is connected to.
     */
    Browser getBrowser() {
        browser
    }

    /**
     * The driver of the browser that the page is connected to.
     */
    WebDriver getDriver() {
        getInitializedBrowser().driver
    }

    /**
     * Returns the name of this class.
     *
     * @see Class#getName()
     */
    String toString() {
        this.class.name
    }

    /**
     * Checks if the browser is not at an unexpected page and then executes this page's "at checker".
     *
     * @return whether the at checker succeeded or not.
     * @see #verifyAtSafely(boolean)
     * @throws AssertionError if this page's "at checker" doesn't pass (with implicit assertions enabled)
     * @throws UnexpectedPageException when at an unexpected page
     */
    boolean verifyAt() {
        def verificationResult = getAtVerificationResult(true)
        if (!verificationResult) {
            getInitializedBrowser().checkIfAtAnUnexpectedPage(getClass())
            verificationResult.rethrowAnyErrors()
        }
        verificationResult
    }

    /**
     * Executes this page's "at checker", suppressing any AssertionError that is thrown
     * and returning false.
     *
     * @return whether the at checker succeeded or not.
     * @see #verifyAt()
     */
    boolean verifyAtSafely(boolean honourGlobalAtCheckWaiting = true) {
        getAtVerificationResult(honourGlobalAtCheckWaiting)
    }

    /**
     * Executes this page's "at checker" and captures the result wrapping up any AssertionError that might have been thrown.
     *
     * @return at verification result with any AssertionError that might have been thrown wrapped up
     * @see AtVerificationResult
     */
    AtVerificationResult getAtVerificationResult(boolean honourGlobalAtCheckWaiting = true) {
        Throwable caughtException = null
        boolean atResult = false
        try {
            atResult = verifyThisPageAtOnly(honourGlobalAtCheckWaiting)
        } catch (AssertionError e) {
            caughtException = e
        }
        new AtVerificationResult(atResult, caughtException)
    }

    /**
     * Sends the browser to this page's url.
     *
     * @param params query parameters to be appended to the url
     * @param fragment optional url fragment identifier
     * @param args "things" that can be used to generate an extra path to append to this page's url
     * @see #convertToPath(java.lang.Object)
     * @see #getPageUrl(java.lang.String)
     */
    void to(Map params, UrlFragment fragment = null, Object[] args) {
        def path = convertToPath(*args)
        if (path == null) {
            path = ""
        }
        getInitializedBrowser().go(params, getPageUrl(path), fragment ?: getPageFragment())
        getInitializedBrowser().page(this)
    }

    /**
     * Returns the constant part of the url to this page.
     * <p>
     * This implementation returns the static {@code url} property of the class.
     */
    String getPageUrl() {
        this.class.url
    }

    /**
     * Returns the fragment part of the url to this page.
     * <p>
     * This implementation returns the static {@code fragment} property of the class wrapped in a {@code UrlFragment} instance.
     *
     * @see geb.url.UrlFragment
     */
    UrlFragment getPageFragment() {
        this.class.fragment ? UrlFragment.of(this.class.fragment) : null
    }

    /**
     * Returns the url to this page, with path appended to it.
     *
     * @see #getPageUrl()
     */
    String getPageUrl(String path) {
        def pageUrl = getPageUrl()
        path ? (pageUrl ? pageUrl + path : path) : pageUrl
    }

    /**
     * Converts the arguments to a path to be appended to this page's url.
     * <p>
     * This is called by the {@link #to(java.util.Map, geb.url.UrlFragment, java.lang.Object)} method and can be used for accessing variants of the page.
     * <p>
     * This implementation returns the string value of each argument, separated by "/"
     */
    // tag::convert_to_path[]
    String convertToPath(Object[] args) {
        args ? '/' + args*.toString().join('/') : ""
    }
    // end::convert_to_path[]

    /**
     * Returns the title of the current browser window.
     *
     * @see org.openqa.selenium.WebDriver#getTitle()
     */
    String getTitle() {
        getInitializedBrowser().driver.title
    }

    /**
     * Provides access to the browser object's JavaScript interface.
     *
     * @see geb.Browser#getJs()
     */
    JavascriptInterface getJs() {
        getInitializedBrowser().js
    }

    /**
     * Lifecycle method called when the page is connected to the browser.
     * <p>
     * This implementation does nothing.
     *
     * @param previousPage The page that was active before this one
     */
    @SuppressWarnings(["UnusedMethodParameter", "EmptyMethod"])
    void onLoad(Page previousPage) {
    }

    /**
     * Lifecycle method called when this page is being replaced as the browser's page instance.
     * <p>
     * This implementation does nothing.
     *
     * @param nextPage The page that will be active after this one
     */
    @SuppressWarnings(["UnusedMethodParameter", "EmptyMethod"])
    void onUnload(Page nextPage) {
    }

    /**
     * Uses the {@link geb.Configuration#getDefaultWait() default wait} from the {@code configuration} to
     * wait for {@code block} to return a true value according to the Groovy Truth.
     * The page is reloaded using {@code WebDriver.Navigation#refresh( )} before each evaluation of {@code block}.
     *
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     * @see geb.Configuration#getDefaultWait()
     */
    public <T> T refreshWaitFor(Map params = [:], Closure<T> block) {
        waitingSupport.waitFor(params, withRefresh(block))
    }

    /**
     * Uses the {@link geb.Configuration#getWaitPreset(java.lang.String) wait preset} from the {@code configuration}
     * with the given name to to wait for {@code block} to return a true value according to the Groovy Truth.
     * The page is reloaded using {@code WebDriver.Navigation#refresh( )} before each evaluation of {@code block}.
     *
     * @param waitPreset the name of the wait preset in {@code configuration} to use
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     * @see geb.Configuration#getWaitPreset(java.lang.String)
     */
    public <T> T refreshWaitFor(Map params = [:], String waitPreset, Closure<T> block) {
        waitingSupport.waitFor(params, waitPreset, withRefresh(block))
    }

    /**
     * Invokes {@code block} every {@link geb.Configuration#getDefaultWaitRetryInterval()} seconds, until it returns
     * a true value according to the Groovy Truth, waiting at most {@code timeout} seconds.
     * The page is reloaded using {@code WebDriver.Navigation#refresh( )} before each evaluation of {@code block}.
     *
     * @param timeout the number of seconds to wait for block to return (roughly)
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     */
    public <T> T refreshWaitFor(Map params = [:], Number timeout, Closure<T> block) {
        waitingSupport.waitFor(params, timeout, withRefresh(block))
    }

    /**
     * Invokes {@code block} every {@code interval} seconds, until it returns
     * a true value according to the Groovy Truth, waiting at most {@code timeout} seconds.
     * The page is reloaded using {@code WebDriver.Navigation#refresh( )} before each evaluation of {@code block}.
     *
     * @param interval the number of seconds to wait between invoking {@code block}
     * @param timeout the number of seconds to wait for block to return (roughly)
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     */
    public <T> T refreshWaitFor(Map params = [:], Number timeout, Number interval, Closure<T> block) {
        waitingSupport.waitFor(params, timeout, interval, withRefresh(block))
    }

    GebException uninitializedException() {
        def message = "Instance of page ${getClass()} has not been initialized. Please pass it to Browser.to(), Browser.via(), Browser.page() or Browser.at() before using it."
        new PageInstanceNotInitializedException(message)
    }

    @Override
    PageContentContainer getRootContainer() {
        this
    }

    @Override
    List<String> getContentPath() {
        []
    }

    boolean getShouldVerifyAtImplicitly() {
        atChecker || browser.config.requirePageAtCheckers
    }

    void at(Object... args) {
        throw new MissingMethodException("at", getClass(), args)
    }

    private Browser getInitializedBrowser() {
        if (browser == null) {
            throw uninitializedException()
        }
        browser
    }

    /**
     * Executes this page's "at checker".
     *
     * @return whether the at checker succeeded or not.
     * @throws AssertionError if this page's "at checker" doesn't pass (with implicit assertions enabled)
     */
    private boolean verifyThisPageAtOnly(boolean honourGlobalAtCheckWaiting) {
        Closure verifier = atChecker?.clone()
        if (verifier) {
            verifier.delegate = this
            verifier.resolveStrategy = Closure.DELEGATE_FIRST
            getEffectiveAtCheckWaitingExecutor(honourGlobalAtCheckWaiting).execute(verifier)
        } else {
            throw new UndefinedAtCheckerException(this.class.name)
        }
    }

    private Closure getAtChecker() {
        getClass().at
    }

    private Wait getGlobalAtCheckWaiting(boolean honourGlobalAtCheckWaiting) {
        honourGlobalAtCheckWaiting ? getInitializedBrowser().config.atCheckWaiting : null
    }

    private PotentiallyWaitingExecutor getEffectiveAtCheckWaitingExecutor(boolean honourGlobalAtCheckWaiting) {
        def wait = getClass().atCheckWaiting != null ?
                pageLevelAtCheckWaiting :
                getGlobalAtCheckWaiting(honourGlobalAtCheckWaiting)

        new PotentiallyWaitingExecutor(wait)
    }

    protected Wait getPageLevelAtCheckWaiting() {
        def atCheckWaitingValue = getClass().atCheckWaiting
        getInitializedBrowser().config.getWaitForParam(atCheckWaitingValue)
    }

    private <T> Closure<T> withRefresh(Closure<T> block) {
        { ->
            browser.driver.navigate().refresh()
            block.call()
        }
    }

}
