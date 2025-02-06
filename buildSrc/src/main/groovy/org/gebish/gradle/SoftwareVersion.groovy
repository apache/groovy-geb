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
package org.gebish.gradle

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

@CompileStatic
class SoftwareVersion implements Comparable<SoftwareVersion> {
    private static final String DOT = '.'
    Integer major
    Integer minor
    Integer patch

    static SoftwareVersion of(String version) {
        def parts = version.split('\\.')
        Integer major = parts[0] as Integer
        Integer minor = parts.length > 1 ? parts[1] as Integer : null
        Integer patch = parts.length > 2 ? parts[2] as Integer : null
        return new SoftwareVersion(major: major, minor: minor, patch: patch)
    }

    String toString() {
        List<Integer> parts = []
        if (major != null) {
            parts.add(major)
        }
        if (minor != null) {
            parts.add(minor)
        }
        if (patch != null) {
            parts.add(patch)
        }
        parts.join(DOT)
    }

    @Override
    int compareTo(@NotNull SoftwareVersion o) {
        if (this.major != o.major) {
            return compareNullable(this.major, o.major)
        }
        if (this.minor != o.minor) {
            return compareNullable(this.minor, o.minor)
        }
        return compareNullable(this.patch, o.patch)
    }

    private static int compareNullable(Integer a, Integer b) {
        if (a == b) return 0
        if (a == null) return -1
        if (b == null) return 1
        return a <=> b
    }
}
