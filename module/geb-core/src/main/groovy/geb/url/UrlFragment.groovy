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
package geb.url

import static com.google.common.net.UrlEscapers.urlFormParameterEscaper

/**
 * A marker class wrapping around a {@code String} that can be used with {@link geb.Page#to(java.util.Map, geb.url.UrlFragment, java.lang.Object)} for navigating to page urls that contain a fragment
 * identifier.
 */
class UrlFragment {

    private final String fragment

    private UrlFragment(String fragment) {
        this.fragment = fragment
    }

    /**
     * A factory method that creates a url fragment using the passed argument as is.
     * @param fragment fragment identifier value
     * @return url fragment representing the string passed as the argument
     */
    static UrlFragment of(String fragment) {
        new UrlFragment(fragment)
    }

    /**
     * A factory method that creates a url fragment using form encoded representation of the {@code Map} passed as the argument.
     * This method is useful when dealing with single page applications that store state in the fragment identifier by form encoding it.
     * @param fragmentMap a map that is to be transformed into a form encoded fragment
     * @return url fragment representing the form encoded {@code Map} passed as the argument
     */
    static UrlFragment of(Map<?, ?> fragmentMap) {
        def fragmentString = fragmentMap.collect {
            [it.key, it.value]*.toString().collect { urlFormParameterEscaper().escape(it) }.join("=")
        }.join("&")
        of(fragmentString)
    }

    String toString() {
        fragment
    }
}
