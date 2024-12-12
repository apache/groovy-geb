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

class CompositePageEventListener implements PageEventListener {

    private final List<PageEventListener> pageEventListeners

    CompositePageEventListener(PageEventListener... pageEventListeners) {
        this.pageEventListeners = pageEventListeners.findAll()
    }

    @Override
    void beforeAtCheck(Browser browser, Page page) {
        pageEventListeners*.beforeAtCheck(browser, page)
    }

    @Override
    void onAtCheckSuccess(Browser browser, Page page) {
        pageEventListeners*.onAtCheckSuccess(browser, page)
    }

    @Override
    void onAtCheckFailure(Browser browser, Page page) {
        pageEventListeners*.onAtCheckFailure(browser, page)
    }

    @Override
    void pageWillChange(Browser browser, Page oldPage, Page newPage) {
        pageEventListeners*.pageWillChange(browser, oldPage, newPage)
    }

    @Override
    void unexpectedPageEncountered(Browser browser, Page unexpectedPage) {
        pageEventListeners*.unexpectedPageEncountered(browser, unexpectedPage)
    }
}
