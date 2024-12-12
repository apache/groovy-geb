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

import geb.Browser
import geb.Module
import geb.error.InvalidPageContent
import geb.navigator.Navigator
import geb.navigator.factory.NavigatorFactory
import org.openqa.selenium.WebDriver.TargetLocator

class ModuleBaseCalculator {

    static NavigatorFactory calculate(
            Browser browser, Module module, NavigatorFactory navigatorFactory, TargetLocator targetLocator,
            Map params = [:]
    ) {
        def moduleClass = module.getClass()
        def moduleBaseDefinition = moduleClass.base
        if (!moduleBaseDefinition) {
            navigatorFactory
        } else {
            // Clone it because the same closure may be used
            // via through a subclass and have a different base
            def moduleBaseDefinitionClone = moduleBaseDefinition.clone()
            moduleBaseDefinitionClone.delegate = new ModuleBaseDefinitionDelegate(browser, module, navigatorFactory, targetLocator, params)
            moduleBaseDefinitionClone.resolveStrategy = Closure.DELEGATE_FIRST
            def moduleBase = moduleBaseDefinitionClone()

            if (!(moduleBase instanceof Navigator)) {
                throw new InvalidPageContent("The static 'base' parameter of module class $moduleClass did not return a Navigator")
            }

            navigatorFactory.relativeTo(moduleBase)
        }
    }

}