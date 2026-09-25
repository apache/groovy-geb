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
package geb.direct

import com.microsoft.playwright.Download
import java.nio.file.Path

class PlaywrightDownloads {
    final PlaywrightWebDriver driver
    PlaywrightDownloads(PlaywrightWebDriver driver) { this.driver = driver }
    Download expectDownload(Closure trigger) { driver.page.waitForDownload { trigger.call() } }
    Download waitForDownload(Closure trigger) { expectDownload(trigger) }
    void saveAs(Download download, Path path) { download.saveAs(path) }
    void saveAs(Download download, String path) { saveAs(download, Path.of(path)) }
}
