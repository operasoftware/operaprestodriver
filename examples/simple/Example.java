import com.opera.core.systems.*;
import org.openqa.selenium.WebElement;

class Example {

  public static void main(String[] args) {
    /*
    Init the driver. This will automatically find your Opera browser and
    extract Opera launcher from the jar if it doesn't find an Opera launcher.

    If you want to override the Opera or launcher location you can either
    use these environment variables:

      OPERA_PATH - absolute path to your Opera binary
      OPERA_LAUNCHER - absolute path to the Opera launcher binary

    or create a DesiredCapabilities object:

      import org.openqa.selenium.remote.DesiredCapabilities;
      ...
      DesiredCapabilities capabilities = new DesiredCapabilities();
      capabilities.setCapability(OperaDriver.BINARY, ...);
      capabilities.setCapability(OperaDriver.LAUNCHER, ...);

      OperaDriver driver = new OperaDriver(capabilities);

    Have a look at the documentation for more settings.
    */
    OperaDriver driver = new OperaDriver();

    /*
    OperaDriver supports all of the WebDriver methods. This one navigates to
    the given webpage
    */
    driver.get("http://www.opera.com/");
    System.out.println("Current url: "+ driver.getCurrentUrl());

    /*
    You can access elements and their properties through a variety of methods
    */
    WebElement el = driver.findElementById("footer-promo");
    System.out.println(el.getText());

    /*
    When finished, shutdown Opera
    */
    driver.quit();
  }

}
