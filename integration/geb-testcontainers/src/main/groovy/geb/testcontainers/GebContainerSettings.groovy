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
package geb.testcontainers

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode
import static org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat

/**
 * Container-specific settings parsed from system properties.
 * <p>
 * Timeout and waiting configuration should be set in {@code GebConfig.groovy}
 * using standard Geb mechanisms.
 *
 * @author James Daugherty
 * @since 4.1
 */
@Slf4j
@CompileStatic
class GebContainerSettings {

    private static final VncRecordingMode DEFAULT_RECORDING_MODE = VncRecordingMode.SKIP
    private static final VncRecordingFormat DEFAULT_RECORDING_FORMAT = VncRecordingFormat.MP4

    boolean tracingEnabled
    String recordingDirectoryName
    String reportingDirectoryName
    boolean restartRecordingContainerPerTest
    VncRecordingMode recordingMode
    VncRecordingFormat recordingFormat
    LocalDateTime startTime

    GebContainerSettings(LocalDateTime startTime) {
        tracingEnabled = Boolean.parseBoolean(
            System.getProperty('geb.container.tracing.enabled', 'false')
        )
        recordingDirectoryName = System.getProperty(
            'geb.container.recording.directory', 'build/gebContainer/recordings'
        )
        reportingDirectoryName = System.getProperty(
            'geb.container.reporting.directory', 'build/gebContainer/reports'
        )
        recordingMode = VncRecordingMode.valueOf(
            System.getProperty('geb.container.recording.mode', DEFAULT_RECORDING_MODE.name())
        )
        recordingFormat = VncRecordingFormat.valueOf(
            System.getProperty('geb.container.recording.format', DEFAULT_RECORDING_FORMAT.name())
        )
        restartRecordingContainerPerTest = Boolean.parseBoolean(
            System.getProperty('geb.container.recording.restartRecordingContainerPerTest', 'true')
        )
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
