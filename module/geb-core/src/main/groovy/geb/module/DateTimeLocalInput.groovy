/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geb.module

import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

import static java.time.temporal.ChronoField.*

@Slf4j
class DateTimeLocalInput extends AbstractInput {

    private static final DateTimeFormatter DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendFraction(MILLI_OF_SECOND, 0, 3, true)
            .toFormatter()
    
    final String inputType = 'datetime-local'

    void setDateTime(LocalDateTime dateTime) {
        value(reformat(dateTime))
    }

    void setDateTime(String iso8601FormattedDateTime) {
        setDateTime(LocalDateTime.parse(iso8601FormattedDateTime))
    }

    LocalDateTime getDateTime() {
        String value = value()
        value ? LocalDateTime.parse(value) : null
    }

    @Override
    protected boolean isTypeValid(String type) {
        super.isTypeValid(type) || type == "text"
    }

    private static String reformat(LocalDateTime localDateTime) {
        String inputValue = localDateTime.format(DATE_TIME_FORMAT)
        if(localDateTime.toString() != inputValue) {
            log.warn("The datetime value {} was truncated to {} as it was being used to set the value of a <input type=\"datetime-local\" />",
                    localDateTime, 
                    inputValue)
        }
        return inputValue
    }
}
