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

package geb.transform.implicitassertions;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

public abstract class ImplicitAssertionsTransformationUtil {

    public static void reportError(ASTNode node, String message, SourceUnit sourceUnit) {
        int line = node.getLineNumber() > 0 ? node.getLineNumber() : node.getLastLineNumber();
        int column = node.getColumnNumber() > 0 ? node.getColumnNumber() : node.getLastColumnNumber();
        SyntaxErrorMessage errorMessage = new SyntaxErrorMessage(new SyntaxException(message, line, column), sourceUnit);
        sourceUnit.getErrorCollector().addErrorAndContinue(errorMessage);
    }

}
