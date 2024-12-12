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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import static org.codehaus.groovy.syntax.Types.ASSIGNMENT_OPERATOR
import static org.codehaus.groovy.syntax.Types.ofType
import static geb.transform.implicitassertions.ImplicitAssertionsTransformationUtil.*

class ImplicitAssertionsTransformationVisitor extends ClassCodeVisitorSupport {

    private static final List<ImplicitlyAssertedMethodCallMatcher> IMPLICITLY_ASSERTED_METHOD_CALL_MATCHERS = [
            new ConfigurableByNameImplicitlyAssertedMethodCallMatcher("waitFor"),
            new ConfigurableByNameImplicitlyAssertedMethodCallMatcher("refreshWaitFor"),
            new ByNameImplicitlyAssertedMethodCallMatcher("at")
    ]
    private static final String WAIT_CONDITION = "waitCondition"

    SourceUnit sourceUnit

    ImplicitAssertionsTransformationVisitor(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    @Override
    void visitField(FieldNode node) {
        if (node.static && node.initialExpression in ClosureExpression) {
            switch (node.name) {
                case 'at':
                    transformEachStatement(node.initialExpression, true)
                    break
                case 'content':
                    visitContentDsl(node.initialExpression)
                    break
            }
        }
    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        if (statement.expression in MethodCallExpression) {
            MethodCallExpression expression = statement.expression
            if (isSpockVerifyMethodConditionCall(expression)) {
                compensateForSpock(expression)
            } else if (expression.arguments in ArgumentListExpression) {
                ArgumentListExpression arguments = expression.arguments
                potentiallyTransform(expression.methodAsString, arguments.expressions)
            }
        }
    }

    void compensateForSpock(MethodCallExpression expression) {
        if (expression.arguments in ArgumentListExpression) {
            ArgumentListExpression arguments = expression.arguments as ArgumentListExpression
            List<Expression> argumentExpressions = arguments.expressions

            if (argumentExpressions.size() == 12) {
                visitVerifyMethodConditionCall(argumentExpressions, 7)
            }
        }
    }

    boolean isSpockVerifyMethodConditionCall(MethodCallExpression expression) {
        if (expression.objectExpression in ClassExpression && expression.method in ConstantExpression) {
            ClassExpression classExpression = expression.objectExpression as ClassExpression
            ConstantExpression method = expression.method as ConstantExpression

            classExpression.type.name == "org.spockframework.runtime.SpockRuntime" && method.value == "verifyMethodCondition"
        }
    }

    boolean potentiallyTransform(String methodName, List<Expression> arguments) {
        def matcherSatisfied = IMPLICITLY_ASSERTED_METHOD_CALL_MATCHERS.any {
            it.isImplicitlyAsserted(methodName, arguments)
        }
        if (matcherSatisfied && lastArgumentIsClosureExpression(arguments)) {
            transformEachStatement(arguments.last() as ClosureExpression, false)
        }
    }

    void visitVerifyMethodConditionCall(List<Expression> argumentExpressions, int methodNameIndex) {
        Expression verifyMethodConditionMethodArg = argumentExpressions.get(methodNameIndex)
        String methodName = getConstantValueOfType(extractRecordedValueExpression(verifyMethodConditionMethodArg), String)

        if (methodName) {
            Expression verifyMethodConditionArgsArgument = argumentExpressions.get(methodNameIndex + 1)
            if (verifyMethodConditionArgsArgument in ArrayExpression) {
                List<Expression> values = (verifyMethodConditionArgsArgument as ArrayExpression).expressions.collect { Expression argumentExpression ->
                    extractRecordedValueExpression(argumentExpression)
                }

                potentiallyTransform(methodName, values)
            }
        }
    }

    Expression extractRecordedValueExpression(Expression valueRecordExpression) {
        if (valueRecordExpression in MethodCallExpression) {
            MethodCallExpression methodCallExpression = valueRecordExpression as MethodCallExpression

            if (methodCallExpression.arguments in ArgumentListExpression) {
                ArgumentListExpression arguments = methodCallExpression.arguments as ArgumentListExpression

                if (arguments.expressions.size() >= 2) {
                    return arguments.expressions.get(1)
                }
            }
        }

        null
    }

    def getConstantValueOfType(Expression expression, Class type) {
        if (expression != null && expression in ConstantExpression) {
            Object value = ((ConstantExpression) expression).value
            type.isInstance(value) ? value : null
        } else {
            null
        }
    }

    boolean isTransformable(ExpressionStatement statement) {
        if (statement.expression in BinaryExpression) {
            BinaryExpression binaryExpression = statement.expression
            if (ofType(binaryExpression.operation.type, ASSIGNMENT_OPERATOR)) {
                reportError(statement, "Expected a condition, but found an assignment. Did you intend to write '==' ?", sourceUnit)
                false
            }
        }
        true
    }

    @Override
    protected SourceUnit getSourceUnit() {
        sourceUnit
    }

    private boolean lastArgumentIsClosureExpression(ArgumentListExpression arguments) {
        lastArgumentIsClosureExpression(arguments.expressions)
    }

    private boolean lastArgumentIsClosureExpression(List<Expression> arguments) {
        arguments && arguments.last() in ClosureExpression
    }

    private boolean requiredOptionSpecifiedAsFalse(ArgumentListExpression arguments) {
        MapExpression paramMap = arguments.expressions.find { it in MapExpression }
        paramMap?.mapEntryExpressions.any {
            if (it.keyExpression in ConstantExpression && it.valueExpression in ConstantExpression) {
                ConstantExpression key = it.keyExpression
                ConstantExpression value = it.valueExpression
                key.value == 'required' && value.value == false
            }
        }
    }

    private Expression option(ArgumentListExpression arguments, String optionName) {
        MapExpression paramMap = arguments.expressions.find { it in MapExpression }
        paramMap?.mapEntryExpressions?.find {
            if (it.keyExpression in ConstantExpression) {
                ConstantExpression key = it.keyExpression
                key.value == optionName
            }
        }?.valueExpression
    }

    private void visitContentDsl(ClosureExpression closureExpression) {
        BlockStatement blockStatement = closureExpression.code
        blockStatement.statements.each { Statement statement ->
            if (statement in ExpressionStatement) {
                ExpressionStatement expressionStatement = statement
                if (expressionStatement.expression in MethodCallExpression) {
                    MethodCallExpression methodCall = expressionStatement.expression
                    if (methodCall.arguments in ArgumentListExpression) {
                        ArgumentListExpression arguments = methodCall.arguments
                        if (lastArgumentIsClosureExpression(arguments)) {
                            handleWaitingContent(arguments)
                            handleWaitConditionContent(arguments)
                        }
                    }
                }
            }
        }
    }

    private void handleWaitConditionContent(ArgumentListExpression arguments) {
        def waitCondition = option(arguments, WAIT_CONDITION)
        if (waitCondition in ClosureExpression) {
            transformEachStatement(waitCondition, false)
        }
    }

    private void handleWaitingContent(ArgumentListExpression arguments) {
        if ((option(arguments, "wait") || option(arguments, WAIT_CONDITION)) && !requiredOptionSpecifiedAsFalse(arguments)) {
            transformEachStatement(arguments.expressions.last(), true)
        }
    }

    private void transformEachStatement(ClosureExpression closureExpression, boolean appendTrueToNonAssertedStatements) {
        BlockStatement blockStatement = closureExpression.code
        ListIterator iterator = blockStatement.statements.listIterator()
        while (iterator.hasNext()) {
            iterator.set(maybeTransform(iterator.next(), appendTrueToNonAssertedStatements))
        }
    }

    private Statement maybeTransform(Statement statement, boolean appendTrueToNonAssertedStatements) {
        Statement result = statement
        Expression expression = getTransformableExpression(statement)
        if (expression) {
            result = transform(expression, statement, appendTrueToNonAssertedStatements)
        }
        result
    }

    private Expression getTransformableExpression(Statement statement) {
        if (statement in ExpressionStatement) {
            ExpressionStatement expressionStatement = statement
            if (!(expressionStatement.expression in DeclarationExpression)
                    && isTransformable(expressionStatement)) {
                return expressionStatement.expression
            }
        }
    }

    private Statement transform(Expression expression, Statement statement, boolean appendTrueToNonAssertedStatements) {
        Statement replacement

        Expression recordedValueExpression = createRuntimeCall("recordValue", expression)
        BooleanExpression booleanExpression = new BooleanExpression(recordedValueExpression)

        Statement retrieveRecordedValueStatement = new ExpressionStatement(createRuntimeCall("retrieveRecordedValue"))

        Statement withAssertion = new AssertStatement(booleanExpression)
        withAssertion.sourcePosition = expression
        withAssertion.statementLabel = (String) expression.getNodeMetaData("statementLabel")

        BlockStatement assertAndRetrieveRecordedValue = new BlockStatement()
        assertAndRetrieveRecordedValue.addStatement(withAssertion)
        assertAndRetrieveRecordedValue.addStatement(retrieveRecordedValueStatement)

        if (expression in MethodCallExpression) {
            MethodCallExpression methodCall = expression

            replacement = wrapInVoidMethodCheck(
                    expression,
                    assertAndRetrieveRecordedValue,
                    methodCall.objectExpression,
                    methodCall.method,
                    methodCall.arguments,
                    appendTrueToNonAssertedStatements
            )
        } else if (expression in StaticMethodCallExpression) {
            StaticMethodCallExpression methodCall = expression

            replacement = wrapInVoidMethodCheck(
                    expression,
                    assertAndRetrieveRecordedValue,
                    new ClassExpression(methodCall.ownerType),
                    new ConstantExpression(methodCall.method),
                    methodCall.arguments,
                    appendTrueToNonAssertedStatements
            )
        } else {
            replacement = assertAndRetrieveRecordedValue
        }

        replacement.sourcePosition = statement
        replacement
    }

    private Statement wrapInVoidMethodCheck(Expression original, BlockStatement assertAndRetrieveRecordedValue, Expression targetExpression, Expression methodExpression,
                                            Expression argumentsExpression, boolean appendTrueToNonAssertedStatements) {
        Statement noAssertion = new BlockStatement()
        noAssertion.addStatement(new ExpressionStatement(original))
        if (appendTrueToNonAssertedStatements) {
            noAssertion.addStatement(new ExpressionStatement(ConstantExpression.TRUE))
        }
        StaticMethodCallExpression isVoidMethod = createRuntimeCall(
                "isVoidMethod",
                targetExpression,
                methodExpression,
                toArgumentArray(argumentsExpression)
        )

        new IfStatement(new BooleanExpression(isVoidMethod), noAssertion, assertAndRetrieveRecordedValue)
    }

    private StaticMethodCallExpression createRuntimeCall(String methodName, Expression... argumentExpressions) {
        ArgumentListExpression argumentListExpression = new ArgumentListExpression()
        for (Expression expression in argumentExpressions) {
            argumentListExpression.addExpression(expression)
        }

        new StaticMethodCallExpression(new ClassNode(Runtime), methodName, argumentListExpression)
    }

    private Expression toArgumentArray(Expression arguments) {
        List<Expression> argumentList
        if (arguments instanceof NamedArgumentListExpression) {
            argumentList = [arguments]
        } else {
            TupleExpression tuple = arguments
            argumentList = tuple.expressions
        }
        List<SpreadExpression> spreadExpressions = argumentList.findAll { it in SpreadExpression }
        if (spreadExpressions) {
            spreadExpressions.each { reportError(it, 'Spread expressions are not allowed here', sourceUnit) }
            null
        } else {
            new ArrayExpression(ClassHelper.OBJECT_TYPE, argumentList)
        }
    }
}

