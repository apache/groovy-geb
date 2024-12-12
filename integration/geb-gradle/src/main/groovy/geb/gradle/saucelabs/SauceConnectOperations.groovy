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
package geb.gradle.saucelabs

import org.gradle.api.file.FileCollection

class SauceConnectOperations {

    private URLClassLoader sauceConnectManagerClassLoader

    private final FileCollection sauceConnect

    SauceConnectOperations(FileCollection sauceConnect) {
        this.sauceConnect = sauceConnect
    }

    def getOperatingSystem() {
        loadOperatingSystemClass().operatingSystem
    }

    Class loadSauceConnectFourManagerClass() {
        loadClass("com.saucelabs.ci.sauceconnect.SauceConnectFourManager")
    }

    String getDirectory() {
        def os = getOperatingSystem()
        if (os.metaClass.respondsTo(os, "getDirectory", Boolean)) {
            os.getDirectory(false)
        } else {
            os.directory
        }
    }

    private Class loadOperatingSystemClass() {
        loadClass('com.saucelabs.ci.sauceconnect.SauceConnectFourManager$OperatingSystem')
    }

    protected URLClassLoader getSauceConnectManagerClassLoader() {
        sauceConnectManagerClassLoader ?= new URLClassLoader(sauceConnect.files*.toURI()*.toURL() as URL[])
    }

    private Class loadClass(String name) {
        getSauceConnectManagerClassLoader().loadClass(name)
    }
}
