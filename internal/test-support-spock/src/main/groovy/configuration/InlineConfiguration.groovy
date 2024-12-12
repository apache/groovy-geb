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
package configuration

import geb.Configuration
import geb.ConfigurationLoader
import geb.test.CloseableTempDirectory

class InlineConfiguration {

    static Configuration parseConfigScript(String script) {
        parseConfigScript(null, script)
    }

    static Configuration parseConfigScript(String env, String script) {
        try(def tempDirectory = new CloseableTempDirectory()) {
            def directory = tempDirectory.file
            def groovyClassLoader = new GroovyClassLoader(InlineConfiguration.classLoader)
            groovyClassLoader.addClasspath(directory.absolutePath)
            def configFile = new File(directory, "GebConfig.groovy")
            configFile << script
            new ConfigurationLoader(env, null, groovyClassLoader).getConf(configFile.name)
        }
    }

}