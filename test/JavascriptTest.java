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

public class JavascriptTest extends TestCase
{
  private static OperaDriverSettings settings;
  private static OperaDriver driver;

  public JavascriptTest()
  {
    settings = new OperaDriverSettings();
    settings.setRunOperaLauncherFromOperaDriver(true);
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    settings.setOperaBinaryArguments("");

    driver = new OperaDriver(settings);
  }

  private void loadFixture(String filename)
  {
    // Won't know if this works until OperaDriver works
    driver.get(getClass().getProtectionDomain().getCodeSource().getLocation()+
      File.pathSeparator+"fixtures/"+filename
    );
  }

  //@Before
  public void setup()
  {
    loadFixture("javascript.html");
  }

  public void testTyping()
  {
    String text = "Hello, world!";

    driver.executeScript("document.getElementById('one').focus()");
    driver.type(text);

    Assert.assertEquals(driver.findElementById("one").getValue(), text);
  }

  // Make sure that typing actually happens. When the focus switches half way
  // through typing we should continue typing on the other textbox
  public void testTypingReal()
  {
    // Changes text field focus after 200ms
    driver.executeScript("autofocus()");
    driver.type("abcdefghijklmnopqrstuvwxyz");

    Assert.assertTrue(driver.findElementById("one").getValue().startsWith("ab"));
    Assert.assertTrue(driver.findElementById("two").getValue().endsWith("yz"));
  }

  public void testTypingKeyEvents()
  {
    loadFixture("keys.html");
    driver.type("one");

    //String[] log = driver.findElementById("log").text.split("\n");
    //Assert.assertEquals(log.length, 9);

  }

  // TODO finish this test
  public void testConsoleListener()
  {
    driver.addConsoleListener(new IConsoleListener() {
      public List<FilterRule> getFilters() {
        List<FilterRule> list = new ArrayList<FilterRule>();
        list.add(new FilterRule("*", FilterRule.FilterType.STARTS_WITH, ""));
        return list;
      }

      public void onMessage(String message) {
        System.out.println("!!!"+message);
      }
    });
  }
}
