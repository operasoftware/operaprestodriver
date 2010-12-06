import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import com.opera.core.systems.settings.*;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;

public class ScreenshotTest extends TestCase
{
  private static OperaDriverSettings settings;
  private static OperaDriver driver;

  private static final String IMG_FILE = "/tmp/screenshottest.png";

  public ScreenshotTest()
  {
    settings = new OperaDriverSettings();
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    settings.setOperaBinaryArguments("");

    driver = new OperaDriver(settings);
  }

  public void testScreenshotWrites()
  {
    driver.get("http://localhost/");
    driver.saveScreenshot(IMG_FILE, 0);

    File f = new File(IMG_FILE);
    Assert.assertTrue(f.exists());
  }

}
