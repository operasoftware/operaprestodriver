import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;
import junit.*;

import com.opera.core.systems.*;
import com.opera.core.systems.settings.*;
import com.opera.core.systems.model.FilterRule;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

public class JavascriptTest extends TestCase
{
  private static OperaDriverSettings settings;
  private static OperaDriver driver;
  private static String base_dir;

  public void setUp()
  {
    if (driver != null)
    {
      driver.get(base_dir + "javascript.html");
      // Click to focus the document
      driver.mouseEvent(1, 1, 1);
    }
  }

  public void testInit()
  {
    settings = new OperaDriverSettings();
    settings.setRunOperaLauncherFromOperaDriver(true);
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    settings.setOperaBinaryArguments("");

    driver = new OperaDriver(settings);

    String separator = System.getProperty("file.separator");
    base_dir = System.getProperty("user.dir");
    base_dir = base_dir + separator + "test" + separator + "fixtures" + separator;
    File base_directory = new File(base_dir);

    Assert.assertTrue(base_directory.isDirectory());
  }

  public void testClear()
  {
    driver.findElementById("input_email").clear();
    Assert.assertTrue(driver.findElementById("log").getValue().contains("email changed"));
  }

  public void testRightClick()
  {
    ((OperaWebElement) driver.findElementById("local")).rightClick();
    Assert.assertTrue(driver.findElementById("log").getValue().contains("mousedown 2"));
    Assert.assertTrue(driver.findElementById("log").getValue().contains("mouseup 2"));
  }

  public void testHover()
  {
    OperaWebElement local = ((OperaWebElement) driver.findElementById("local"));
    String hash = local.getImageHash();
    local.hover();
    Assert.assertNotEquals(hash, local.getImageHash());
  }
}
