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

  private void loadFixture(String filename)
  {
    String path = getClass().getProtectionDomain().getCodeSource().getLocation()+
      "fixtures/"+filename;

    driver.get(path);
  }

  public void setUp()
  {
    if (driver != null)
    {
      loadFixture("javascript.html");
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
  }

  public void testTyping()
  {
    String text = "Hello, world!";

    driver.executeScript("document.getElementById('one').focus()");
    driver.type(text);
    try { Thread.sleep(1000); } catch (Exception e) {}
    Assert.assertEquals(text, driver.findElementById("one").getValue());
  }

  // Make sure that typing actually happens. When the focus switches half way
  // through typing we should continue typing on the other textbox
  public void testTypingKeyEvents()
  {
    loadFixture("keys.html");

    driver.type("hi");

    String log = driver.findElementById("log").getValue();
    Assert.assertTrue(log.contains("press, 104, h,"));
    Assert.assertTrue(log.contains("up, 73, I,"));
  }

  // TODO finish this test
  /*
oper  public void testConsoleListener()
  {
    driver.addConsoleListener(new IConsoleListener() {
      public List<FilterRule> getFilters() {
        List<FilterRule> list = new ArrayList<FilterRule>();
        list.add(new FilterRule("*", FilterRule.FilterType.STARTS_WITH, ""));
        return list;
      }

      public void onMessage(String message) {
        Assert.assertEquals("one", message);
      }
    });
  }
  */

  public void testDoubleClick()
  {
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();
    Assert.assertEquals(driver.findElementById("two").getValue(), "double");
  }

  public void testShutDownOperaDriver()
  {
	  driver.shutdown();
  }
}
