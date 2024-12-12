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

class CallbackDriverFactory implements DriverFactory {

    final private Closure callback

    CallbackDriverFactory(Closure callback) {
        this.callback = callback
    }

    WebDriver getDriver() {
        try {
            def driver = callback()
            if (!(driver instanceof WebDriver)) {
                throw new DriverCreationException("callback '${callback}' returned '$driver' which is not a WebDriver implementation")
            }
            driver
        } catch (Throwable e) {
            throw new DriverCreationException("failed to create driver from callback '${callback}'", e)
        }
    }

}