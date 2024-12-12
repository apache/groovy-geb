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

import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Represents a particular configuration of waiting, but does not encompass what is to be waited on.
 * <p>
 * Generally not used by user code, but used internally by {@link geb.Configuration} and {@link geb.waiting.WaitingSupport}.
 */
class Wait {

    /**
     * 5 seconds
     */
    static public final Number DEFAULT_TIMEOUT = 5

    /**
     * 100 milliseconds
     */
    static public final Number DEFAULT_RETRY_INTERVAL = 0.1

    private static final int HASHCODE_MULTIPLIER = 31

    /**
     * The maximum amount of seconds that something can be waited on.
     */
    final Number timeout

    /**
     * How many seconds to wait before trying something again while waiting.
     */
    final Number retryInterval

    /**
     * Whether we should append cause strings to the returned exception message or not
     */
    final boolean includeCauseInExceptionMessage

    String customMessage
    boolean quiet

    Wait(Number timeout = DEFAULT_TIMEOUT, Number retryInterval = DEFAULT_RETRY_INTERVAL, boolean includeCauseInExceptionMessage = false) {
        this.timeout = timeout
        this.retryInterval = [timeout, retryInterval].min()
        this.includeCauseInExceptionMessage = includeCauseInExceptionMessage
    }

    String toString() {
        "Wait[timeout: $timeout, retryInterval: $retryInterval]"
    }

    boolean equals(other) {
        if (this.is(other)) {
            true
        } else if (!(other instanceof Wait)) {
            false
        } else {
            this.timeout == other.timeout && this.retryInterval == other.retryInterval && this.includeCauseInExceptionMessage == other.includeCauseInExceptionMessage
        }
    }

    int hashCode() {
        int code = 41
        code = HASHCODE_MULTIPLIER * code + timeout.hashCode()
        code = HASHCODE_MULTIPLIER * code + retryInterval.hashCode()
        code = HASHCODE_MULTIPLIER * code + includeCauseInExceptionMessage.hashCode()
        code
    }

    /**
     * Invokes the given {@code block} every {@code retryInterval} seconds until it returns
     * a true value according to the Groovy Truth. If {@code block} does not return a truish value
     * within {@code timeout} seconds then a {@link geb.waiting.WaitTimeoutException} will be thrown.
     * <p>
     * If the given block is executing at the time when the timeout is reached, it will not be interrupted. This means that
     * this method may take longer than the specified {@code timeout}. For example, if the {@code block} takes 5 seconds
     * to complete but the timeout is 2 seconds, the wait is always going to take at least 5 seconds.
     * <p>
     * If {@code block} throws any {@link Throwable}, it is treated as a failure and the {@code block} will be tried
     * again after the {@code retryInterval} has expired. If the last invocation of {@code block} throws an exception
     * it will be the <em>cause</em> of the {@link geb.waiting.WaitTimeoutException} that will be thrown.
     */
    public <T> T waitFor(Closure<T> block) {
        def timeoutThreshold = timeoutThresholdFromNow()
        def pass
        def thrown = null

        try {
            pass = block()
        } catch (Throwable e) {
            pass = new UnknownWaitForEvaluationResult(e)
            thrown = e
        }

        def timedOut = timeoutThreshold - System.nanoTime() <= 0
        while (!pass && !timedOut) {
            sleepForRetryInterval()
            try {
                pass = block()
                thrown = null
            } catch (Throwable e) {
                pass = new UnknownWaitForEvaluationResult(e)
                thrown = e
            } finally {
                timedOut = timeoutThreshold - System.nanoTime() <= 0
            }
        }

        if (!pass && timedOut && !quiet) {
            throw new WaitTimeoutException(this, thrown, pass)
        }

        pass as T
    }

    private static long toMilliseconds(Number seconds) {
        seconds * 1000L
    }

    private static long toNanoseconds(Number seconds) {
        MILLISECONDS.toNanos(toMilliseconds(seconds))
    }

    private long timeoutThresholdFromNow() {
        System.nanoTime() + toNanoseconds(timeout)
    }

    /**
     * Blocks the caller for the retryInterval
     */
    private void sleepForRetryInterval() {
        Thread.sleep(toMilliseconds(retryInterval))
    }
}
