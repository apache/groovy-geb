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
package geb.content

import geb.Page
import geb.TemplateOptionsConfiguration
import geb.error.InvalidPageContent

class PageContentTemplateParams {

    private static final String MAX = 'max'
    private static final String MIN = 'min'
    private static final String TIMES = 'times'
    private static final String REQUIRED = 'required'

    private final PageContentTemplate owner

    private final String name

    private final TemplateOptionsConfiguration config

    /**
     * The value of the 'required' option, as a boolean according to the Groovy Truth. Defaults to true.
     */
    boolean required

    /**
     * The value of the 'cache' option, as a boolean according to the Groovy Truth. Defaults to false.
     */
    boolean cache

    /**
     * If the to option was a list, this will be the specified list. Defaults to null.
     */
    List toList

    /**
     * If the to option was a single page class or instance, this will be the value used. Defaults to null.
     */
    def toSingle

    /**
     * The value of the 'page' option. Defaults to null.
     */
    Class<? extends Page> page

    /**
     * The value of the 'wait' option. Defaults to null (no wait).
     */
    def wait

    /**
     * The value of the 'toWait' options. Defaults to null (no waiting when switching pages).
     */
    def toWait

    /**
     * Effective lower bound of the number of elements returned by content definition based on evaluation of 'min', 'times' and 'required'
     */
    int min

    /**
     * Effective upper bound of the number of elements returned by content definition based on evaluation of 'max' and 'times'
     */
    int max

    /**
     * The value of the 'waitCondition' option. Defaults to null (no wait).
     */
    Closure<?> waitCondition

    PageContentTemplateParams(PageContentTemplate owner, String name, Map<String, ?> params, TemplateOptionsConfiguration config) {
        this.owner = owner
        this.name = name
        this.config = config

        extractParams(params)
    }

    private void extractParams(Map<String, ?> params) {
        def paramsToProcess = params == null ? Collections.emptyMap() : new HashMap<String, Object>(params)

        cache = toBoolean(paramsToProcess, 'cache', config.cache)

        def toParam = paramsToProcess.remove("to")
        toSingle = extractToSingle(toParam)
        toList = extractToList(toParam)

        if (toParam && toSingle == null && toList == null) {
            throwInvalidContent("contains 'to' content parameter which is not a class or instance that extends Page or a list of classes or instances that extend Page: $toParam")
        }

        extractBounds(paramsToProcess)

        validateRequired(paramsToProcess[REQUIRED])
        required = toBoolean(paramsToProcess, REQUIRED, max != 0 && min != 0)

        page = extractPage(paramsToProcess)

        waitCondition = extractClosure(paramsToProcess, 'waitCondition') ?: config.waitCondition

        def waitParam = toObject(paramsToProcess, "wait", config.wait)
        wait = waitParam != null ? waitParam : waitCondition != null

        toWait = toObject(paramsToProcess, "toWait", config.toWait)

        throwIfAnyParamsLeft(paramsToProcess)
    }

    private void validateRequired(required) {
        def message = '''contains conflicting bounds and 'required' options'''
        if (required == true && (min == 0 || max == 0)) {
            throwInvalidContent(message)
        }
        if (required == false && min > 0) {
            throwInvalidContent(message)
        }
    }

    private void extractBounds(Map paramsToProcess) {
        if (paramsToProcess.containsKey(TIMES)) {
            throwIfOptionSpecifiedWithTimes(paramsToProcess, MAX)
            throwIfOptionSpecifiedWithTimes(paramsToProcess, MIN)
        }
        if (paramsToProcess.containsKey(MIN) && paramsToProcess.containsKey(MAX) && paramsToProcess[MIN] > paramsToProcess[MAX]) {
            throwInvalidContent('''contains 'max' option that is lower than the 'min' option''')
        }
        def times = paramsToProcess.remove(TIMES)
        def notRequired = paramsToProcess[REQUIRED] == false || (!paramsToProcess.containsKey(REQUIRED) && config.required.present && !config.required.get())
        def explicitlyRequired = paramsToProcess[REQUIRED]
        def defaultMin = notRequired ? 0 : (explicitlyRequired ? 1 : config.min.orElse(1))
        def timesMin = times != null ? minTimes(times) : defaultMin
        def timesMax = times != null ? maxTimes(times) : config.max.orElse(Integer.MAX_VALUE)
        max = toNonNegativeInt(paramsToProcess, MAX, timesMax)
        min = toNonNegativeInt(paramsToProcess, MIN, Math.min(max, timesMin))
    }

    private void throwIfOptionSpecifiedWithTimes(Map<String, ?> params, String optionName) {
        if (params.containsKey(optionName)) {
            throwInvalidContent("contains both '$optionName' and 'times' options which cannot be used together")
        }
    }

    private List extractToList(Object toParam) {
        if (toParam instanceof List) {
            toParam
        }
    }

    private extractToSingle(Object toParam) {
        if ((toParam instanceof Class && Page.isAssignableFrom(toParam)) || toParam instanceof Page) {
            toParam
        }
    }

    private void throwIfAnyParamsLeft(Map paramsToProcess) {
        def unrecognizedParams = paramsToProcess.keySet() as TreeSet
        if (unrecognizedParams) {
            throwInvalidContent("uses unknown content parameters: ${unrecognizedParams.join(", ")}")
        }
    }

    private Class<? extends Page> extractPage(Map<String, ?> params) {
        def pageParam = params.remove("page")
        if (pageParam && (!(pageParam instanceof Class) || !Page.isAssignableFrom(pageParam))) {
            throwInvalidContent("contains 'page' content parameter that is not a class that extends Page: $pageParam")
        }
        pageParam as Class<? extends Page>
    }

    private Closure<?> extractClosure(Map paramsToProcess, String optionName) {
        def param = paramsToProcess.remove(optionName)
        if (param) {
            if (param instanceof Closure) {
                param
            } else {
                throwInvalidContent("contains '$optionName' option that is not a closure")
            }
        }
    }

    private boolean toBoolean(Map<String, ?> params, String key, boolean defaultValue) {
        toObject(params, key, defaultValue) as boolean
    }

    private toObject(Map<String, ?> params, String key, Object defaultValue) {
        params.containsKey(key) ? params.remove(key) : defaultValue
    }

    private int toNonNegativeInt(Map<String, ?> params, String key, int defaultValue) {
        def value = params.containsKey(key) ? params.remove(key) : defaultValue
        if (!(value in Integer && value >= 0)) {
            throwInvalidContent("contains '$key' option that is not a non-negative integer")
        }
        value
    }

    private int minTimes(int times) {
        validateTimes(times)
        times
    }

    private int maxTimes(int times) {
        validateTimes(times)
        times
    }

    private int minTimes(IntRange times) {
        validateTimes(times)
        times.from
    }

    private int maxTimes(IntRange times) {
        validateTimes(times)
        times.to
    }

    @SuppressWarnings('UnusedPrivateMethodParameter')
    private int minTimes(times) {
        throwInvalidTimesOption()
    }

    private void validateTimes(int times) {
        if (times < 0) {
            throwInvalidTimesOption()
        }
    }

    private void validateTimes(IntRange times) {
        if (times.reverse) {
            throwInvalidContent('''contains inverted 'times' option''')
        }
        if (times.from < 0) {
            throwInvalidTimesOption()
        }
    }

    private void throwInvalidTimesOption() {
        throwInvalidContent('''contains 'times' option that is not a non-negative integer or a range of non-negative integers''')
    }

    private void throwInvalidContent(String message) {
        throw new InvalidPageContent(owner.owner, name, message)
    }

}
