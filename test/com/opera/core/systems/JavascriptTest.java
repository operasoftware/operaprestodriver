package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class JavascriptTest extends TestBase
{

  @Before
  public void setUp()
  {
    driver.get(fixture("javascript.html"));
    // Click to focus the document
    driver.mouseEvent(1, 1, 1);
  }

  @Test
  public void testTyping()
  {
    String text = "Hello, world!";

    driver.executeScript("document.getElementById('one').focus()");
    driver.type(text);

    Assert.assertEquals(text, driver.findElementById("one").getAttribute("value"));
  }

  // Make sure that typing actually happens. When the focus switches half way
  // through typing we should continue typing on the other textbox
  @Test
  public void testTypingKeyEvents()
  {
    driver.get(fixture("keys.html"));

    driver.type("hi");

    String log = driver.findElementById("log").getAttribute("value");
    Assert.assertTrue(log.contains("press, 104, h,"));
    Assert.assertTrue(log.contains("up, 73, I,"));
  }

  @Test
  public void testDoubleClick()
  {
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();
    Assert.assertEquals(driver.findElementById("two").getAttribute("value"), "double");
  }

  @Test
  public void testWindowCount() throws Exception {
    int numWindows = driver.getWindowCount();

    driver.findElementById("open_window").click();

    Assert.assertEquals(numWindows + 1, driver.getWindowCount());
  }

  @Ignore
  @Test
  public void testWindowCount2() throws Exception {
    int numWindows = driver.getWindowCount();

    driver.close();

    Assert.assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
    Assert.assertEquals(numWindows - 1, driver.getWindowCount());
  }
}
