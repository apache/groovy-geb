package geb.frame

import org.openqa.selenium.WebElement
import org.openqa.selenium.NoSuchFrameException
import geb.Browser
import geb.navigator.Navigator
import geb.content.SimplePageContent

class FrameSupport {

    Browser browser

    FrameSupport(Browser browser) {
        this.browser = browser
    }

    void withFrame(def frame, Closure block) {
        browser.driver.switchTo().frame(frame)
        try {
            block.call()
        } finally {
            browser.driver.switchTo().defaultContent()
        }
    }

    private withFrameForContent(content, Closure block) {
        WebElement element = content.firstElement()
        if (element == null) {
            throw new NoSuchFrameException("No elements for given content: $content")
        }
        withFrame(element, block)
    }

    void withFrame(Navigator frame, Closure block) {
        withFrameForContent(frame, block)
    }

    void withFrame(SimplePageContent frame, Closure block) {
        withFrameForContent(frame, block)
    }
}
