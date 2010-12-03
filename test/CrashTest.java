import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import com.opera.core.systems.settings.*;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;

public class CrashTest extends TestCase
{
  private static OperaDriverSettings settings;
  private static OperaDriver driver;

  public CrashTest()
  {
    settings = new OperaDriverSettings();
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    settings.setOperaBinaryArguments("");

    driver = new OperaDriver(settings);
  }

  public void testCore32291()
  {
    driver.get("http://t/core/bts/crashers/visual/CORE-32291/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  public void testCore32766()
  {
    driver.get("http://t/core/bts/crashers/visual/CORE-32766/002.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  public void testCore33582()
  {
    driver.get("http://t/core/bts/crashers/visual/CORE-33582/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  public void testCore34039()
  {
    driver.get("http://t/core/bts/crashers/visual/CORE-34039/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  public void testCore34284()
  {
    driver.get("http://t/core/bts/crashers/visual/CORE-34284/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  public void testCore32224()
  {
    driver.get("http://t/core/bts/crashers/visual/CORE-32224/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }
}
