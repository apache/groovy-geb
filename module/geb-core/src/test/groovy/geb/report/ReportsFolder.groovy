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
package geb.report

import geb.test.CloseableTempDirectory
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation

class ReportsFolder implements IMethodInterceptor {

    private final String groupName

    private File directory

    ReportsFolder(String groupName) {
        this.groupName = groupName
    }

    File getGroupDir() {
        new File(directory, groupName)
    }

    Set<String> getReportFileNames() {
        groupDir.listFiles()*.name
    }

    @Override
    @SuppressWarnings("EmptyTryBlock")
    void intercept(IMethodInvocation invocation) throws Throwable {
        try(def tempDirectory = new CloseableTempDirectory()) {
            directory = tempDirectory.file
            invocation.browser.config.reportsDir = directory
            invocation.proceed()
        } finally {
            directory = null
        }
    }
}
