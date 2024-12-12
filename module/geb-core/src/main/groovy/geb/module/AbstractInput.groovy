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
package geb.module

import geb.error.InvalidModuleBaseException

abstract class AbstractInput extends FormElement {

    abstract String getInputType()

    @Override
    protected void initialized() {
        ensureAtMostOneBaseElement()
        if (!empty) {
            def tag = navigator.tag()
            if (tag.toLowerCase() != "input") {
                throw new InvalidModuleBaseException("Specified base element for ${getClass().name} module was '$tag' but only input is allowed as the base element.")
            }
            def type = navigator.getAttribute("type")?.toLowerCase()
            if (!isTypeValid(type)) {
                throw new InvalidModuleBaseException(
                    "Specified base element for ${getClass().name} module was an input of type '$type' but only input of type ${inputType} is allowed as the base element."
                )
            }
        }
    }

    protected boolean isTypeValid(String type) {
        type == inputType
    }

}
