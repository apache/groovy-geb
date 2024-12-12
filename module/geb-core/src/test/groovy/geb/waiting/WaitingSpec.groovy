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
package geb.waiting

import geb.test.GebSpecWithCallbackServer

class WaitingSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            res.outputStream << """
                <html>
                <head>
                  <script type="text/javascript" charset="utf-8">
                    function showIn(i) {
                      setTimeout(function() {
                        document.body.innerHTML = "<div>a</div>";
                      }, i * 1000);
                    }
                  </script>
                </head>
                <body>
                </body>
                </html>
            """
        }
    }
}
