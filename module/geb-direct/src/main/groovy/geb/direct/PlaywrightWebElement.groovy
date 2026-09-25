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

import com.microsoft.playwright.*
import org.openqa.selenium.*

import java.nio.file.Path
import java.nio.file.Paths

@SuppressWarnings([
    'UnnecessarySetter',
    'DuplicateStringLiteral',
    'PublicMethodsBeforeNonPublicMethods',
    'StaticMethodsBeforeInstanceMethods',
    'StaticFieldsBeforeInstanceFields'
])
class PlaywrightWebElement implements WebElement {
    final PlaywrightWebDriver driver
    final Locator locator

    PlaywrightWebElement(PlaywrightWebDriver driver, Locator locator) { this.driver = driver; this.locator = locator }
    ElementHandle handle() { locator.elementHandle() }
    void click() {
        if (tagName == 'option') {
            locator.evaluate('''element => {
                element.selected = true
                const select = element.parentElement
                if (select?.tagName === 'SELECT') {
                    select.dispatchEvent(new Event('input', { bubbles: true }))
                    select.dispatchEvent(new Event('change', { bubbles: true }))
                }
            }''')
            return
        }
        locator.click()
    }
    void hover() { locator.hover() }
    void doubleClick() { locator.dblclick() }
    void check() { locator.check() }
    void uncheck() { locator.uncheck() }
    List<String> selectOption(String value) { locator.selectOption(value) as List<String> }
    void press(String key) { locator.press(key) }
    void focus() { locator.focus() }
    void scrollIntoViewIfNeeded() { locator.scrollIntoViewIfNeeded() }
    void dragTo(WebElement target) { locator.dragTo(((PlaywrightWebElement) target).locator) }
    void setInputFiles(String... paths) { locator.setInputFiles(paths.collect { java.nio.file.Path.of(it) } as java.nio.file.Path[]) }
    void submit() { locator.evaluate('(element) => element.closest("form")?.requestSubmit()') }
    void sendKeys(CharSequence... keysToSend) {
        if (locator.getAttribute('type') == 'file') {
            locator.setInputFiles(keysToSend.collect { Paths.get(it.toString()) } as Path[])
            return
        }
        def keyboard = driver.page.keyboard()
        locator.focus()
        StringBuilder text = new StringBuilder()
        LinkedHashSet<String> heldModifiers = [] as LinkedHashSet
        keysToSend.each { CharSequence sequence ->
            if (sequence instanceof Keys) {
                flushText(text, keyboard)
                applyKey(keyboard, (Keys) sequence, heldModifiers)
            } else {
                String value = sequence?.toString() ?: ''
                for (int i = 0; i < value.length(); i++) {
                    char ch = value.charAt(i)
                    Keys special = Keys.getKeyFromUnicode(ch)
                    if (special != null) {
                        flushText(text, keyboard)
                        applyKey(keyboard, special, heldModifiers)
                    } else {
                        text.append(ch)
                    }
                }
            }
        }
        flushText(text, keyboard)
        heldModifiers.toList().reverseEach { keyboard.up(it) }
    }

    private void flushText(StringBuilder text, def keyboard) {
        if (text.length() > 0) {
            keyboard.type(text.toString())
            text.setLength(0)
        }
    }

    private static void applyKey(def keyboard, Keys key, Set<String> heldModifiers) {
        if (key == Keys.NULL) {
            heldModifiers.toList().reverseEach { keyboard.up(it) }
            heldModifiers.clear()
            return
        }
        String name = playwrightKey(key)
        if (isModifier(key)) {
            if (!heldModifiers.contains(name)) {
                keyboard.down(name)
                heldModifiers.add(name)
            }
            return
        }
        keyboard.press(name)
    }

    private static boolean isModifier(Keys key) {
        key in [
            Keys.SHIFT, Keys.LEFT_SHIFT, Keys.RIGHT_SHIFT,
            Keys.CONTROL, Keys.LEFT_CONTROL, Keys.RIGHT_CONTROL,
            Keys.ALT, Keys.LEFT_ALT, Keys.RIGHT_ALT,
            Keys.META, Keys.COMMAND, Keys.RIGHT_COMMAND, Keys.OPTION
        ]
    }

    static String playwrightKey(Keys key) {
        switch (key) {
            case Keys.ENTER:
            case Keys.RETURN: return 'Enter'
            case Keys.TAB: return 'Tab'
            case Keys.ESCAPE: return 'Escape'
            case Keys.BACK_SPACE: return 'Backspace'
            case Keys.DELETE: return 'Delete'
            case Keys.SPACE: return ' '
            case Keys.ARROW_UP:
            case Keys.UP: return 'ArrowUp'
            case Keys.ARROW_DOWN:
            case Keys.DOWN: return 'ArrowDown'
            case Keys.ARROW_LEFT:
            case Keys.LEFT: return 'ArrowLeft'
            case Keys.ARROW_RIGHT:
            case Keys.RIGHT: return 'ArrowRight'
            case Keys.HOME: return 'Home'
            case Keys.END: return 'End'
            case Keys.PAGE_UP: return 'PageUp'
            case Keys.PAGE_DOWN: return 'PageDown'
            case Keys.SHIFT:
            case Keys.LEFT_SHIFT:
            case Keys.RIGHT_SHIFT: return 'Shift'
            case Keys.CONTROL:
            case Keys.LEFT_CONTROL:
            case Keys.RIGHT_CONTROL: return 'Control'
            case Keys.ALT:
            case Keys.LEFT_ALT:
            case Keys.RIGHT_ALT:
            case Keys.OPTION: return 'Alt'
            case Keys.META:
            case Keys.COMMAND:
            case Keys.RIGHT_COMMAND: return 'Meta'
            case Keys.NULL: return 'Null'
            default: return key.toString()
        }
    }

    void clear() { locator.clear() }
    String getTagName() { locator.evaluate('(element) => element.tagName.toLowerCase()') as String }
    private static final Set<String> BOOLEAN_ATTRIBUTES = [
        'async', 'autofocus', 'autoplay', 'checked', 'compact', 'complete', 'controls', 'declare',
        'defaultchecked', 'defaultselected', 'defer', 'disabled', 'draggable', 'ended', 'formnovalidate',
        'hidden', 'indeterminate', 'iscontenteditable', 'ismap', 'itemscope', 'loop', 'multiple', 'muted',
        'nohref', 'noresize', 'noshade', 'novalidate', 'nowrap', 'open', 'paused', 'pubdate', 'readonly',
        'required', 'reversed', 'scoped', 'seamless', 'seeking', 'selected', 'spellcheck', 'truespeed',
        'willvalidate'
    ] as Set

    String getAttribute(String name) {
        String encoded = "'${name.replace('\\', '\\\\').replace("'", "\\'")}'"
        if (BOOLEAN_ATTRIBUTES.contains(name?.toLowerCase(Locale.ROOT))) {
            // Selenium boolean attribute contract: "true" when present, null when absent.
            return locator.evaluate("""element => {
                const attr = element.getAttribute(${encoded});
                if (attr === null) { return null; }
                return 'true';
            }""") as String
        }
        locator.evaluate("""element => {
            const value = element[${encoded}];
            if (value == null || value === false) {
                return element.getAttribute(${encoded});
            }
            return String(value);
        }""") as String
    }
    String getDomAttribute(String name) { locator.getAttribute(name) }
    String getDomProperty(String name) {
        String encoded = "'${name.replace('\\', '\\\\').replace("'", "\\'")}'"
        locator.evaluate("element => { const value = element[${encoded}]; return value == null ? null : String(value) }") as String
    }
    boolean isSelected() {
        String tag = tagName
        if (tag == 'option') {
            return locator.evaluate('element => element.selected') as boolean
        }
        locator.isChecked()
    }
    boolean isEnabled() { locator.isEnabled() }
    String getText() { locator.innerText() }
    String getInnerHTML() { locator.innerHTML() }
    String getInputValue() { locator.inputValue() }
    def getBoundingBox() { locator.boundingBox() }
    int getCount() { locator.count() }
    boolean isHidden() { locator.isHidden() }
    boolean isEditable() { locator.isEditable() }
    boolean isChecked() { locator.isChecked() }
    List<WebElement> findElements(By by) { driver.findElements(locator.locator(PlaywrightBy.selector(by))) }
    WebElement findElement(By by) {
        def elements = findElements(by)
        if (elements.empty) {
            throw new NoSuchElementException("Unable to locate element: $by")
        }
        elements.first()
    }
    boolean isDisplayed() { locator.isVisible() }
    Point getLocation() { def box = locator.boundingBox(); new Point((box?.x ?: 0) as int, (box?.y ?: 0) as int) }
    Dimension getSize() { def box = locator.boundingBox(); new Dimension((box?.width ?: 0) as int, (box?.height ?: 0) as int) }
    Rectangle getRect() { new Rectangle(getLocation(), getSize()) }
    String getCssValue(String propertyName) {
        String escapedPropertyName = propertyName.replace('\\', '\\\\').replace('"', '\\"')
        locator.evaluate("(element) => getComputedStyle(element).getPropertyValue(\"${escapedPropertyName}\")") as String
    }
    <X> X getScreenshotAs(OutputType<X> target) { target.convertFromPngBytes(locator.screenshot()) }
}
