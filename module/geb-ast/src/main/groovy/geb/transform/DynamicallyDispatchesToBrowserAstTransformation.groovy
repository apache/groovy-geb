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
package geb.transform

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params

@SuppressWarnings("SpaceAfterComma")
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class DynamicallyDispatchesToBrowserAstTransformation extends AbstractASTTransformation {

    public static final String NAME_PARAM_NAME = "name"
    public static final String PROPERTY_MISSING_METHOD_NAME = "propertyMissing"

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        def classNode = nodes[1] as ClassNode

        addMethodMissingNode(classNode)
        addMissingPropertyGetter(classNode)
        addMissingPropertySetter(classNode)
    }

    private void addMethodMissingNode(ClassNode classNode) {
        def parameters = params(param(STRING_TYPE, NAME_PARAM_NAME), param(OBJECT_TYPE, "args"))
        def code = macro { return getBrowser()."$name"(*args) } as Statement

        addMethod(classNode, "methodMissing", parameters, code)
    }

    private void addMissingPropertySetter(ClassNode classNode) {
        def parameters = params(param(STRING_TYPE, NAME_PARAM_NAME), param(OBJECT_TYPE, "value"))
        def code = macro { return getBrowser()."$name" = value } as Statement

        addMethod(classNode, PROPERTY_MISSING_METHOD_NAME, parameters, code)
    }

    private void addMissingPropertyGetter(ClassNode classNode) {
        def parameters = params(param(STRING_TYPE, NAME_PARAM_NAME))
        def code = macro { return getBrowser()."$name" } as Statement

        addMethod(classNode, PROPERTY_MISSING_METHOD_NAME, parameters, code)
    }

    private void addMethod(ClassNode classNode, String methodName, Parameter[] parameters, Statement code) {
        def methodNode = new MethodNode(
                methodName,
                ACC_PUBLIC,
                OBJECT_TYPE,
                parameters,
                [] as ClassNode[],
                code
        )

        classNode.addMethod(methodNode)
    }

}
