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
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper

@CompileStatic
abstract class PageContentSupport {

    abstract getContent(String name, Object[] args)

    abstract Navigator getNavigator()

    abstract PageContentContainer getOwner()

    abstract Set<String> getContentNames()

    Object methodMissing(String name, Object args) {
        Object[] argsArray = (args instanceof Object[]) ? (Object[]) args : new Object[] { args }
        try {
            getContent(name, argsArray)
        } catch (UndefinedPageContentException | MissingMethodException ignored) {
            InvokerHelper.invokeMethod(navigator, name, argsArray)
        }
    }

    Object propertyMissing(String name) {
        try {
            return getContent(name, new Object[0])
        } catch (UndefinedPageContentException ignore) {
            try {
                InvokerHelper.getProperty(navigator, name)
            } catch (MissingPropertyException ignored) {
                throw new MissingPropertyException("Unable to resolve $name as content for ${owner}, or as a property on its Navigator context. Is $name a class you forgot to import?", name,
                    owner.getClass())
            }
        }
    }

    Object propertyMissing(String name, Object val) {
        Object[] argsArray = (val instanceof Object[]) ? (Object[]) val : new Object[] { val }
        try {
            def content = getContent(name, new Object[0])
            return InvokerHelper.invokeMethod(content, 'value', argsArray)
        } catch (UndefinedPageContentException ignore) {
            try {
                return InvokerHelper.invokeMethod(navigator, 'propertyMissing', new Object[] { name, val })
            } catch (MissingPropertyException ignored) {
                throw new MissingPropertyException(name, owner.getClass())
            }
        }
    }
}