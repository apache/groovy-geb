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
package geb.module

import java.time.LocalTime

class TimeInput extends AbstractInput {

    final String inputType = "time"

    void setTime(LocalTime time) {
        value(time.toString())
    }

    void setTime(String iso8601FormattedTime) {
        value(iso8601FormattedTime)
    }

    LocalTime getTime() {
        parseTime(value() as String)
    }

    LocalTime getMin() {
        parseTime(attr("min"))
    }

    LocalTime getMax() {
        parseTime(attr("max"))
    }

    Integer getStep() {
        attr("step")?.toInteger()
    }

    private LocalTime parseTime(String string) {
        if (string == null || string.empty) {
            return null
        }
        LocalTime.parse(string)
    }

    @Override
    protected boolean isTypeValid(String type) {
        super.isTypeValid(type) || type == "text"
    }

}
