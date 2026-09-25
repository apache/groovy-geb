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

import org.openqa.selenium.*

@SuppressWarnings('UnnecessarySetter')
class PlaywrightWindow implements WebDriver.Window {
    private static final Dimension FULLSCREEN_SIZE = new Dimension(1920, 1080)
    final PlaywrightWebDriver driver
    private Point logicalPosition = new Point(0, 0)

    PlaywrightWindow(PlaywrightWebDriver driver) { this.driver = driver }
    Dimension getSize() { def size = driver.page.viewportSize(); new Dimension(size.width, size.height) }
    void setSize(Dimension targetSize) { driver.page.setViewportSize(targetSize.width, targetSize.height) }
    // Playwright cannot move the native browser window, so preserve a logical position for WebDriver callers.
    Point getPosition() { new Point(logicalPosition.x, logicalPosition.y) }
    void setPosition(Point targetPosition) { logicalPosition = new Point(targetPosition.x, targetPosition.y) }
    // Best-effort: Playwright has no native maximize/minimize chrome control.
    void maximize() { setSize(FULLSCREEN_SIZE) }

    @SuppressWarnings('EmptyMethod')
    void minimize() {
        // Intentionally no-op: Playwright cannot minimize OS browser chrome.
    }

    void fullscreen() { setSize(FULLSCREEN_SIZE) }
}
