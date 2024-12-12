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

/**
 * Implementations of this interface can be registered with {@link geb.Configuration} to listen to certain {@link geb.Page} events.
 *
 * @see geb.PageEventListenerSupport
 * @see geb.CompositePageEventListener
 */
interface PageEventListener {

    /**
     * Called just before at checking is performed for a page
     *
     * @param browser The {@link Browser} instance used to perform the at checking
     * @param page The {@link geb.Page} for which the at checker is about to be verified
     */
    void beforeAtCheck(Browser browser, Page page)

    /**
     * Called just after at checking has succeeded for a page
     *
     * @param browser The {@link Browser} instance used to perform the at checking
     * @param page The {@link geb.Page} for which the at checker has succeeded
     */
    void onAtCheckSuccess(Browser browser, Page page)

    /**
     * Called just after at checking has failed for a page
     *
     * @param browser The {@link Browser} instance used to perform the at checking
     * @param page The {@link geb.Page} for which the at checker has failed
     */
    void onAtCheckFailure(Browser browser, Page page)

    /**
     * Called each time the browser's page instance changes.
     *
     * Note that this is not when the browser navigates to a new page, but when it's page <i>object</i>
     * changes.
     *
     * @param browser The {@link Browser} instance for which the page instance is about to change
     * @param oldPage The {@link geb.Page} instance currently set on the browser
     * @param newPage The {@link geb.Page} instance which is about to be set on the browser
     */
    void pageWillChange(Browser browser, Page oldPage, Page newPage)

    /**
     * Called when an unexpected page is encountered
     *
     * @param browser The {@link Browser} instance used to check for the unexpected page
     * @param unexpectedPage The unexpected {@link geb.Page} for which the at checker has succeeded
     */
    void unexpectedPageEncountered(Browser browser, Page unexpectedPage)
}
