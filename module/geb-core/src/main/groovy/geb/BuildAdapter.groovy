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
package geb

/**
 * Used to supply default configuration type values to allow a build system (or anything else)
 * to configure the Geb runtime from the outside.
 */
interface BuildAdapter {

    /**
     * Provides the default baseUrl to use when no value has been configured.
     * <p>
     * This method may return {@code null}.
     *
     * @see geb.Configuration#getBaseUrl()
     */
    String getBaseUrl()

    /**
     * Provides the default location to write report files.
     * <p>
     * This method may return {@code null}.
     *
     * @see geb.Configuration#getReportsDir()
     */
    File getReportsDir()

}