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
package geb.error

import geb.AtVerificationResult
import geb.Page
import geb.UnexpectedPage

import static java.lang.System.lineSeparator

class UnexpectedPageException extends GebException {

    UnexpectedPageException(Page page) {
        this(page.getClass())
    }

    UnexpectedPageException(Class<? extends Page> page) {
        super("At checker page verification failed for page $page.name")
    }

    UnexpectedPageException(Page page, Throwable cause) {
        super("At checker page verification failed for page $page", cause)
    }

    UnexpectedPageException(Page actualPage, Class<? extends Page> expectedPage) {
        super(buildMessage(actualPage, "expected to be at $expectedPage.name"))
    }

    UnexpectedPageException(Page actualPage, Page expectedPage) {
        super(buildMessage(actualPage, "expected to be at ${expectedPage.getClass().name}"))
    }

    UnexpectedPageException(Page actualPage, Class<? extends Page>[] potentials) {
        super(buildMessage(actualPage, "trying to find page match (given potentials: $potentials)"))
    }

    UnexpectedPageException(Page actualPage, Page[] potentials) {
        super(buildMessage(actualPage, "trying to find page match (given potentials: $potentials)"))
    }

    UnexpectedPageException(Map<? extends Page, AtVerificationResult> pageVerificationResults) {
        super("Unable to find page match. At checker verification results:${lineSeparator()}${lineSeparator()}${format(pageVerificationResults)}.")
    }

    private static String format(Map<? extends Page, AtVerificationResult> pageVerificationResults) {
        def textResults = pageVerificationResults.entrySet().collect { atVerificationResultEntry ->
            "Result for ${atVerificationResultEntry.key.getClass().name}: $atVerificationResultEntry.value"
        }
        textResults.join("${lineSeparator()}${lineSeparator()}")
    }

    private static buildMessage(Page actualPage, String message) {
        def standardMessage = "An unexpected page ${actualPage.getClass().name} was encountered when $message."

        actualPage instanceof UnexpectedPage ? "$standardMessage ${actualPage.unexpectedPageMessage}" : standardMessage
    }
}
