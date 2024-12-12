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
package geb.driver

import org.openqa.selenium.WebDriver
import geb.error.UnableToLoadAnyDriversException

class NameBasedDriverFactory implements DriverFactory {

    public static final String DRIVER_SEPARATOR = ":"

    final ClassLoader classLoader
    final String driverNames

    NameBasedDriverFactory(ClassLoader classLoader, String driverNames) {
        this.classLoader = classLoader
        this.driverNames = driverNames
    }

    WebDriver getDriver() {
        def potentials = getPotentialDriverClassNames()

        def driverClass
        for (potential in potentials) {
            driverClass = attemptToLoadDriverClass(potential)
            if (driverClass) {
                break
            }
        }

        if (driverClass) {
            driverClass.newInstance()
        } else {
            throw new UnableToLoadAnyDriversException(potentials as String[])
        }
    }

    protected attemptToLoadDriverClass(String driverClassName) {
        try {
            classLoader.loadClass(driverClassName)
        } catch (ClassNotFoundException e) {
            null
        }
    }

    protected getPotentialDriverClassNames() {
        driverNames.split(DRIVER_SEPARATOR).collect {
            DriverRegistry.translateFromShortNameIfRequired(it)
        }
    }

}