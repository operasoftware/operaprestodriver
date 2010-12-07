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
  private static String base_dir;

  public void testJavascriptTest()
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

  public void testTyping()
  {
	  driver.get(base_dir + "javascript.html");
	  String text = "Hello, world!";

	  driver.executeScript("document.getElementById('one').focus()");	 
	  driver.type(text);

	  Assert.assertEquals(text, driver.findElementById("one").getValue());
  }

  // Make sure that typing actually happens. When the focus switches half way
  // through typing we should continue typing on the other textbox
  public void testTypingReal()
  {
	  driver.get(base_dir + "javascript.html");
    
	  // Changes text field focus after 200ms
	  driver.executeScript("autofocus()");
	  driver.type("abcdefghijklmnopqrstuvwxyz");

	  Assert.assertTrue(driver.findElementById("one").getValue().startsWith("ab"));
  }
  
  public void testTypingReal2()
  {
	  Assert.assertTrue(driver.findElementById("two").getValue().endsWith("yz"));
  }  
  
  public void testTakeDown()
  {
	  driver.shutdown();
  }    
}
