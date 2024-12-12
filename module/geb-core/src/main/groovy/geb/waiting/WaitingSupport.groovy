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

interface WaitingSupport {

    /**
     * Uses the {@link geb.Configuration#getWaitPreset(java.lang.String) wait preset} from the {@code configuration}
     * with the given name to to wait for {@code block} to return a true value according to the Groovy Truth.
     *
     * @param waitPreset the name of the wait preset in {@code configuration} to use
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     * @see geb.Configuration#getWaitPreset(java.lang.String)
     */
    public <T> T waitFor(String waitPreset, Closure<T> block)

    public <T> T waitFor(Map params, String waitPreset, Closure<T> block)

    /**
     * Uses the {@link geb.Configuration#getDefaultWait() default wait} from the {@code configuration} to
     * wait for {@code block} to return a true value according to the Groovy Truth.
     *
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     * @see geb.Configuration#getDefaultWait()
     */
    public <T> T waitFor(Closure<T> block)

    public <T> T waitFor(Map params, Closure<T> block)

    /**
     * Invokes {@code block} every {@link geb.Configuration#getDefaultWaitRetryInterval()} seconds, until it returns
     * a true value according to the Groovy Truth, waiting at most {@code timeout} seconds.
     *
     * @param timeout the number of seconds to wait for block to return (roughly)
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     */
    public <T> T waitFor(Number timeout, Closure<T> block)

    public <T> T waitFor(Map params, Number timeout, Closure<T> block)

    /**
     * Invokes {@code block} every {@code interval} seconds, until it returns
     * a true value according to the Groovy Truth, waiting at most {@code timeout} seconds.
     *
     * @param interval the number of seconds to wait between invoking {@code block}
     * @param timeout the number of seconds to wait for block to return (roughly)
     * @param block what is to be waited on to return a true-ish value
     * @return the true-ish return value from {@code block}
     * @throws {@link geb.waiting.WaitTimeoutException} if the block does not produce a true-ish value in time
     */
    public <T> T waitFor(Number timeout, Number interval, Closure<T> block)

    public <T> T waitFor(Map params, Number timeout, Number interval, Closure<T> block)

}