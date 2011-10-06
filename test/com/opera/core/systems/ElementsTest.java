package com.opera.core.systems;

import junit.framework.Assert;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

public class ElementsTest extends TestBase {

  @Test
  public void testClear() {
    driver.get(fixture("javascript.html"));
    driver.findElementById("input_email").clear();
    assertTrue(driver.findElementById("log").getAttribute("value").contains(
        "email changed"));
  }

  @Test
  public void testMouseOver() {
    driver.get(fixture("mouse.html"));

    OperaWebElement test = ((OperaWebElement) driver.findElementById("test"));
    String hash = test.getImageHash();
    test.mouseOver();
    Assert.assertNotSame(hash, test.getImageHash());
  }

  // This test sometimes causes problems. So put it last
  @Test
  public void testRightClick() {
    driver.get(fixture("mouse.html"));

    // Cast as OperaWebElement to make rightClick available
    ((OperaWebElement) driver.findElementById("test")).rightClick();
    assertTrue(driver.findElementById("log").getAttribute("value").contains(
        "mousedown 2"));
    assertTrue(driver.findElementById("log").getAttribute("value").contains(
        "mouseup 2"));
  }

  @Test
  public void testSelect() throws Exception {
    getFixture("select.html");
    WebElement el = driver.findElementById("dog");
    el.click();
    assertTrue(el.isSelected());
  }

}
