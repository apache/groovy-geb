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

import groovy.transform.CompileStatic
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

@CompileStatic
class ImplicitAssertionsTransformationVisitor extends ClassCodeVisitorSupport {

    private static final List<ImplicitlyAssertedMethodCallMatcher> IMPLICITLY_ASSERTED_METHOD_CALL_MATCHERS = [
        new ConfigurableByNameImplicitlyAssertedMethodCallMatcher("waitFor"),
        new ConfigurableByNameImplicitlyAssertedMethodCallMatcher("refreshWaitFor"),
        new ByNameImplicitlyAssertedMethodCallMatcher("at")
    ] as List<ImplicitlyAssertedMethodCallMatcher>
    private static final String WAIT_CONDITION = "waitCondition"

    SourceUnit sourceUnit

    ImplicitAssertionsTransformationVisitor(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    @Override
    void visitField(FieldNode node) {
        if (node.static && node.initialExpression instanceof ClosureExpression) {
            switch (node.name) {
                case 'at':
                    transformEachStatement((ClosureExpression) node.initialExpression, true)
                    break
                case 'content':
                    visitContentDsl((ClosureExpression) node.initialExpression)
                    break
            }
        }
    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        if (statement.expression instanceof MethodCallExpression) {
            def expression = (MethodCallExpression) statement.expression
            if (isSpockVerifyMethodConditionCall(expression)) {
                compensateForSpock(expression)
            } else if (expression.arguments instanceof ArgumentListExpression) {
                def arguments = (ArgumentListExpression) expression.arguments
                potentiallyTransform(expression.methodAsString, arguments.expressions)
            }
        }
    }

    void compensateForSpock(MethodCallExpression expression) {
        if (expression.arguments instanceof ArgumentListExpression) {
            def arguments = (ArgumentListExpression) expression.arguments
            def argumentExpressions = arguments.expressions

            if (argumentExpressions.size() == 12) {
                visitVerifyMethodConditionCall(argumentExpressions, 7)
            }
        }
    }

    boolean isSpockVerifyMethodConditionCall(MethodCallExpression expression) {
        if (expression.objectExpression instanceof ClassExpression && expression.method instanceof ConstantExpression) {
            def classExpression = (ClassExpression) expression.objectExpression
            def method = (ConstantExpression) expression.method

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
            if (verifyMethodConditionArgsArgument instanceof ArrayExpression) {
                def values = ((ArrayExpression) verifyMethodConditionArgsArgument).expressions.collect { argumentExpression ->
                    extractRecordedValueExpression(argumentExpression)
                }

                potentiallyTransform(methodName, values)
            }
        }
    }

    Expression extractRecordedValueExpression(Expression valueRecordExpression) {
        if (valueRecordExpression instanceof MethodCallExpression) {
            def methodCallExpression = (MethodCallExpression) valueRecordExpression

            if (methodCallExpression.arguments instanceof ArgumentListExpression) {
                def arguments = (ArgumentListExpression) methodCallExpression.arguments

                if (arguments.expressions.size() >= 2) {
                    return arguments.expressions.get(1)
                }
            }
        }

        null
    }

    def getConstantValueOfType(Expression expression, Class type) {
        if (expression != null && expression instanceof ConstantExpression) {
            Object value = ((ConstantExpression) expression).value
            type.isInstance(value) ? value : null
        } else {
            null
        }
    }

    boolean isTransformable(ExpressionStatement statement) {
        if (statement.expression instanceof BinaryExpression) {
            def binaryExpression = (BinaryExpression) statement.expression
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
        arguments && arguments.last() instanceof ClosureExpression
    }

    private boolean requiredOptionSpecifiedAsFalse(ArgumentListExpression arguments) {
        def paramMap = arguments.expressions.find { it instanceof MapExpression } as MapExpression
        paramMap?.mapEntryExpressions?.any {
            if (it.keyExpression instanceof ConstantExpression && it.valueExpression instanceof ConstantExpression) {
                def key = (ConstantExpression) it.keyExpression
                def value = (ConstantExpression) it.valueExpression
                key.value == 'required' && value.value == false
            }
        }
    }

    private Expression option(ArgumentListExpression arguments, String optionName) {
        def paramMap = arguments.expressions.find { it instanceof MapExpression } as MapExpression
        paramMap?.mapEntryExpressions?.find {
            if (it.keyExpression instanceof ConstantExpression) {
                def key = (ConstantExpression) it.keyExpression
                key.value == optionName
            }
        }?.valueExpression
    }

    private void visitContentDsl(ClosureExpression closureExpression) {
        def blockStatement = closureExpression.code as BlockStatement
        blockStatement.statements.each { statement ->
            if (statement instanceof ExpressionStatement) {
                def expressionStatement = (ExpressionStatement) statement
                if (expressionStatement.expression instanceof MethodCallExpression) {
                    def methodCall = (MethodCallExpression) expressionStatement.expression
                    if (methodCall.arguments instanceof ArgumentListExpression) {
                        def arguments = (ArgumentListExpression) methodCall.arguments
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
        if (waitCondition instanceof ClosureExpression) {
            transformEachStatement(waitCondition, false)
        }
    }

    private void handleWaitingContent(ArgumentListExpression arguments) {
        if ((option(arguments, "wait") || option(arguments, WAIT_CONDITION)) && !requiredOptionSpecifiedAsFalse(arguments)) {
            transformEachStatement(arguments.expressions.last() as ClosureExpression, true)
        }
    }

    private void transformEachStatement(ClosureExpression closureExpression, boolean appendTrueToNonAssertedStatements) {
        def blockStatement = closureExpression.code as BlockStatement
        def iterator = blockStatement.statements.listIterator()
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
        if (statement instanceof ExpressionStatement) {
            def expressionStatement = (ExpressionStatement) statement
            if (!(expressionStatement.expression instanceof DeclarationExpression)
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

        if (expression instanceof MethodCallExpression) {
            def methodCall = (MethodCallExpression) expression

            replacement = wrapInVoidMethodCheck(
                    expression,
                    assertAndRetrieveRecordedValue,
                    methodCall.objectExpression,
                    methodCall.method,
                    methodCall.arguments,
                    appendTrueToNonAssertedStatements
            )
        } else if (expression instanceof StaticMethodCallExpression) {
            def methodCall = (StaticMethodCallExpression) expression

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
            argumentList = [arguments] as List<Expression>
        } else {
            def tuple = arguments as TupleExpression
            argumentList = tuple.expressions
        }
        def spreadExpressions = argumentList.findAll { it instanceof SpreadExpression } as List<SpreadExpression>
        if (spreadExpressions) {
            spreadExpressions.each { reportError(it, 'Spread expressions are not allowed here', sourceUnit) }
            null
        } else {
            new ArrayExpression(ClassHelper.OBJECT_TYPE, argumentList)
        }
    }
}

