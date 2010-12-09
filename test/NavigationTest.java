import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import com.opera.core.systems.settings.*;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;

public class NavigationTest extends TestCase
{
  private static OperaDriverSettings settings;
  private static OperaDriver driver;

  private static final String SERVER = "http://localhost/";

  public NavigationTest()
  {
    settings = new OperaDriverSettings();
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    settings.setOperaBinaryArguments("");

    driver = new OperaDriver(settings);
  }

  public void testBack()
  {
    int i;
    for (i = 0; i <= 100; i++) driver.get(SERVER+"?test="+i);

    for (i = 0; i <= 50; i++) driver.navigate().back();

    Assert.assertEquals(driver.getCurrentUrl(), SERVER+"?test=50");
  }

  public void testForward()
  {
    int i;
    for (i = 0; i <= 100; i++) driver.get(SERVER+"?test="+i);

    for (i = 0; i <= 50; i++) driver.navigate().back();
    for (i = 0; i <= 25; i++) driver.navigate().forward();

    Assert.assertEquals(driver.getCurrentUrl(), SERVER+"?test=75");
  }

  public void testShutDownOperaDriver()
  {
	  driver.shutdown();
  }
}
