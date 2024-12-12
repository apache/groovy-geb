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
package navigator

import fixture.GebSpecWithServerUsingJavascript

class DragAndDropSpec extends GebSpecWithServerUsingJavascript {

    def setup() {
        html """
            <html>
                <body style="margin: 0">
                    <div id="draggable" style="width: 100px; height: 100px; background-color: black"/>
                    ${interactjs()}
                    ${javascript '''
                        interact('#draggable')
                            .draggable({
                                onmove: function dragMoveListener (event) {
                                    var target = event.target,
                                        x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
                                        y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;

                                    target.style.webkitTransform =
                                    target.style.transform =
                                      'translate(' + x + 'px, ' + y + 'px)';

                                    target.setAttribute('data-x', x);
                                    target.setAttribute('data-y', y);
                                }
                            });
                    '''}
                    </body>
            </html>
        """
    }

    def "drag and dropping"() {
        when:
        // tag::verbose[]
        interact {
            clickAndHold($('#draggable'))
            moveByOffset(150, 200)
            release()
        }
        // end::verbose[]

        then:
        $('#draggable').x == 150
        $('#draggable').y == 200
    }

    def "drag and dropping using convenience method"() {
        when:
        // tag::convenience[]
        interact {
            dragAndDropBy($("#draggable"), 150, 200)
        }
        // end::convenience[]

        then:
        $('#draggable').x == 150
        $('#draggable').y == 200
    }
}
