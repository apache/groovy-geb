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

import geb.Browser
import org.openqa.selenium.Cookie

/**
 * Provides methods to directly download content into the running program using HttpURLConnection.
 * <p>
 * Any cookies that the browser currently has will be automatically transferred to the url connection,
 * allowing it to assume the context of the browser.
 * <p>
 * An instance of this class will be mixed in to all browser, page and module objects making these methods
 * public methods on those objects.
 */
class DefaultDownloadSupport implements DownloadSupport {

    /*
        NOTE - if public methods are added here, make sure they are also added to the binding updater.
    */

    // HTTP 1.1 states that this charset is the default if none was specified
    static final private DEFAULT_CHARSET = "ISO-8859-1"

    final private Browser browser

    DefaultDownloadSupport(Browser browser) {
        this.browser = browser
    }

    /**
     * Creates a http url connection to a url, that has the same cookies as the browser.
     * <p>
     * Valid options are:
     *
     * <ul>
     * <li>{@code uri} - <em>optional</em> - the uri to resolve relative to the base option (current browser page used if {@code null})
     * <li>{@code base} - <em>optional</em> - what to resolve the uri against (current browser page used if {@code null})
     * </ul>
     */
    HttpURLConnection download(Map options = [:]) {
        def url = resolveUrl(options)
        def connection = url.openConnection()
        applyCookies(connection, browser)
        browser.config.getDownloadConfig()?.call(connection)
        connection
    }

    HttpURLConnection download(String uri) {
        download(uri: uri)
    }

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response input stream.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    InputStream downloadStream(Map options = [:], Closure connectionConfig = null) {
        wrapInDownloadException(downloadWithConfig(options, connectionConfig)) { it.inputStream }
    }

    /**
     * Opens a url connection via {@link #download(String)} and returns the response input stream.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    InputStream downloadStream(String uri, Closure connectionConfig = null) {
        downloadStream(uri: uri, connectionConfig)
    }

    InputStream downloadStream(Closure connectionConfig) {
        downloadStream([:], connectionConfig)
    }

    /**
     * Opens a url connection via {@link #download(Map)} and returns the response text, if the content type was textual.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    String downloadText(Map options = [:], Closure connectionConfig = null) {
        def connection = downloadWithConfig(options, connectionConfig)
        connection.connect()
        def contentType = connection.contentType

        if (isTextContentType(contentType)) {
            def charset = determineCharset(contentType)
            wrapInDownloadException(connection) { it.inputStream.getText(charset) }
        } else {
            throw new DownloadException(connection, "cannot extract text from connection as content type is non text (is: $contentType)")
        }
    }

    /**
     * Opens a url connection via {@link #download(String)} and returns the response text, if the content type was textual.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    String downloadText(String uri, Closure connectionConfig = null) {
        downloadText(uri: uri, connectionConfig)
    }

    String downloadText(Closure connectionConfig) {
        downloadText([:], connectionConfig)
    }

    /**
     * Opens a url connection via {@link #download(Map)} and returns the raw bytes.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    byte[] downloadBytes(Map options = [:], Closure connectionConfig = null) {
        downloadStream(options, connectionConfig).bytes
    }

    byte[] downloadBytes(Closure connectionConfig) {
        downloadStream(connectionConfig).bytes
    }

    /**
     * Opens a url connection via {@link #download(String)} and returns the raw bytes.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     */
    byte[] downloadBytes(String uri, Closure connectionConfig = null) {
        downloadBytes(uri: uri, connectionConfig)
    }

    /**
     * Opens a url connection via {@link #download(Map)} and returns the content object.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     *
     * @see URLConnection#getContent()
     */
    Object downloadContent(Map options = [:], Closure connectionConfig = null) {
        wrapInDownloadException(downloadWithConfig(options, connectionConfig)) { it.content }
    }

    /**
     * Opens a url connection via {@link #download(String)} and returns the content object.
     * <p>
     * If connectionConfig is given, it is called with the {@link HttpURLConnection} before the request is made.
     *
     * @see URLConnection#getContent()
     */
    Object downloadContent(String uri, Closure connectionConfig = null) {
        downloadContent(uri: uri, connectionConfig)
    }

    Object downloadContent(Closure connectionConfig) {
        downloadContent([:], connectionConfig)
    }

    private HttpURLConnection downloadWithConfig(Map options, Closure config) {
        def connection = download(options)
        config?.call(connection)
        connection
    }

    /**
     * Returns a URL for what is to be downloaded.
     * <p>
     * If uri is non {@code null}, it is resolved against the browser's current page url. If it is {@code null},
     * the browser's current page url will be returned.
     */
    private URL resolveUrl(Map options) {
        def uri = options.uri
        def base = options.base ?: browser.driver.currentUrl
        uri ? new URI(base).resolve(uri).toURL() : new URL(base)
    }

    /**
     * Copies the browser's current cookies to the given connection via the "Cookie" header.
     */
    private applyCookies(HttpURLConnection connection, Browser browser) {
        applyCookies(connection, browser.driver.manage().cookies)
    }

    /**
     * Copies the given cookies to the given connection via the "Cookie" header.
     */
    private applyCookies(HttpURLConnection connection, Collection<Cookie> cookies) {
        def cookieHeader = cookies.collect { "${it.name}=${it.value}" }.join("; ")
        connection.setRequestProperty("Cookie", cookieHeader)
    }

    private boolean isTextContentType(String contentType) {
        contentType?.startsWith("text/")
    }

    private determineCharset(String contentType) {
        if (contentType) {
            def parts = contentType.split(";")*.trim()
            def charsetPart = parts.find { it.startsWith("charset=") }
            if (charsetPart) {
                charsetPart.split("=", 2)[1]
            } else {
                DEFAULT_CHARSET
            }
        } else {
            DEFAULT_CHARSET
        }
    }

    private wrapInDownloadException(HttpURLConnection connection, Closure operation) {
        try {
            operation(connection)
        } catch (Throwable e) {
            throw new DownloadException(connection, "An error occurred during the download operation", e)
        }
    }
}
