package com.opera.core.systems;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavascriptTest extends TestBase {

  @Before
  public void setUp() {
    driver.get(fixture("javascript.html"));
  }

  @Test
  public void testTyping() {
    String text = "Hello, world!";

    driver.executeScript("document.getElementById('one').focus()");
    driver.type(text);

    assertEquals(text, driver.findElementById("one").getAttribute("value"));
  }

  // Make sure that typing actually happens. When the focus switches half way
  // through typing we should continue typing on the other textbox.
  @Test
  public void testTypingKeyEvents() {
    driver.get(fixture("keys.html"));

    driver.type("hi");

    String log = driver.findElementById("log").getAttribute("value");
    assertTrue(log.contains("press, 104, h,"));
    assertTrue(log.contains("up, 73, I,"));
  }

  @Test
  public void testDoubleClick() {
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();
    assertEquals(driver.findElementById("two").getAttribute("value"), "double");
  }

  @Test
  public void testWindowCount() throws Exception {
    int numWindows = driver.getWindowCount();

    driver.findElementById("open_window").click();

    assertEquals(numWindows + 1, driver.getWindowCount());
  }

  @Ignore
  @Test
  public void testWindowCount2() throws Exception {
    int numWindows = driver.getWindowCount();

    driver.close();

    assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
    assertEquals(numWindows - 1, driver.getWindowCount());
  }

}
