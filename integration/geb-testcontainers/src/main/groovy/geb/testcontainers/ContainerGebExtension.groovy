/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package geb.testcontainers

import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.parallel.ExclusiveResource
import org.spockframework.runtime.model.parallel.ResourceAccessMode

import java.time.LocalDateTime

/**
 * A Spock Extension that manages the Testcontainers
 * lifecycle for a {@link ContainerGebSpec}.
 *
 * <p> ContainerGebSpec cannot be a
 * {@link geb.test.ManagedGebTest ManagedGebTest} with a static test manager because
 * the test manager needs to be rebuilt when the container is (re)initialized.
 * Instead, we initialize the same interceptors as the {@link geb.spock.GebExtension GebExtension} does.
 *
 * @author James Daugherty
 * @since 4.1
 */
@Slf4j
@CompileStatic
class ContainerGebExtension implements IGlobalExtension {

    ExclusiveResource exclusiveResource
    WebDriverContainerHolder holder

    @Override
    void start() {
        exclusiveResource = new ExclusiveResource(
                ContainerGebSpec.name,
                ResourceAccessMode.READ_WRITE
        )
        holder = new WebDriverContainerHolder(
                new GebContainerSettings(LocalDateTime.now())
        )
        addShutdownHook {
            holder.stop()
        }
    }

    @Override
    void stop() {
        holder.stop()
    }

    @Override
    void visitSpec(SpecInfo spec) {
        if (!isContainerGebSpec(spec)) {
            return
        }

        // Do not allow parallel execution since there's only 1 set of containers in testcontainers
        spec.addExclusiveResource(exclusiveResource)

        // Always initialize the container requirements first so the GebTestManager can properly configure the browser
        spec.addSharedInitializerInterceptor { invocation ->
            holder.reinitialize(invocation)

            ContainerGebSpec gebSpec = invocation.sharedInstance as ContainerGebSpec
            gebSpec.holder = holder

            // Code below here is from the geb.spock.GebExtension since there can only be 1 shared initializer per extension
            holder.testManager.beforeTestClass(invocation.spec.reflection)
            invocation.proceed()
        }

        spec.addSetupInterceptor { invocation ->
            // By this point, any server under test should be running, so setup the browser url correctly
            holder.setupBrowserUrl(invocation)
            invocation.proceed()
        }

        spec.addInterceptor { invocation ->
            try {
                invocation.proceed()
            } finally {
                holder.testManager.afterTestClass()
            }
        }

        spec.allFeatures*.addIterationInterceptor { invocation ->
            holder.restartVncRecordingContainer()
            holder.testManager.beforeTest(invocation.instance.getClass(), invocation.iteration.displayName)
            try {
                invocation.proceed()
            } finally {
                holder.testManager.afterTest()
            }
        }

        addOnFailureReporter(spec)
        spec.addListener(new GebRecordingTestListener(holder))
    }

    private static void addOnFailureReporter(SpecInfo spec) {
        List<MethodInfo> methods = spec.allFeatures*.featureMethod + spec.allFixtureMethods.toList()
        methods.each {
            it.addInterceptor(new GebOnFailureReporter())
        }
    }

    @TailRecursive
    private boolean isContainerGebSpec(SpecInfo spec) {
        if (spec) {
            if (spec.filename.startsWith("${ContainerGebSpec.simpleName}.")) {
                return true
            }
            return isContainerGebSpec(spec.superSpec)
        }
        return false
    }
}
