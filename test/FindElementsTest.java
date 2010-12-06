import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import com.opera.core.systems.settings.*;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

public class FindElementsTest extends TestCase
{
  private static OperaDriverSettings settings;
  private static OperaDriver driver;

  public FindElementsTest()
  {
    settings = new OperaDriverSettings();
    settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
    settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
    // Could set -url here to navigate to the fixture page
    settings.setOperaBinaryArguments("");

    driver = new OperaDriver(settings);

    // Won't know if this works until OperaDriver works
    // Mad hacks to load the test.html file in the test directory
    driver.get(getClass().getProtectionDomain().getCodeSource().getLocation()+File.pathSeparator+"fixtures/test.html");
  }

  public void testActiveElement()
  {
    driver.executeScript("document.getElementById('local').focus()");
    WebElement el = driver.findActiveElement();
    Assert.assertEquals(el.getAttribute("id"), "local");
  }

  // Link text
  public void testLinkText()
  {
    WebElement el = driver.findElementByLinkText("accumsan ante");
    Assert.assertEquals(el.getAttribute("id"), "local");
  }

  public void testElementsLinkText()
  {
    List<WebElement> els = driver.findElementsByLinkText("accumsan ante");

    for (WebElement el : els)
    {
      Assert.assertEquals(el.getText(), "accumsan ante");
      Assert.assertEquals(el.getTagName().toLowerCase(), "a");
    }
  }

  // Partial link text
  public void testPartialLinkText()
  {
    WebElement el = driver.findElementByPartialLinkText("pell");
    Assert.assertEquals(el.getAttribute("id"), "external");
  }

  public void testElementsPartialLinkText()
  {
    List<WebElement> els = driver.findElementsByPartialLinkText("te");

    for (WebElement el : els)
    {
      Assert.assertTrue(el.getText().contains("te"));
      Assert.assertEquals(el.getTagName().toLowerCase(), "a");
    }
  }

  // ID
  public void testId()
  {
    WebElement el = driver.findElementById("call-to-action");
    Assert.assertEquals(el.getAttribute("id"), "call-to-action");
    Assert.assertEquals(el.getTagName().toLowerCase(), "p");
  }

  public void testElementsId()
  {
    List<WebElement> els = driver.findElementsById("external");

    for (WebElement el : els)
    {
      Assert.assertEquals(el.getAttribute("id"), "external");
    }
  }

  // XPath
  public void testXPath()
  {
    WebElement el = driver.findElementByXPath("//span[4]");
    Assert.assertEquals(el.getText(), "ante");
  }

  public void testElementsXPath()
  {
    List<WebElement> els = driver.findElementsByXPath("//span");
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getTagName().toLowerCase(), "span");
    }
  }

  // Class
  public void testClass()
  {
    WebElement el = driver.findElementByClassName("invert");
    Assert.assertEquals(el.getText(), "Ante");
  }

  public void testElementsClass()
  {
    List<WebElement> els = driver.findElementsByClassName("invert");

    for (WebElement el : els)
    {
      Assert.assertTrue(el.getAttribute("class").contains("invert"));
    }
  }

  // Name
  public void testName()
  {
    WebElement el = driver.findElementByName("radios");
    Assert.assertEquals(el.getAttribute("id"), "radio_little");
  }

  public void testElementsName()
  {
    List<WebElement> els = driver.findElementsByName("radios");

    Assert.assertEquals(els.size(), 3);
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getAttribute("name"), "radios");
    }
  }

  // Tag name
  public void testTagName()
  {
    WebElement el = driver.findElementByTagName("label");
    Assert.assertEquals(el.getAttribute("for"), "input_email");
  }

  public void testElementsTagName()
  {
    List<WebElement> els = driver.findElementsByTagName("label");

    Assert.assertEquals(els.size(), 4);
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getTagName(), "label");
    }
  }

  // CSS selector
  public void testCssSelector()
  {
    WebElement el = driver.findElementByCssSelector("p > span + a");
    Assert.assertEquals(el.getAttribute("id"), "local");
  }

  public void testElementsCssSelector()
  {
    List<WebElement> els = driver.findElementsByName("div input[name=radios]");

    Assert.assertEquals(els.size(), 3);
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getAttribute("name"), "radios");
    }
  }

}
