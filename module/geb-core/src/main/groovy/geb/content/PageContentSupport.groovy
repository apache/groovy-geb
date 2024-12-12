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

import geb.error.UndefinedPageContentException
import geb.navigator.Navigator

abstract class PageContentSupport {

    abstract getContent(String name, Object[] args)

    abstract Navigator getNavigator()

    abstract PageContentContainer getOwner()

    abstract Set<String> getContentNames()

    def methodMissing(String name, args) {
        try {
            getContent(name, *args)
        } catch (UndefinedPageContentException e1) {
            getNavigator().methodMissing(name, args)
        }
    }

    def propertyMissing(String name) {
        try {
            getContent(name)
        } catch (UndefinedPageContentException e1) {
            try {
                getNavigator().propertyMissing(name)
            } catch (MissingPropertyException e2) {
                throw new MissingPropertyException("Unable to resolve $name as content for ${owner}, or as a property on its Navigator context. Is $name a class you forgot to import?", name,
                    owner.getClass())
            }
        }
    }

    def propertyMissing(String name, val) {
        try {
            getContent(name).value(val)
        } catch (UndefinedPageContentException e) {
            try {
                getNavigator().propertyMissing(name, val)
            } catch (MissingPropertyException e1) {
                throw new MissingPropertyException(name, owner.getClass())
            }
        }
    }
}