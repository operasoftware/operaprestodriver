/**
 * Based on the example found at
 * http://code.google.com/p/selenium/wiki/RemoteWebDriver
 */
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;

class RemoteWebDriverExample {

  public static void main(String[] args) throws Exception {
    // Change this to match the location of your server
    URL server = new URL("http://127.0.0.1:4444/wd/hub");

    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setBrowserName("opera");

    System.out.println("Connecting to " + server);

    WebDriver driver = new RemoteWebDriver(server, capabilities);

    driver.get("http://www.opera.com");

    driver.quit();
  }

}