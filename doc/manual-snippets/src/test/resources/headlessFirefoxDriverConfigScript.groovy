import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions

driver = {
    def options = new FirefoxOptions()
    options.addArguments('--headless')
    new FirefoxDriver(options)
}
