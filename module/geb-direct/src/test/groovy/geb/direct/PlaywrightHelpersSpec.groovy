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

import com.sun.net.httpserver.HttpServer
import geb.direct.support.PlaywrightSpecSupport
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.Point
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ PlaywrightSpecSupport.skipped() })
class PlaywrightHelpersSpec extends Specification {
    PlaywrightWebDriver driver

    def cleanup() { driver?.quit() }

    def 'input, navigation, window, and storage helpers drive the active browser'() {
        given:
        HttpServer server = PlaywrightSpecSupport.server { String path, headers ->
            "<title>${path}</title><input id=\"input\"><div id=\"target\" style=\"width:50px;height:50px\"></div>"
        }
        driver = PlaywrightSpecSupport.driver()
        driver.navigate().to(PlaywrightSpecSupport.url(server))

        when:
        driver.findElement(By.id('input')).click()
        driver.keyboard.type('a')
        driver.keyboard.press('End')
        driver.keyboard.down('Shift')
        driver.keyboard.up('Shift')
        driver.keyboard.insertText('b')
        driver.mouse.move(10, 10)
        driver.mouse.click(10, 10)
        driver.mouse.dblclick(10, 10)
        driver.mouse.down()
        driver.mouse.up()
        driver.mouse.wheel(0, 10)
        driver.navigate().to(new URL(PlaywrightSpecSupport.url(server, '/next')))
        driver.navigate().back()
        driver.navigate().forward()
        driver.navigate().refresh()
        driver.manage().window().size = new Dimension(640, 480)
        driver.manage().window().position = new Point(3, 4)
        driver.manage().window().maximize()
        driver.manage().window().fullscreen()
        driver.manage().window().minimize()
        driver.storage.local('key', 'value')
        driver.storage.clearLocal()
        driver.storage.session('key', 'value')
        driver.storage.clearSession()
        driver.wait.waitForLoadState('load')
        driver.wait.waitForSelector('#input')
        driver.wait.waitForFunction('() => document.readyState === "complete"')
        driver.wait.waitForTimeout(1)
        driver.emulation.setViewportSize(800, 600)
        driver.emulation.setGeolocation(1, 2, 3d)
        driver.emulation.offline = false
        driver.emulation.extraHTTPHeaders = ['X-Direct': 'value']
        driver.emulation.emulateMedia('screen', 'dark', 'reduce')
        driver.emulation.grantPermissions(['geolocation'], PlaywrightSpecSupport.url(server))
        driver.emulation.clearPermissions()

        then:
        driver.findElement(By.id('input')).displayed
        driver.manage().window().position == new Point(3, 4)
        driver.page.viewportSize().width == 800

        cleanup:
        server.stop(0)
    }
}
