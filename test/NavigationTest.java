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
  private static String base_dir;

  public void testNavigationTest()
  {
    settings = new OperaDriverSettings();
    settings.setRunOperaLauncherFromOperaDriver(true);
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    settings.setOperaBinaryArguments("-geometry 1024x768 -url opera:blank");

    driver = new OperaDriver(settings);
    
    String separator = System.getProperty("file.separator");
    base_dir = System.getProperty("user.dir");
    base_dir = base_dir + separator + "test" + separator + "fixtures" + separator;    
    File base_directory = new File(base_dir);
    
    Assert.assertTrue(base_directory.isDirectory());
  }

  public void testBack()
  {
	  driver.get(base_dir + "javascript.html");
	  driver.get(base_dir + "test.html");
	  driver.get(base_dir + "keys.html");
	  driver.navigate().back();
	  Assert.assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  public void testForward()
  {
    driver.navigate().forward();
    Assert.assertTrue(driver.getCurrentUrl().indexOf("keys.html") > 0);
  }
  
  public void testBack2()
  {
	  driver.navigate().back();
	  driver.navigate().back();
	  Assert.assertTrue(driver.getCurrentUrl().indexOf("javascript.html") > 0);
  }  
  
  public void testForward2()
  {
    driver.navigate().forward();
    driver.navigate().forward();
    Assert.assertTrue(driver.getCurrentUrl().indexOf("keys.html") > 0);
  }  
  
  public void testTakeDown()
  {
	  driver.shutdown();
  }  
}
