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

import org.threeten.extra.YearWeek

class WeekInput extends AbstractInput {

    final String inputType = "week"

    void setWeek(YearWeek week) {
        value(week.toString())
    }

    void setWeek(String iso8601FormattedWeek) {
        value(iso8601FormattedWeek)
    }

    YearWeek getWeek() {
        parseWeek(value() as String)
    }

    YearWeek getMin() {
        parseWeek(attr("min"))
    }

    YearWeek getMax() {
        parseWeek(attr("max"))
    }

    Integer getStep() {
        attr("step")?.toInteger()
    }

    private YearWeek parseWeek(String string) {
        if (string == null || string.empty) {
            return null
        }
        YearWeek.parse(string)
    }

    @Override
    protected boolean isTypeValid(String type) {
        super.isTypeValid(type) || type == "text"
    }

}
