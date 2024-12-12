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

import geb.Module
import geb.error.InvalidModuleBaseException

/**
 * A module that can be used to access common properties of form elements. Serves as a base for more specialised modules modeling form elements.
 *
 * An attempt to initialize this module with a base other than a {@code button}, {@code input}, {@code option}, {@code select} or {@code textarea} will result in an
 * {@link geb.error.InvalidModuleBaseException}.
 */
class FormElement extends Module {

    private final static SUPPORTED_TAGS = ['button', 'input', 'option', 'select', 'textarea']
    private final static String DISABLED = "disabled"
    private final static String READONLY = "readonly"
    private final static String TRUE = "true"

    /**
     * Allows to check if the first element of base navigator for this module is disabled based on the value of it's {@code disabled} attribute.
     * @return true when the first element of base navigator is disabled
     */
    boolean isDisabled() {
        if (empty) {
            throw new UnsupportedOperationException("This operation is not supported on an empty navigator based ${getClass().name} module")
        }
        def value = navigator.getAttribute(DISABLED)
        // Different drivers return different values here
        (value == DISABLED || value == TRUE)
    }

    /**
     * Shorthand for {@code !disabled}
     * @return true when the first element of base navigator is enabled
     *
     * @see #isDisabled()
     */
    boolean isEnabled() {
        !disabled
    }

    /**
     * Allows to check if the first element of base navigator for this module is read-only based on the value of it's {@code readonly} attribute.
     * @return true when the first element of base navigator is read-only
     */
    boolean isReadOnly() {
        if (empty) {
            throw new UnsupportedOperationException("This operation is not supported on an empty navigator based ${getClass().name} module")
        }
        def value = navigator.getAttribute(READONLY)
        (value == READONLY || value == TRUE)
    }

    /**
     * Shorthand for {@code !readOnly}.
     * @return true when the first element of base navigator is editable
     *
     * @see #isReadOnly()
     */
    boolean isEditable() {
        !readOnly
    }

    protected void initialized() {
        ensureAtMostOneBaseElement()
        if (!navigator.empty) {
            def tag = navigator.tag()
            if (!SUPPORTED_TAGS.contains(tag.toLowerCase())) {
                throw new InvalidModuleBaseException("Specified base element for ${getClass().name} module was '${tag}' but only the following are allowed: ${SUPPORTED_TAGS.join(', ')}")
            }
        }
    }

    protected void ensureAtMostOneBaseElement() {
        def size = navigator.size()
        if (size > 1) {
            throw new InvalidModuleBaseException("Specified base navigator for ${getClass().name} module has $size elements but at most one element is allowed.")
        }
    }

}
