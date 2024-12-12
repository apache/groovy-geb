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
package geb.buildadapter

import geb.BuildAdapter

/**
 * Loads values via system properties.
 */
class SystemPropertiesBuildAdapter implements BuildAdapter {

    static public final String BASE_URL_PROPERTY_NAME = "geb.build.baseUrl"
    static public final String REPORTS_DIR_PROPERTY_NAME = "geb.build.reportsDir"

    /**
     * Returns the system property {@code geb.build.baseUrl}.
     */
    String getBaseUrl() {
        System.getProperty(BASE_URL_PROPERTY_NAME)
    }

    /**
     * Returns a {@link java.io.File} constructed with the system property {@code geb.build.reportsDir}, or {@code null} if not set.
     */
    File getReportsDir() {
        def value = System.getProperty(REPORTS_DIR_PROPERTY_NAME)
        value ? new File(value) : null
    }

}