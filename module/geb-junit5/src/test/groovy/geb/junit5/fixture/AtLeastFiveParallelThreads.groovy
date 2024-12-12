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
package geb.junit5.fixture

import org.junit.platform.engine.ConfigurationParameters
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy
import org.spockframework.runtime.DefaultParallelExecutionConfiguration

class AtLeastFiveParallelThreads implements ParallelExecutionConfigurationStrategy {

    private static final int MINIMUM_PARALLELISM = 5

    private static final int KEEP_ALIVE_SECONDS = 30

    private static final int MAX_POOL_SIZE_BASE = 256

    @Override
    ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        def processors = Runtime.runtime.availableProcessors()
        if (processors < MINIMUM_PARALLELISM) {
            return new DefaultParallelExecutionConfiguration(
                    MINIMUM_PARALLELISM, MINIMUM_PARALLELISM, MAX_POOL_SIZE_BASE + MINIMUM_PARALLELISM,
                    MINIMUM_PARALLELISM, KEEP_ALIVE_SECONDS)
        }
        new DefaultParallelExecutionConfiguration(
                processors, processors, MAX_POOL_SIZE_BASE + processors,
                processors, KEEP_ALIVE_SECONDS)
    }
}
