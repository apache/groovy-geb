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
 * Loads the {@link BuildAdapter} implementation class to be used.
 */
class BuildAdapterFactory {

    /**
     * The system property used to specify the {@link geb.BuildAdapter} implementation ({@code geb.build.adapter}).
     */
    static public final String ADAPTER_PROPERTY_NAME = "geb.build.adapter"

    /**
     * The build adapter to use.
     * <p>
     * If the system property {@code geb.build.adapter} is set, the class by that name is loaded, instatied with no args, and returned.
     * Otherwise, an instance of {@link geb.buildadapter.SystemPropertiesBuildAdapter} will be returned.
     * <p>
     *
     * @param classLoader The class loader to attempt to load the class with
     * @throws {@link java.lang.ClassNotFoundException} If the system property specifies a non existent class
     */
    static BuildAdapter getBuildAdapter(ClassLoader classLoader) throws ClassNotFoundException {
        def className = System.getProperty(ADAPTER_PROPERTY_NAME)
        if (className) {
            classLoader.loadClass(className).newInstance()
        } else {
            new SystemPropertiesBuildAdapter()
        }
    }

}