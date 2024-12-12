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
package geb.download

interface DownloadSupport {

    /**
     * Creates a http url connection to a url, that has the same cookies as the browser.
     * Resolves the base and the uri to the current browser page
     */
    HttpURLConnection download()

    /**
     * Creates a http url connection to a url, that has the same cookies as the browser.
     * <p>
     * Valid options are:
     *
     * <ul>
     * <li>{@code uri} - the uri to resolve relative to the base option (current browser page used if {@code null})
     * <li>{@code base} - what to resolve the uri against (current browser page used if {@code null})
     * </ul>
     */
    HttpURLConnection download(Map options)

    /**
     * Calls download with the single option '{@code uri}' as the given value.
     */
    HttpURLConnection download(String uri)

    /**
     * Opens a url connection via {@link #download(Map)} using an empty Map and returns the response input stream.
     */
    InputStream downloadStream()

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response input stream.
     */
    InputStream downloadStream(Map options)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response input stream.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    InputStream downloadStream(Map options, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(String)} and returns the response input stream.
     */
    InputStream downloadStream(String uri)

    /**
     * Opens a url connection via {@link #download(String)} and returns the response input stream.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    InputStream downloadStream(String uri, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response input stream.
     * <p>
     * connectionConfig is called with the {@link HttpURLConnection} before the request is made.
     */
    InputStream downloadStream(Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response text, if the content type was textual.
     */
    String downloadText()

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response text, if the content type was textual.
     */
    String downloadText(Map options)
    /**
     * Opens a url connection via {@link #download(Map)} and returns the response text, if the content type was textual.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    String downloadText(Map options, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(String)} and returns the response text, if the content type was textual.
     */
    String downloadText(String uri)
    /**
     * Opens a url connection via {@link #download(String)} and returns the response text, if the content type was textual.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    String downloadText(String uri, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response text, if the content type was textual.
     * <p>
     * connectionConfig is called with the {@link HttpURLConnection} before the request is made.
     */
    String downloadText(Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(Map)} by passing an empty Map and returns the raw bytes.
     */
    byte[] downloadBytes()

    /**
     * Opens a url connection via {@link #download(Map)} and returns the raw bytes.
     */
    byte[] downloadBytes(Map options)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the raw bytes.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    byte[] downloadBytes(Map options, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the raw bytes.
     * <p>
     * connectionConfig is called with the {@link HttpURLConnection} before the request is made.
     */
    byte[] downloadBytes(Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(String)} and returns the raw bytes.
     */
    byte[] downloadBytes(String uri)

    /**
     * Opens a url connection via {@link #download(String)} and returns the raw bytes.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    byte[] downloadBytes(String uri, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(Map)} by passing an empty Map and returns the content object.
     */
    Object downloadContent()

    /**
     * Opens a url connection via {@link #download(Map)} and returns the content object.
     */
    Object downloadContent(Map options)

    /**
     * Opens a url connection via {@link #download(Map)} and returns the content object.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     *
     * @see URLConnection#getContent()
     */
    Object downloadContent(Map options, Closure connectionConfig)

    /**
     * Opens a url connection via {@link #download(String)} and returns the content object.
     */
    Object downloadContent(String uri)

    /**
     * Opens a url connection via {@link #download(String)} and returns the content object.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     *
     * @see URLConnection#getContent()
     */
    Object downloadContent(String uri, Closure connectionConfig)
    /**
     * Opens a url connection via {@link #download(String)} and returns the content object.
     * <p>
     * connectionConfig is called with the {@link HttpURLConnection} before the request is made.
     *
     * @see URLConnection#getContent()
     */
    Object downloadContent(Closure connectionConfig)
}