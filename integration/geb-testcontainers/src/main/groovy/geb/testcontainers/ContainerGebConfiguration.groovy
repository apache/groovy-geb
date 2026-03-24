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

/**
 * Implement this interface on a {@link ContainerGebSpec} subclass to configure the
 * protocol, hostname, reporting, and file detector for container-based browser tests.
 *
 * <p>Configuration is inherited by subclasses, and can be overridden by re-implementing
 * the desired methods.
 *
 * <p>Example:
 * <pre><code>
 * class MySpec extends ContainerGebSpec {
 *     &#64;Override
 *     String hostName() { 'testing.example.com' }
 *
 *     &#64;Override
 *     boolean reporting() { true }
 * }
 * </code></pre>
 *
 * @author James Daugherty
 * @since 4.1
 */
interface ContainerGebConfiguration {

    /**
     * The protocol that the container's browser will use to access the server under test.
     * <p>Defaults to {@code http}.
     */
    default String protocol() {
        'http'
    }

    /**
     * The hostname that the container's browser will use to access the server under test.
     * <p>Defaults to {@code host.testcontainers.internal}.
     * <p>This is useful when the server under test needs to be accessed with a certain hostname.
     */
    default String hostName() {
        GenericContainer.INTERNAL_HOST_HOSTNAME
    }

    /**
     * Whether reporting should be enabled for this test.
     * Add a {@code GebConfig.groovy} to customize the reporter configuration.
     */
    default boolean reporting() {
        false
    }

    /**
     * The {@link ContainerFileDetector} implementation to use for this class.
     *
     * @since 4.2
     * @see DefaultContainerFileDetector
     * @see UselessContainerFileDetector
     */
    default Class<? extends ContainerFileDetector> fileDetector() {
        DefaultContainerFileDetector
    }
}
