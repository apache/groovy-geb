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

import geb.error.GebAssertionError

import static java.lang.System.lineSeparator

/**
 * Thrown when a wait operation exceeds its timeout.
 *
 * The {@code cause} of the exception will be the exception thrown while waiting.
 */
class WaitTimeoutException extends GebAssertionError {

    final Object lastEvaluationValue

    protected final Wait wait

    WaitTimeoutException(Wait wait) {
        this(wait, null, null)
    }

    WaitTimeoutException(Wait wait, Throwable cause) {
        this(wait, cause, null)
    }

    WaitTimeoutException(Wait wait, Throwable cause, Object lastEvaluationValue) {
        super(toMessage(wait, cause), cause)
        this.wait = wait
        this.lastEvaluationValue = lastEvaluationValue
    }

    private static String toMessage(Wait wait, Throwable cause) {
        StringBuilder message = new StringBuilder("condition did not pass in $wait.timeout seconds")
        if (wait.customMessage != null) {
            message.append("($wait.customMessage)")
        }
        if (cause != null) {
            if (wait.includeCauseInExceptionMessage) {
                message.append(". Failed with exception:${lineSeparator()}$cause")
            } else {
                message.append(" (failed with exception)")
            }
        }
        message
    }

}
