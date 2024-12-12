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
import geb.navigator.Navigator

class PageContentTemplateFactoryDelegate {

    private static final String MODULE_METHOD_NAME = "module"
    private static final String MODULE_LIST_METHOD_NAME = "moduleList"

    private PageContentTemplate template

    @Delegate(allNames = true)
    private final NavigableSupport navigableSupport

    PageContentTemplateFactoryDelegate(PageContentTemplate template) {
        this.template = template
        this.navigableSupport = new NavigableSupport(template.navigatorFactory, template.browser.driver.switchTo())
    }

    def methodMissing(String name, args) {
        template.owner."$name"(*args)
    }

    def propertyMissing(String name) {
        template.owner."$name"
    }

    def module(Map params, Class<? extends Module> moduleClass) {
        throw new MissingMethodException(MODULE_METHOD_NAME, getClass(), [params, moduleClass] as Object[])
    }

    def module(Class<? extends Module> moduleClass, container) {
        throw new MissingMethodException(MODULE_METHOD_NAME, getClass(), [moduleClass, container] as Object[])
    }

    def module(Map params, Class<? extends Module> moduleClass, Navigator base) {
        throw new MissingMethodException(MODULE_METHOD_NAME, getClass(), [params, moduleClass, base] as Object[])
    }

    def moduleList(Map params, Class moduleClass, Navigator navigator) {
        throw new MissingMethodException(MODULE_LIST_METHOD_NAME, getClass(), [params, moduleClass, navigator] as Object[])
    }

    def moduleList(Class moduleClass, Navigator navigator) {
        throw new MissingMethodException(MODULE_LIST_METHOD_NAME, getClass(), [moduleClass, navigator] as Object[])
    }

    def moduleList(Map params, Class moduleClass, Navigator navigator, index) {
        throw new MissingMethodException(MODULE_LIST_METHOD_NAME, getClass(), [params, moduleClass, navigator, index] as Object[])
    }

    def moduleList(Class moduleClass, Navigator navigator, index) {
        throw new MissingMethodException(MODULE_LIST_METHOD_NAME, getClass(), [moduleClass, navigator, index] as Object[])
    }

}
