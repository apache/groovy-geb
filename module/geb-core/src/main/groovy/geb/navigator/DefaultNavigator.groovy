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
package geb.navigator

import geb.Browser
import geb.Module
import geb.Page
import geb.content.ModuleBaseCalculator
import geb.error.SingleElementNavigatorOnlyMethodException
import geb.error.UnableToSetElementException
import geb.error.UnexpectedPageException
import geb.js.JQueryAdapter
import geb.navigator.event.BrowserConfigurationDelegatingNavigatorEventListener
import geb.navigator.event.NavigatorEventListener
import geb.navigator.factory.NavigatorFactory
import geb.waiting.PotentiallyWaitingExecutor
import geb.waiting.Wait
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement

import java.util.function.Supplier

import static java.util.Collections.EMPTY_LIST
import static geb.navigator.WebElementPredicates.matches

class DefaultNavigator implements Navigator {

    protected final static BOOLEAN_ATTRIBUTES = ['async', 'autofocus', 'autoplay', 'checked', 'compact', 'complete',
                                                 'controls', 'declare', 'defaultchecked', 'defaultselected', 'defer', 'disabled', 'draggable', 'ended',
                                                 'formnovalidate', 'hidden', 'indeterminate', 'iscontenteditable', 'ismap', 'itemscope', 'loop',
                                                 'multiple', 'muted', 'nohref', 'noresize', 'noshade', 'novalidate', 'nowrap', 'open', 'paused',
                                                 'pubdate', 'readonly', 'required', 'reversed', 'scoped', 'seamless', 'seeking', 'selected',
                                                 'spellcheck', 'truespeed', 'willvalidate']

    protected final static ELEMENTS_WITH_MUTABLE_VALUE = ['input', 'select', 'textarea']

    final Browser browser

    @Delegate(allNames = true)
    final Locator locator

    protected final Iterable<WebElement> contextElements

    protected NavigatorEventListener eventListener

    DefaultNavigator(Browser browser, Iterable<? extends WebElement> contextElements) {
        this.browser = browser
        this.locator = new DefaultLocator(new SearchContextBasedBasicLocator(contextElements, browser.navigatorFactory))
        this.contextElements = contextElements
        this.eventListener = new BrowserConfigurationDelegatingNavigatorEventListener(browser, this)
    }

    boolean asBoolean() {
        !empty
    }

    @Override
    Navigator filter(Map<String, Object> predicates, String selector) {
        navigatorFor(dynamic(predicates)) {
            contextElements.findAll(matchingSelectorAndPredicates(selector, predicates))
        }
    }

    Navigator has(String selector) {
        findAll { Navigator it ->
            !it.find(selector).empty
        }
    }

    Navigator has(Map<String, Object> predicates) {
        navigatorForMatching(dynamic(predicates)) {
            it.find(predicates)
        }
    }

    Navigator has(Map<String, Object> predicates, String selector) {
        navigatorForMatching(dynamic(predicates)) {
            it.find(predicates, selector)
        }
    }

    Navigator has(By bySelector) {
        findAll { Navigator it ->
            !it.find(bySelector).empty
        }
    }

    Navigator has(Map<String, Object> predicates, By bySelector) {
        navigatorForMatching(dynamic(predicates)) {
            it.find(predicates, bySelector)
        }
    }

    Navigator hasNot(String selector) {
        findAll { Navigator it ->
            it.find(selector).empty
        }
    }

    Navigator hasNot(Map<String, Object> predicates) {
        navigatorForMatching(dynamic(predicates)) {
            it.find(predicates).empty
        }
    }

    Navigator hasNot(Map<String, Object> predicates, String selector) {
        navigatorForMatching(dynamic(predicates)) {
            it.find(predicates, selector).empty
        }
    }

    Navigator hasNot(By bySelector) {
        findAll { Navigator it ->
            it.find(bySelector).empty
        }
    }

    Navigator hasNot(Map<String, Object> predicates, By bySelector) {
        navigatorForMatching(dynamic(predicates)) {
            it.find(predicates, bySelector).empty
        }
    }

    Navigator eq(int index) {
        this[index]
    }

    Navigator add(String selector) {
        add(By.cssSelector(selector))
    }

    Navigator add(By bySelector) {
        add browser.driver.findElements(bySelector)
    }

    Navigator add(WebElement[] elements) {
        add Arrays.asList(elements)
    }

    Navigator add(Collection<WebElement> elements) {
        List<WebElement> result = []
        result.addAll allElements()
        result.addAll elements
        browser.navigatorFactory.createFromWebElements(result)
    }

    Navigator plus(Navigator navigator) {
        add navigator.allElements()
    }

    String attr(String name) {
        getAttribute(name)
    }

    WebElement firstElement() {
        getElement(0)
    }

    WebElement lastElement() {
        getElement(-1)
    }

    Iterator<Navigator> iterator() {
        new NavigatorIterator()
    }

    Navigator findAll(Closure predicate) {
        browser.navigatorFactory.createFromNavigators(toList().findAll(predicate))
    }

    JQueryAdapter getJquery() {
        new JQueryAdapter(this)
    }

    public <T extends Module> T module(Class<T> moduleClass) {
        if (!Module.isAssignableFrom(moduleClass)) {
            throw new IllegalArgumentException("$moduleClass is not a subclass of ${Module}")
        }

        module(moduleClass.newInstance())
    }

    public <T extends Module> T module(T module) {
        def baseNavigatorFactory = browser.navigatorFactory.relativeTo(this)

        NavigatorFactory moduleBaseNavigatorFactory = ModuleBaseCalculator.calculate(browser, module,
                baseNavigatorFactory, browser.driver.switchTo())

        module.init(browser, moduleBaseNavigatorFactory)

        module
    }

    public <T extends Module> List<T> moduleList(Class<T> moduleClass) {
        iterator()*.module(moduleClass)
    }

    @SuppressWarnings(["UnnecessaryCollectCall"])
    public <T extends Module> List<T> moduleList(Closure<T> moduleFactory) {
        iterator().collect { it.module(moduleFactory.call()) }
    }

    @Override
    String getStringRepresentation() {
        getClass().name
    }

    @Override
    void setEventListener(NavigatorEventListener listener) {
        this.eventListener = listener
    }

    @Override
    Navigator filter(String selector) {
        navigatorFor contextElements.findAll { element ->
            CssSelector.matches(element, selector)
        }
    }

    @Override
    Navigator filter(Map<String, Object> predicates) {
        def dynamic = dynamic(predicates)

        if (!dynamic || predicates.size() != 1) {
            navigatorFor(dynamic) {
                contextElements.findAll { matches(it, predicates) }
            }
        } else {
            this
        }
    }

    @Override
    Navigator not(String selector) {
        navigatorFor contextElements.findAll { element ->
            !CssSelector.matches(element, selector)
        }
    }

    @Override
    Navigator not(Map<String, Object> predicates, String selector) {
        navigatorFor(dynamic(predicates)) {
            contextElements.findAll { element ->
                !(CssSelector.matches(element, selector) && matches(element, predicates))
            }
        }
    }

    @Override
    Navigator not(Map<String, Object> predicates) {
        navigatorFor(dynamic(predicates)) {
            contextElements.findAll { element ->
                !matches(element, predicates)
            }
        }
    }

    @Override
    Navigator getAt(int index) {
        navigatorFor(Collections.singleton(getElement(index)))
    }

    @Override
    Navigator getAt(Range range) {
        navigatorFor getElements(range)
    }

    @Override
    Navigator getAt(Collection indexes) {
        navigatorFor getElements(indexes)
    }

    @Override
    WebElement singleElement() {
        ensureContainsAtMostSingleElement("singleElement")
    }

    @Override
    Collection<WebElement> allElements() {
        contextElements.toList()
    }

    @Override
    Iterator<WebElement> elementIterator() {
        contextElements.iterator()
    }

    WebElement getElement(int index) {
        def elements = contextElements.toList()
        if (elements) {
            contextElements[index]
        }
    }

    List<WebElement> getElements(Range range) {
        def elements = contextElements.toList()
        if (elements) {
            elements[range]
        }
    }

    List<WebElement> getElements(Collection indexes) {
        contextElements.toList()[indexes]
    }

    @Override
    Navigator remove(int index) {
        int size = size()
        if (!(index in -size..<size)) {
            this
        } else {
            def elements = contextElements.toList()
            navigatorFor(elements - elements[index])
        }
    }

    @Override
    Navigator next() {
        navigatorFor collectElements {
            it.findElement By.xpath("following-sibling::*")
        }
    }

    @Override
    Navigator next(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectFollowingSiblings {
                it.find { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator next(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectFollowingSiblings {
                it.find(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator nextAll() {
        navigatorFor collectFollowingSiblings()
    }

    @Override
    Navigator nextAll(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectFollowingSiblings {
                it.findAll { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator nextAll(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectFollowingSiblings {
                it.findAll(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator nextUntil(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectFollowingSiblings {
                collectUntil(it, attributes)
            }
        }
    }

    @Override
    Navigator nextUntil(Map<String, Object> attributes = [:], String selector) {
        navigatorFor(dynamic(attributes)) {
            collectFollowingSiblings {
                collectUntil(it, attributes, selector)
            }
        }
    }

    @Override
    Navigator previous() {
        navigatorFor collectPreviousSiblings {
            it ? it.last() : EMPTY_LIST
        }
    }

    @Override
    Navigator previous(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectPreviousSiblings {
                it.reverse().find { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator previous(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectPreviousSiblings {
                it.reverse().find(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator prevAll() {
        navigatorFor collectPreviousSiblings()
    }

    @Override
    Navigator prevAll(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectPreviousSiblings {
                it.reverse().findAll { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator prevAll(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectPreviousSiblings {
                it.reverse().findAll(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator prevUntil(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectPreviousSiblings {
                collectUntil(it.reverse(), attributes)
            }
        }
    }

    @Override
    Navigator prevUntil(Map<String, Object> attributes = [:], String selector) {
        navigatorFor(dynamic(attributes)) {
            collectPreviousSiblings {
                collectUntil(it.reverse(), attributes, selector)
            }
        }
    }

    @Override
    Navigator parent() {
        navigatorFor collectParents()
    }

    @Override
    Navigator parent(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectParents {
                it.findAll { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator parent(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectParents {
                it.findAll(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator parents() {
        navigatorFor collectAncestors {
            it.reverse()
        }
    }

    @Override
    Navigator parents(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectAncestors {
                it.reverse().findAll { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator parents(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectAncestors {
                it.reverse().findAll(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator parentsUntil(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectAncestors {
                collectUntil(it.reverse(), attributes)
            }
        }
    }

    @Override
    Navigator parentsUntil(Map<String, Object> attributes = [:], String selector) {
        navigatorFor(dynamic(attributes)) {
            collectAncestors {
                collectUntil(it.reverse(), attributes, selector)
            }
        }
    }

    @Override
    Navigator closest(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectAncestors {
                it.reverse().find { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator closest(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectAncestors {
                it.reverse().find(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator children() {
        navigatorFor collectChildren()
    }

    @Override
    Navigator children(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectChildren {
                it.findAll { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator children(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectChildren {
                it.findAll(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    Navigator siblings() {
        navigatorFor collectSiblings()
    }

    @Override
    Navigator siblings(Map<String, Object> attributes) {
        navigatorFor(dynamic(attributes)) {
            collectSiblings {
                it.findAll { matches(it, attributes) }
            }
        }
    }

    @Override
    Navigator siblings(Map<String, Object> predicates = [:], String selector) {
        navigatorFor(dynamic(predicates)) {
            collectSiblings {
                it.findAll(matchingSelectorAndPredicates(selector, predicates))
            }
        }
    }

    @Override
    boolean hasClass(String valueToContain) {
        valueToContain in elementClasses(ensureContainsAtMostSingleElement("hasClass", String))
    }

    @Override
    boolean is(String tag) {
        tag.equalsIgnoreCase(ensureContainsAtMostSingleElement("is", String)?.tagName)
    }

    @Override
    boolean isDisplayed() {
        ensureContainsAtMostSingleElement("isDisplayed")?.displayed
    }

    @Override
    String tag() {
        ensureContainsAtMostSingleElement("tag")?.tagName?.toLowerCase()
    }

    @Override
    String text() {
        ensureContainsAtMostSingleElement("text")?.text
    }

    @Override
    String getAttribute(String name) {
        def element = ensureContainsAtMostSingleElement("getAttribute", String)

        if (element) {
            def attribute = element.getAttribute(name)
            if (attribute == 'false' && name in BOOLEAN_ATTRIBUTES) {
                attribute = null
            }

            attribute == null ? "" : attribute
        }
    }

    @Override
    List<String> classes() {
        elementClasses(ensureContainsAtMostSingleElement("classes"))
    }

    @Override
    def value() {
        def element = ensureContainsAtMostSingleElement("value")
        if (element) {
            getInputValue(element)
        }
    }

    @Override
    Navigator value(value) {
        eventListener.beforeValueSet(browser, this, value)
        setInputValues(contextElements, value)
        eventListener.afterValueSet(browser, this, value)
        this
    }

    @Override
    Navigator leftShift(value) {
        eventListener.beforeSendKeys(browser, this, value)
        contextElements.each {
            it.sendKeys value
        }
        eventListener.afterSendKeys(browser, this, value)
        this
    }

    @Override
    Navigator click() {
        def element = ensureContainsAtMostSingleElement("click")
        if (element) {
            eventListener.beforeClick(browser, this)
            element.click()
            eventListener.afterClick(browser, this)
        } else {
            throw new UnsupportedOperationException("not supported on empty navigator objects")
        }
        this
    }

    @Override
    Navigator click(Class<? extends Page> pageClass, Wait wait = null) {
        click(browser.createPage(pageClass), wait)
    }

    @Override
    Navigator click(Page pageInstance, Wait wait = null) {
        click()
        browser.page(pageInstance)
        def at = false
        def assertionError = null
        def throwable = null
        try {
            if (pageInstance.shouldVerifyAtImplicitly) {
                at = new PotentiallyWaitingExecutor(wait).execute { browser.verifyAt() }
            } else {
                at = true
            }
        } catch (AssertionError e) {
            assertionError = e
        } catch (Throwable e) {
            throwable = e
            throw e
        } finally {
            if (!at && !throwable) {
                throw new UnexpectedPageException(pageInstance, (Throwable) assertionError)
            }
        }
        this
    }

    @Override
    Navigator click(List potentialPages, Wait wait = null) {
        click()
        new PotentiallyWaitingExecutor(wait).execute { browser.page(*potentialPages) }
        this
    }

    @Override
    int size() {
        contextElements.size()
    }

    @Override
    boolean isEmpty() {
        size() == 0
    }

    @Override
    Navigator head() {
        first()
    }

    @Override
    Navigator first() {
        navigatorFor(Collections.singleton(firstElement()))
    }

    @Override
    Navigator last() {
        navigatorFor(Collections.singleton(lastElement()))
    }

    @Override
    Navigator tail() {
        def elements = contextElements.toList()
        def tail = elements ? elements.tail() : []
        navigatorFor tail
    }

    @Override
    Navigator verifyNotEmpty() {
        if (empty) {
            throw new EmptyNavigatorException()
        }
        this
    }

    @Override
    int getHeight() {
        ensureContainsAtMostSingleElement("getHeight")?.size?.height ?: 0
    }

    @Override
    int getWidth() {
        ensureContainsAtMostSingleElement("getWidth")?.size?.width ?: 0
    }

    @Override
    int getX() {
        ensureContainsAtMostSingleElement("getX")?.location?.x ?: 0
    }

    @Override
    int getY() {
        ensureContainsAtMostSingleElement("getY")?.location?.y ?: 0
    }

    @Override
    Navigator unique() {
        navigatorFor(allElements().unique(false))
    }

    @Override
    String toString() {
        contextElements*.toString()
    }

    @Override
    String css(String propertyName) {
        ensureContainsAtMostSingleElement("css", String)?.getCssValue(propertyName)
    }

    @Override
    boolean isFocused() {
        ensureContainsAtMostSingleElement("isFocused") == browser.driver.switchTo().activeElement()
    }

    def methodMissing(String name, arguments) {
        def elements = allElements()
        if (!arguments) {
            def navigator = navigatorFor elements.collectMany {
                it.findElements By.name(name)
            }
            if (!navigator.empty || !elements) {
                return navigator
            }
        }
        throw new MissingMethodException(name, getClass(), arguments)
    }

    def propertyMissing(String name) {
        switch (name) {
            case ~/@.+/:
                return getAttribute(name.substring(1))
            default:
                def inputs = collectElements {
                    it.findElements(By.name(name))
                }

                if (inputs) {
                    return getInputValues(inputs)
                }
                throw new MissingPropertyException(name, getClass())
        }
    }

    def propertyMissing(String name, value) {
        def inputs = collectElements {
            it.findElements(By.name(name))
        }

        if (inputs) {
            setInputValues(inputs, value)
        } else {
            throw new MissingPropertyException(name, getClass())
        }
    }

    @Override
    int hashCode() {
        allElements().hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof Navigator) {
            allElements() == obj.allElements()
        }
    }

    protected WebElement ensureContainsAtMostSingleElement(String name, Class<?>... parameterTypes) {
        def elements = allElements()
        if (elements.size() > 1) {
            throw new SingleElementNavigatorOnlyMethodException(Navigator.getMethod(name, parameterTypes), elements.size())
        }
        if (elements) {
            elements.first()
        }
    }

    protected Navigator navigatorFor(Collection<WebElement> contextElements) {
        browser.navigatorFactory.createFromWebElements(contextElements)
    }

    protected Navigator navigatorFor(boolean dynamic, Supplier<Collection<WebElement>> contextElementsSupplier) {
        def elements = dynamic ? toDynamicIterable(contextElementsSupplier) : contextElementsSupplier.get()
        browser.navigatorFactory.createFromWebElements(elements)
    }

    protected Navigator navigatorForMatching(boolean dynamic, @ClosureParams(value = SimpleType, options = "geb.navigator.Navigator") Closure<?> partialNavigatorPredicate) {
        navigatorFor(dynamic) {
            contextElements.findAll { element ->
                partialNavigatorPredicate.call(navigatorFor(Collections.singleton(element)))
            }
        }
    }

    protected Iterable<WebElement> toDynamicIterable(Supplier<Collection<WebElement>> contextElementsSupplier) {
        { -> contextElementsSupplier.get().iterator() } as Iterable<WebElement>
    }

    protected getInputValues(Collection<WebElement> inputs) {
        def values = []
        inputs.each { WebElement input ->
            def value = getInputValue(input)
            if (value != null) {
                values << value
            }
        }
        values.size() < 2 ? values[0] : values
    }

    protected getInputValue(WebElement input) {
        def value = null
        def type = input.getAttribute("type")
        if (input.tagName.toLowerCase() == "select") {
            def select = new SelectFactory().createSelectFor(input)
            if (select.multiple) {
                value = select.allSelectedOptions.collect { getValue(it) }
            } else {
                value = getValue(select.firstSelectedOption)
            }
        } else if (type in ["checkbox", "radio"]) {
            if (input.isSelected()) {
                value = getValue(input)
            }
        } else {
            value = getValue(input)
        }
        value
    }

    protected void setInputValues(Iterable<WebElement> inputs, value) {
        def inputsToTagNames = inputs.collectEntries { [it, it.tagName.toLowerCase()] }
        def unsupportedElements = inputsToTagNames.values() - ELEMENTS_WITH_MUTABLE_VALUE

        if (unsupportedElements) {
            throw new UnableToSetElementException(*unsupportedElements)
        }

        inputsToTagNames.inject(false) { boolean valueSet, WebElement input, String tagName ->
            setInputValue(input, tagName, value, valueSet) || valueSet
        }
    }

    protected boolean setInputValue(WebElement input, String tagName, value, boolean suppressStaleElementException) {
        boolean valueSet = false
        try {
            def type = input.getAttribute("type")
            if (tagName == "select") {
                setSelectValue(input, value)
                valueSet = true
            } else if (type == "checkbox") {
                valueSet = setCheckboxValue(input, value)
            } else if (type == "radio") {
                if (getValue(input) == value.toString() || labelFor(input) == value.toString()) {
                    input.click()
                    valueSet = true
                }
            } else if (type == "file") {
                input.sendKeys value as String
                valueSet = true
            } else if (type in ["color", "date", "datetime-local", "time", "range", "month", "week"]) {
                browser.js.exec(input, value as String, 'arguments[0].setAttribute("value", arguments[1]);')
                valueSet = true
            } else {
                input.clear()
                input.sendKeys value as String
                valueSet = true
            }
        } catch (StaleElementReferenceException e) {
            if (!suppressStaleElementException) {
                throw e
            }
        } finally {
            valueSet
        }
    }

    protected getValue(WebElement input) {
        input?.getAttribute("value")
    }

    protected setSelectValue(WebElement element, value) {
        def select = new SelectFactory().createSelectFor(element)

        def multiple = select.multiple
        if (multiple) {
            select.deselectAll()
        }

        if (value == null || (value instanceof Collection && value.empty)) {
            if (multiple) {
                return
            }
            nonexistentSelectOptionSelected(value.toString(), select)
        }

        def valueStrings
        if (multiple) {
            valueStrings = (value instanceof Collection ? new LinkedList(value) : [value])*.toString()
        } else {
            valueStrings = [value.toString()]
        }

        for (valueString in valueStrings) {
            try {
                select.selectByValue(valueString)
            } catch (NoSuchElementException e1) {
                try {
                    select.selectByVisibleText(valueString)
                } catch (NoSuchElementException e2) {
                    nonexistentSelectOptionSelected(valueString, select)
                }
            }
        }
    }

    private void nonexistentSelectOptionSelected(String valueString, select) {
        def availableValues = select.options*.getAttribute("value")
        def availableTexts = select.options*.getText()
        throw new IllegalArgumentException("Couldn't select option with text or value: $valueString, available texts: $availableTexts, available values: $availableValues")
    }

    protected boolean unselect(WebElement input) {
        if (input.isSelected()) {
            input.click()
            true
        }
    }

    protected boolean select(WebElement input) {
        if (!input.isSelected()) {
            input.click()
            true
        }
    }

    protected boolean setCheckboxValue(WebElement input, value) {
        if (value == null || value == false || (value instanceof Collection && value.empty)) {
            unselect(input)
        } else if (value == true) {
            select(input)
        } else {
            def values = value instanceof Collection ? value*.toString() : [value]
            if (getValue(input) in values || labelFor(input) in values) {
                select(input)
            } else {
                unselect(input)
            }
        }
    }

    protected String labelFor(WebElement input) {
        def id = input.getAttribute("id")
        def labels = browser.driver.findElements(By.xpath("//label[@for='$id']")) ?: input.findElements(By.xpath("ancestor::label"))
        labels ? labels[0].text : null
    }

    protected List<WebElement> collectElements(@ClosureParams(value = SimpleType, options = "org.openqa.selenium.WebElement") Closure closure) {
        List<WebElement> list = []
        contextElements.each {
            try {
                def value = closure(it)
                switch (value) {
                    case Collection:
                        list.addAll value
                        break
                    default:
                        if (value) {
                            list << value
                        }
                }
            } catch (NoSuchElementException e) {
            }
        }
        list
    }

    protected Collection<WebElement> collectUntil(Collection<WebElement> elements, Closure matcher) {
        int index = elements.findIndexOf matcher
        index == -1 ? elements : elements[0..<index]
    }

    protected Collection<WebElement> collectUntil(Collection<WebElement> elements, Map<String, Object> attributes) {
        collectUntil(elements) { matches(it, attributes) }
    }

    protected Collection<WebElement> collectUntil(Collection<WebElement> elements, Map<String, Object> attributes, String selector) {
        collectUntil(elements) { CssSelector.matches(it, selector) && matches(it, attributes) }
    }

    protected Collection<WebElement> collectRelativeElements(String xpath, @ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectElements {
            def elements = it.findElements(By.xpath(xpath))
            filter ? filter(elements) : elements
        }
    }

    protected Collection<WebElement> collectFollowingSiblings(@ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectRelativeElements("following-sibling::*", filter)
    }

    protected Collection<WebElement> collectPreviousSiblings(@ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectRelativeElements("preceding-sibling::*", filter)
    }

    protected Collection<WebElement> collectParents(@ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectRelativeElements("parent::*", filter)
    }

    protected Collection<WebElement> collectAncestors(@ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectRelativeElements("ancestor::*", filter)
    }

    protected Collection<WebElement> collectChildren(@ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectRelativeElements("child::*", filter)
    }

    protected Collection<WebElement> collectSiblings(@ClosureParams(value = FromString, options = "java.util.List<org.openqa.selenium.WebElement>") Closure filter) {
        collectElements {
            def elements = it.findElements(By.xpath("preceding-sibling::*")) + it.findElements(By.xpath("following-sibling::*"))
            filter ? filter(elements) : elements
        }
    }

    protected List<String> elementClasses(WebElement element) {
        element?.getAttribute("class")?.tokenize()?.unique()?.sort() ?: EMPTY_LIST
    }

    protected Closure<Boolean> matchingSelectorAndPredicates(String selector, Map<String, Object> predicates) {
        { WebElement element -> CssSelector.matches(element, selector) && matches(element, predicates) }
    }

    /**
     * Iterator for looping over the context elements of a Navigator instance.
     */
    private class NavigatorIterator implements Iterator<Navigator> {

        private int index

        boolean hasNext() {
            index < DefaultNavigator.this.size()
        }

        Navigator next() {
            DefaultNavigator.this[index++]
        }

        void remove() {
            throw new UnsupportedOperationException()
        }
    }

    protected boolean dynamic(Map<String, Object> attributes) {
        attributes[DYNAMIC_ATTRIBUTE_NAME]
    }
}
