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
package geb.webstorage

import geb.test.GebSpecWithCallbackServer
import geb.test.browsers.Chrome
import geb.test.browsers.RequiresRealBrowser

@RequiresRealBrowser
@Chrome
abstract class AbstractWebStorageSpec extends GebSpecWithCallbackServer {

    abstract String getStorageObjectName()
    abstract WebStorage getStorage()

    def setup() {
        html {}
    }

    def cleanup() {
        js.exec """
            ${storageObjectName}.clear();
        """
    }

    def "reading values from local storage"() {
        given:
        js.exec """
            ${storageObjectName}.setItem("from-javascript", "value from javascript");
        """

        expect:
        storage["from-javascript"] == "value from javascript"
    }

    def "writing values to local storage"() {
        when:
        storage["from-geb"] = "value from geb"

        then:
        js.exec("""return ${storageObjectName}.getItem("from-geb");""") == "value from geb"
    }

    def "listing keys in local storage"() {
        given:
        js.exec """
            ${storageObjectName}.setItem("from-javascript", "value from javascript");
        """

        expect:
        storage.keySet() == ["from-javascript"].toSet()
    }

    def "clearing keys in local storage"() {
        given:
        js.exec """
            ${storageObjectName}.setItem("from-javascript", "value from javascript");
        """

        when:
        storage.clear()

        then:
        js.exec("""return ${storageObjectName}.length;""") == 0
    }

    def "removing items from local storage"() {
        given:
        js.exec """
            ${storageObjectName}.setItem("from-javascript", "value from javascript");
        """

        when:
        storage.remove("from-javascript")

        then:
        js.exec("""return ${storageObjectName}.getItem("from-javascript");""") == null
    }

    def "obtaining element count in local storage"() {
        given:
        js.exec """
            ${storageObjectName}.setItem("from-javascript", "value from javascript");
        """

        expect:
        storage.size() == 1
    }

}
