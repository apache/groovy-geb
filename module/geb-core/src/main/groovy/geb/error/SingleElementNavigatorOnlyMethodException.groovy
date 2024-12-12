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
package geb.error

import java.lang.reflect.Method

class SingleElementNavigatorOnlyMethodException extends GebException {

    SingleElementNavigatorOnlyMethodException(Method singleElementMethod, int navigatorSize) {
        super(buildMessage(singleElementMethod, navigatorSize))
    }

    private static String buildMessage(Method singleElementMethod, int navigatorSize) {
        "Method ${methodDescription(singleElementMethod)} can only be called on single element navigators but it was called on a navigator with size $navigatorSize. " +
                "Please use the spread operator to call this method on all elements of this navigator or change the selector used to create this navigator to only match a single element."
    }

    private static String methodDescription(Method method) {
        def parameterTypeNames = method.parameterTypes*.name
        "$method.name(${parameterTypeNames.join(', ')})"
    }
}