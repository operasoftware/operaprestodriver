import com.opera.core.systems.OperaDriver;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ProxyExample {

  public static void main(String... args) {
    // Set your proxy settings:
    Proxy proxy = new Proxy();
    proxy.setHttpProxy("localhost:48000");

    // Put them into our requested capabilities:
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability("proxy", proxy);

    // This Opera instance should use a proxy:
    WebDriver driver = new OperaDriver(capabilities);

    driver.get("http://opera.com/");
    driver.quit();
  }

}