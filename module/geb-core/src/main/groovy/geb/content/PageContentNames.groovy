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

import geb.Module
import geb.Page

import java.beans.Introspector

class PageContentNames {

    private static final Set<String> NOT_ALLOWED_PAGE_NAMES = getNotAllowedContentNames(Page)
    private static final Set<String> NOT_ALLOWED_MODULE_NAMES = getNotAllowedContentNames(Module)

    static boolean isNotAllowed(PageContentContainer owner, String name) {
        switch (owner) {
            case Page:
                return NOT_ALLOWED_PAGE_NAMES.contains(name)
            case Module:
                return NOT_ALLOWED_MODULE_NAMES.contains(name)
            default:
                throw new IllegalArgumentException("Unexpected content owner type when veryfing content name legality: ${owner.class}")
        }
    }

    private static Set<String> getNotAllowedContentNames(Class<?> aClass) {
        getPropertyNames(aClass) << "content"
    }

    private static Set<String> getPropertyNames(Class<?> aClass) {
        ["get", "is"].collect { propertyNamesFromMethodNames(aClass, it) }.sum() as Set
    }

    private static List<String> propertyNamesFromMethodNames(Class aClass, String propertyAccessorPrefix) {
        def prefixLength = propertyAccessorPrefix.size()
        aClass.methods*.name
            .findAll { it.size() > prefixLength && it.startsWith(propertyAccessorPrefix) }*.substring(prefixLength)
            .collect { Introspector.decapitalize(it) }
    }

    private PageContentNames() {
    }

}
