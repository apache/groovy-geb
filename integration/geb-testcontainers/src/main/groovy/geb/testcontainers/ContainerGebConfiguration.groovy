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

import org.testcontainers.containers.GenericContainer

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Annotation to configure the protocol, hostname, reporting, and file detector
 * for container-based browser tests. Apply to a {@link ContainerGebSpec} subclass.
 *
 * <p>Values set here override the defaults from {@link ContainerGebSpec}.
 * This annotation is {@link Inherited}, so subclasses inherit their parent's configuration.
 * Subclasses can re-apply the annotation to override specific values.
 *
 * <p>For configuration that requires dynamic values or complex logic, override
 * the corresponding methods on {@link ContainerGebSpec} directly instead.
 *
 * <p>Example:
 * <pre><code>
 * &#64;ContainerGebConfiguration(hostName = 'testing.example.com', reporting = true)
 * class MySpec extends ContainerGebSpec {
 *     // ...
 * }
 * </code></pre>
 *
 * @author James Daugherty
 * @since 4.1
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface ContainerGebConfiguration {

    static final String DEFAULT_HOSTNAME_FROM_CONTAINER = GenericContainer.INTERNAL_HOST_HOSTNAME
    static final String DEFAULT_PROTOCOL = 'http'

    /**
     * The protocol that the container's browser will use to access the server under test.
     * <p>Defaults to {@code http}.
     */
    String protocol() default DEFAULT_PROTOCOL

    /**
     * The hostname that the container's browser will use to access the server under test.
     * <p>Defaults to {@code host.testcontainers.internal}.
     */
    String hostName() default DEFAULT_HOSTNAME_FROM_CONTAINER

    /**
     * Whether reporting should be enabled for this test.
     */
    boolean reporting() default false

    /**
     * The {@link ContainerFileDetector} implementation to use for this class.
     *
     * @since 4.2
     * @see DefaultContainerFileDetector
     * @see UselessContainerFileDetector
     */
    Class<? extends ContainerFileDetector> fileDetector() default DefaultContainerFileDetector
}
