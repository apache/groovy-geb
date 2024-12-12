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
package geb.spock

import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.builtin.RetryFeatureInterceptor
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Retry

import java.lang.annotation.Annotation

import static org.spockframework.runtime.model.MethodKind.CLEANUP
import static org.spockframework.runtime.model.MethodKind.SETUP

class RetryFixturesExtension implements IAnnotationDrivenExtension<RetryFixtures> {

    @Override
    void visitSpecAnnotation(RetryFixtures annotation, SpecInfo spec) {
        def allSetupMethods = spec.bottomSpec.allFixtureMethods
            .findAll { it.kind in [SETUP, CLEANUP] } as Collection<MethodInfo>

        allSetupMethods*.addInterceptor(
            new RetryFeatureInterceptor(new Retry() {

                @Override
                Class<? extends Throwable>[] exceptions() {
                    [Exception, AssertionError]
                }

                @Override
                Class<? extends Closure> condition() {
                    annotation.condition()
                }

                @Override
                int count() {
                    3
                }

                @Override
                int delay() {
                    0
                }

                @Override
                Retry.Mode mode() {
                    throw new UnsupportedOperationException()
                }

                @Override
                Class<? extends Annotation> annotationType() {
                    Retry
                }
            })
        )
    }
}
