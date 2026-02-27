/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package grails.plugin.geb

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import geb.waiting.Wait

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode
import static org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat

/**
 * Handles parsing various recording configuration
 * used by {@link ContainerGebExtension}.
 *
 * @author James Daugherty
 * @since 4.1
 */
@Slf4j
@CompileStatic
class GebContainerSettings {

    public static final boolean DEFAULT_AT_CHECK_WAITING = false
    private static final VncRecordingMode DEFAULT_RECORDING_MODE = VncRecordingMode.SKIP
    private static final VncRecordingFormat DEFAULT_RECORDING_FORMAT = VncRecordingFormat.MP4
    public static final int DEFAULT_TIMEOUT_IMPLICITLY_WAIT = 0
    public static final int DEFAULT_TIMEOUT_PAGE_LOAD = 300
    public static final int DEFAULT_TIMEOUT_SCRIPT = 30

    boolean tracingEnabled
    String recordingDirectoryName
    String reportingDirectoryName
    String browserType
    boolean restartRecordingContainerPerTest
    VncRecordingMode recordingMode
    VncRecordingFormat recordingFormat
    LocalDateTime startTime
    int implicitlyWait
    int pageLoadTimeout
    int scriptTimeout

    boolean atCheckWaiting
    Number timeout
    Number retryInterval

    GebContainerSettings(LocalDateTime startTime) {
        tracingEnabled = getBooleanProperty('geb.container.tracing.enabled', false)
        recordingDirectoryName = System.getProperty('geb.container.recording.directory', 'build/gebContainer/recordings')
        reportingDirectoryName = System.getProperty('geb.container.reporting.directory', 'build/gebContainer/reports')
        // browserType = System.getProperty('geb.container.browser.type', DEFAULT_BROWSER_TYPE)
        // browserType = System.getProperty('geb.env', DEFAULT_BROWSER_TYPE)
        recordingMode = VncRecordingMode.valueOf(
                System.getProperty('geb.container.recording.mode', DEFAULT_RECORDING_MODE.name())
        )
        recordingFormat = VncRecordingFormat.valueOf(
                System.getProperty('geb.container.recording.format', DEFAULT_RECORDING_FORMAT.name())
        )
        restartRecordingContainerPerTest = getBooleanProperty(
                'geb.container.recording.restartRecordingContainerPerTest',
                true
        )
        implicitlyWait = getIntProperty('geb.container.timeouts.implicitlyWait', DEFAULT_TIMEOUT_IMPLICITLY_WAIT)
        pageLoadTimeout = getIntProperty('geb.container.timeouts.pageLoad', DEFAULT_TIMEOUT_PAGE_LOAD)
        scriptTimeout = getIntProperty('geb.container.timeouts.script', DEFAULT_TIMEOUT_SCRIPT)
        atCheckWaiting = getBooleanProperty('geb.container.atCheckWaiting.enabled', DEFAULT_AT_CHECK_WAITING)
        timeout = getNumberProperty('geb.container.timeouts.timeout', Wait.DEFAULT_TIMEOUT)
        retryInterval = getNumberProperty('geb.container.timeouts.retryInterval', Wait.DEFAULT_RETRY_INTERVAL)
        this.startTime = startTime
    }

    boolean isRecordingEnabled() {
        recordingMode != VncRecordingMode.SKIP
    }

    @Memoized
    File getRecordingDirectory() {
        if (!recordingEnabled) {
            return null
        }
        createDirectory(recordingDirectoryName, 'recording')
    }

    @Memoized
    File getReportingDirectory() {
        if (!reportingDirectoryName) {
            return null
        }
        createDirectory(reportingDirectoryName, 'reporting')
    }

    private static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        Boolean.parseBoolean(System.getProperty(propertyName, defaultValue.toString()))
    }

    private static int getIntProperty(String propertyName, int defaultValue) {
        System.getProperty(propertyName)?.toInteger() ?: defaultValue
    }

    private static Number getNumberProperty(String propertyName, Number defaultValue) {
        def propValue = System.getProperty(propertyName)
        if (propValue) {
            try {
                if (propValue.contains('.')) {
                    return new BigDecimal(propValue)
                }
                return Integer.parseInt(propValue)
            } catch (NumberFormatException ignored) {
                log.warn(
                        'Could not parse property [{}] with value [{}] as a Number. Using default value [{}] instead.',
                        propertyName,
                        propValue,
                        defaultValue
                )
            }
        }
        return defaultValue
    }

    private File createDirectory(String directoryName, String useCase) {
        def dir = new File(
                "$directoryName$File.separator${DateTimeFormatter.ofPattern('yyyyMMdd_HHmmss').format(startTime)}"
        )
        if (!dir.exists()) {
            if (!dir.parentFile.exists()) {
                log.info('Could not find [{}] directory for {}. Creating...', directoryName, useCase)
            }
            dir.mkdirs()
        } else if (!dir.isDirectory()) {
            throw new IllegalStateException(
                    "Configured $useCase directory [$dir] is expected to be a directory, but found file instead."
            )
        }

        return dir
    }
}
