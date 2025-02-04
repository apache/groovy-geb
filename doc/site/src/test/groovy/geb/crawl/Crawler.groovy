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
package geb.crawl

import io.micronaut.http.MediaType
import org.codehaus.groovy.runtime.StackTraceUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import javax.net.ssl.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

abstract class Crawler {

    final int retryLimit = 3
    final int retryWaitMillis = 1000

    final String startingUrl
    final Set<String> knownBadHosts

    protected final AtomicInteger counter = new AtomicInteger(0)
    protected final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.runtime.availableProcessors() * 4)

    protected final ConcurrentMap<String, Link> seen = new ConcurrentHashMap<>()

    Crawler(String startingUrl, Set<String> knownBadHosts = [].toSet()) {
        this.startingUrl = startingUrl
        this.knownBadHosts = knownBadHosts
    }

    abstract List<String> findPageLinks(Response response)

    boolean isCrawlable(Link link) {
        isUnderStartUrl(link) && !link.uri.fragment
    }

    boolean isUnderStartUrl(Link link) {
        link.uri.toString().startsWith(startingUrl)
    }

    boolean shouldUseHeadRequest(Link url) {
        !isUnderStartUrl(url)
    }

    abstract boolean shouldValidateFragment(Link url)

    Collection<String> getDownloadableExtensions() {
        ["pdf", "zip", "jar"]
    }

    boolean isDownload(Link link) {
        downloadableExtensions.any { link.uri.toString().endsWith(".$it") }
    }

    void addPageErrors(Link link, Response response) {
        if (response.statusCode == 404) {
            link.errors << new StatusCodeError(response.statusCode)
        }

        def fragment = link.uri.fragment
        if (response.document && fragment && shouldValidateFragment(link)) {
            if (!response.document.select("a").any { Element it -> it.attr("name") == fragment } && response.document.getElementById(fragment) == null) {
                link.errors << new BadFragmentError(link.uri.fragment)
            }
        }
    }

    List<Link> crawl() {
        def link = new Link(startingUrl)
        seen.put(startingUrl, link)
        executorService.execute(toVisit(link))

        long startAt = System.currentTimeMillis()
        long stopAt = startAt + (1000 * 60 * 10) // 10 minutes
        while (counter.get() > 0 && System.currentTimeMillis() < stopAt) {
            sleep 100
        }

        if (counter.get() == 0) {
            new ArrayList<>(seen.values())
        } else {
            throw new RuntimeException("timeout")
        }
    }

    Runnable toVisit(Link link) {
        counter.incrementAndGet()
        return {
            try {
                visit(link)
            } catch (e) {
                link.errors << new ExceptionError(e)
            } finally {
                counter.getAndDecrement()
            }
        }
    }

    boolean isHttpUrl(String url) {
        url ==~ ~/^https?:\/.+/
    }

    protected visit(Link link) {
        link.attempt()

        if (!isDownload(link) && isCrawlable(link)) {
            visitCrawlable link
        } else {
            visitNonCrawlable link
        }
        if (link.errors) {
            if (link.attemptCount < retryLimit) {
                link.errors.clear()
                executorService.schedule(toVisit(link), retryWaitMillis, TimeUnit.MILLISECONDS)
            }
        }
    }

    protected visitCrawlable(Link link) {
        Response lastResponse = new Response(link.uri, openUrlConnection(link.uri))
        addPageErrors(link, lastResponse)

        if (!link.errors && lastResponse.contentType == MediaType.TEXT_HTML_TYPE) {
            findPageLinks(lastResponse).each { String it ->
                def newLink = toLink(link.uri, it)
                if (newLink) {
                    executorService.execute(toVisit(newLink))
                }
            }
        }
    }

    protected visitNonCrawlable(Link link) {
        def connection = openUrlConnection(link.uri)
        connection.instanceFollowRedirects = true
        def method = shouldUseHeadRequest(link) ? "HEAD" : "GET"
        connection.requestMethod = method

        def targetLink = link
        try {
            if (knownBadHosts.any { targetLink.uri.toString().contains(it) }) {
                throw new IOException()
            }
            while (connection.responseCode > 300 && connection.responseCode < 400) {
                def redirectTo = connection.getURL().toURI().resolve(connection.getHeaderField("location")).toString()
                targetLink = new Link(redirectTo)
                connection = openUrlConnection(targetLink.uri)
            }

            addPageErrors(targetLink, new Response(targetLink.uri, connection))
        } catch (IOException e) {
            targetLink.errors << new ExceptionError(e)
        }
    }

    @SuppressWarnings("UnnecessarySetter")
    protected HttpURLConnection openUrlConnection(URI uri) {
        HttpURLConnection connection = uri.toURL().openConnection() as HttpURLConnection

        connection.instanceFollowRedirects = false

        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        if (connection instanceof HttpsURLConnection) {
            def https = connection as HttpsURLConnection
            https.hostnameVerifier = new HostnameVerifier() {
                @Override
                boolean verify(String s, SSLSession sslSession) {
                    true
                }
            }

            def trustManager = new X509TrustManager() {
                @Override
                void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                X509Certificate[] getAcceptedIssuers() {
                    new X509Certificate[0]
                }
            }

            def sc = SSLContext.getInstance("SSL")
            sc.init([] as KeyManager[], [trustManager] as TrustManager[], new SecureRandom())
            https.setSSLSocketFactory(sc.socketFactory)
        }
        connection
    }

    protected Link toLink(URI currentUrl, String url) {
        URI uri = new URI(url.replaceAll("\\s", "%20").replaceAll("<", "%3C").replaceAll(">", "%3E"))
        def href = uri.isAbsolute() ? uri.toString() : currentUrl.resolve(uri).toString()
        href = NormalizeURL.normalize(href)
        if (isHttpUrl(href)) {
            assert href.startsWith('http')
            def newLink = new Link(href)
            def existing = seen.putIfAbsent(href, newLink)
            def link = existing ?: newLink
            link.referrers.add(currentUrl.toString())

            return existing ? null : newLink
        }

        null
    }

    static class Response {
        final int statusCode
        final URI uri
        final MediaType contentType
        final Document document

        protected final HttpURLConnection connection

        Response(URI uri, HttpURLConnection connection) {
            this.uri = uri
            this.connection = connection

            // Force the request
            statusCode = connection.responseCode
            String contentTypeHeader = connection.getHeaderField("Content-Type")
            if (contentTypeHeader != null) {
                contentType = MediaType.of(contentTypeHeader)
            }
            if (connection.requestMethod == "GET" && contentType == MediaType.TEXT_HTML_TYPE) {
                def stream = statusCode >= 400 ? connection.errorStream : connection.inputStream
                document = Jsoup.parse(stream, "UTF-8", uri.toString())
            } else {
                document = null
            }
        }
    }

    static class StatusCodeError {
        final int code

        StatusCodeError(int code) { this.code = code }

        String toString() { "HTTP status: $code" }
    }

    static class BadFragmentError {
        final String fragment

        BadFragmentError(String fragment) { this.fragment = fragment }

        String toString() { "Bad fragment: $fragment" }
    }

    static class ExceptionError {
        final Throwable exception

        ExceptionError(Throwable exception) { this.exception = StackTraceUtils.deepSanitize(exception) }

        String toString() {
            def stringWriter = new StringWriter()
            def printWriter = new PrintWriter(stringWriter)
            printWriter.println(exception.message)
            exception.printStackTrace(printWriter)
            stringWriter.toString()
        }
    }

    static class Link {
        final URI uri

        final Set<String> referrers = new HashSet().asSynchronized()
        final List errors = [].asSynchronized()

        int attemptCount = 0

        Link(String url) {
            this.uri = new URI(url)
        }

        String toString() {
            "$uri (referrers: $referrers, errors: $errors, attempts: $attemptCount)"
        }

        void attempt() {
            ++attemptCount
        }

        boolean equals(Object o) {
            if (this.is(o)) {
                return true
            }
            if (getClass() != o.class) {
                return false
            }

            Link link = (Link) o

            uri == link.uri
        }

        int hashCode() {
            uri.hashCode()
        }
    }
}
