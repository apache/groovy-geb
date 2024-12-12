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
package geb.transform.implicitassertions

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapExpression

class ConfigurableByNameImplicitlyAssertedMethodCallMatcher implements ImplicitlyAssertedMethodCallMatcher {

    private final ByNameImplicitlyAssertedMethodCallMatcher byNameMatcher

    ConfigurableByNameImplicitlyAssertedMethodCallMatcher(String methodName) {
        this.byNameMatcher = new ByNameImplicitlyAssertedMethodCallMatcher(methodName)
    }

    @Override
    boolean isImplicitlyAsserted(String methodName, List<Expression> arguments) {
        byNameMatcher.isImplicitlyAsserted(methodName, arguments) && !implicitAssertionsDisabled(arguments)
    }

    private boolean implicitAssertionsDisabled(List<Expression> expressions) {
        if (expressions) {
            if (expressions.first() in MapExpression) {
                def mapExpression = expressions.first() as MapExpression
                mapExpression.mapEntryExpressions.any {
                    if (it.keyExpression in ConstantExpression && it.valueExpression in ConstantExpression) {
                        def key = it.keyExpression as ConstantExpression
                        def value = it.valueExpression as ConstantExpression

                        key.value == "implicitAssertions" && value.value == false
                    }
                }
            }
        }
    }
}
