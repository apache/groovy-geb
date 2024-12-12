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
package geb.testng

import org.testng.TestNG
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.testng.xml.XmlClass
import org.testng.xml.XmlSuite
import org.testng.xml.XmlTest

import java.nio.file.Files

@Test
class GebReportingTestOnFailureTest extends AbstractGebReportingTestTest {

    protected static final String FAILURE_REPORT_LABEL = "failure"

    private File outputDir

    @BeforeMethod
    void before() {
        outputDir = Files.createTempDirectory(null).toFile()
    }

    @AfterMethod
    void after() {
        outputDir.deleteDir()
    }

    @Test
    void reportingTestShouldReportOnTestFailureOnlyIfThatStrategyIsEnabled() {
        runTestClass(InnerGebReportingTestOnFailureTest)

        def reportDir = new File(browser.config.reportsDir, InnerGebReportingTestOnFailureTest.name.replace('.', '/'))

        assert tryToFindReport("success", END_OF_METHOD_REPORT_LABEL, 1, 1, reportDir) == null

        doTestReport("failure", FAILURE_REPORT_LABEL, 2, 1, reportDir)
        assert tryToFindReport("failure", END_OF_METHOD_REPORT_LABEL, 2, 2, reportDir) == null
    }

    private void runTestClass(Class aClass) {
        def suite = new XmlSuite()
        def test = new XmlTest(suite)
        test.classes << new XmlClass(aClass)

        new TestNG(xmlSuites: [suite], outputDirectory: outputDir.absolutePath).run()
    }
}

@Test(groups = ["doNotRunFromGradle"])
class InnerGebReportingTestOnFailureTest extends AbstractGebReportingTestTest {

    @SuppressWarnings("ConstantAssertExpression")
    void success() {
        assert true
    }

    @Test(dependsOnMethods = ["success"])
    @SuppressWarnings("ConstantAssertExpression")
    void failure() {
        assert false
    }
}
