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
package fixture

import geb.test.GebSpecWithCallbackServer

class GebSpecWithServerUsingJavascript extends GebSpecWithCallbackServer {

    public static final String JQUERY_RESOURCE_NAME = "jquery-2.1.4.min"

    String jqueryResource() {
        resource(JQUERY_RESOURCE_NAME)
    }

    String jquery() {
        resourceJavascript(JQUERY_RESOURCE_NAME)
    }

    String interactjs() {
        resourceJavascript("interact-1.2.4.min")
    }

    String resource(String resourceName) {
        getClass().getResource("/${resourceName}.js").text
    }

    String resourceJavascript(String resourceName) {
        javascript(resource(resourceName))
    }

    String javascript(String code) {
        """
            <script type="text/javascript">
                $code
            </script>
        """
    }
}