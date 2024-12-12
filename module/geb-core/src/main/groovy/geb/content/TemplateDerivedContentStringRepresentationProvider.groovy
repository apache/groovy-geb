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

class TemplateDerivedContentStringRepresentationProvider implements StringRepresentationProvider {

    private final PageContentTemplate template
    private final StringRepresentationProvider innerProvider
    private final Object[] args

    TemplateDerivedContentStringRepresentationProvider(PageContentTemplate template, Object[] args, StringRepresentationProvider innerProvider) {
        this.template = template
        this.innerProvider = innerProvider
        this.args = args
    }

    String getStringRepresentation() {
        def name = args ? "${template.name}(${args.join(', ')})" : template.name
        "${template.owner} -> $name: ${innerProvider.stringRepresentation}"
    }

}
