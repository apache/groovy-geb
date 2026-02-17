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

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.InvokerHelper;

@CompileStatic
public abstract class Runtime {

    private static final ThreadLocal<Object> recordedValue = new ThreadLocal<>();

    @SuppressWarnings("UnusedDeclaration")
    public static boolean isVoidMethod(Object target, String method, Object... args) {

        if (target == null) {
            return false;
        }

        Class[] argTypes = new Class[args.length];
        int i = 0;
        for (Object arg : args) {
            argTypes[i++] = arg == null ? null : arg.getClass();
        }

        MetaClass metaClass = target instanceof Class ? InvokerHelper.getMetaClass((Class) target) : InvokerHelper.getMetaClass(target);

        MetaMethod metaMethod = metaClass.pickMethod(method, argTypes);
        if (metaMethod == null) {
            return false;
        }

        Class returnType = metaMethod.getReturnType();
        return returnType == void.class || returnType == Void.class;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Object recordValue(Object value) {
        recordedValue.set(value);
        return value;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Object retrieveRecordedValue() {
        Object value = recordedValue.get();
        recordedValue.remove();
        return value;
    }

}
