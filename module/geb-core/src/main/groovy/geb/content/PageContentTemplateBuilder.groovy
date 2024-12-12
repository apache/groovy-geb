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
import geb.error.InvalidPageContent
import geb.navigator.factory.NavigatorFactory

class PageContentTemplateBuilder {

    final Browser browser
    final DynamicDelegationSuppressingPageContentContainer container
    final NavigatorFactory navigatorFactory

    final Map<String, PageContentTemplate> templates = [:]

    PageContentTemplateBuilder(Browser browser, PageContentContainer container, NavigatorFactory navigatorFactory) {
        this.browser = browser
        this.container = new DynamicDelegationSuppressingPageContentContainer(container)
        this.navigatorFactory = navigatorFactory
    }

    static Map<String, PageContentTemplate> build(Browser browser, PageContentContainer container, NavigatorFactory navigatorFactory, List<Closure> templatesDefinitions) {
        PageContentTemplateBuilder builder = new PageContentTemplateBuilder(browser, container, navigatorFactory)
        for (templatesDefinition in templatesDefinitions) {
            templatesDefinition.delegate = builder
            templatesDefinition()
        }
        builder.templates
    }

    static Map<String, PageContentTemplate> build(Browser browser, PageContentContainer container, NavigatorFactory navigatorFactory, String property, Class startAt, Class stopAt = Object) {
        if (!stopAt.isAssignableFrom(startAt)) {
            throw new IllegalArgumentException("$startAt is not a subclass of $stopAt")
        }

        def templatesDefinitions = []
        def clazz = startAt

        while (clazz != stopAt) {
            def templatesDefinition
            //noinspection GroovyUnusedCatchParameter
            try {
                templatesDefinition = clazz[property]
            } catch (MissingPropertyException e) {
                // swallow
            }

            if (templatesDefinition) {
                if (!(templatesDefinition instanceof Closure)) {
                    throw new IllegalArgumentException("'$property' static property of class $clazz should be a Closure")
                }
                templatesDefinitions << templatesDefinition.clone()
            }

            clazz = clazz.superclass
        }

        build(browser, container, navigatorFactory, templatesDefinitions.reverse())
    }

    def propertyMissing(String name) {
        container.unwrap()[name]
    }

    def methodMissing(String name, args) {
        def definition = null
        def params = null

        if (PageContentNames.isNotAllowed(container.unwrap(), name)) {
            throwInvalidContent(name, "uses a not allowed content name: '$name'. Please use another name.")
        }

        if (args.size() == 0) {
            throwInvalidContent(name, "contains no definition")
        } else if (args.size() == 1) {
            if ((args[0] instanceof Map)) {
                params = args[0]
            } else {
                definition = args[0]
            }
        } else if (args.size() == 2) {
            params = args[0]
            definition = args[1]
        }

        if (params != null && !(params instanceof Map)) {
            throwBadInvocationError(name, args)
        }
        if (definition != null) {
            if (!(definition instanceof Closure)) {
                throwBadInvocationError(name, args)
            }
        } else {
            if (params?.aliases == null) {
                throwBadInvocationError(name, args)
            }
        }

        def template = create(name, params, definition)
        templates[name] = template
        template
    }

    private void throwInvalidContent(String name, String message) {
        throw new InvalidPageContent(container.unwrap(), name, message)
    }

    private throwBadInvocationError(name, args) {
        throwInvalidContent(name, "is invalid, params must be either a Closure, or Map and Closure (args were: ${args*.class})")
    }

    private PageContentTemplate create(name, params, definition) {
        def aliasedName = params?.aliases
        if (aliasedName) {
            if (!templates[aliasedName]) {
                throwInvalidContent(name, "aliases an unknown element '${params.aliases}'")
            }
            templates[aliasedName]
        } else {
            new PageContentTemplate(browser, container.unwrap(), name, params, definition, navigatorFactory)
        }
    }

}