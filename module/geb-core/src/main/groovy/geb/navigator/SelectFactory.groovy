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
package geb.navigator

import org.openqa.selenium.WebElement

class SelectFactory {

    static public final String SELECT_CLASS_NAME = "org.openqa.selenium.support.ui.Select"

    def createSelectFor(WebElement element) {
        loadSelectClass().newInstance(element)
    }

    protected Class loadSelectClass() {
        try {
            classLoaderToUse.loadClass(SELECT_CLASS_NAME)
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException(errorMessage)
        }
    }

    protected ClassLoader getClassLoaderToUse() {
        this.class.classLoader
    }

    protected getErrorMessage() {
        "Unable to find class '$SELECT_CLASS_NAME', which is required when interacting with select elements.\n" +
            "\n" +
            "This class is part of the selenium-support jar, which is not part of the WebDriver core that is depended on by each of the drivers. " +
            "This means that you need to add this jar to your project yourself as an explicit dependency.\n" +
            "This jar is available from maven central: http://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-support\n" +
            "The use of this class was introduced in Geb 0.6.1 due to changes in WebDriver 2.6.0"
    }
}