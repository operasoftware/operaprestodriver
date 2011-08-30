package com.opera.core.systems;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class KeyEventsTest extends TestBase {

  private WebElement logEl;

  private boolean logContains(CharSequence s) {
    return logEl.getAttribute("value").contains(s);
  }

  @Before
  public void setUp() {
    getFixture("keys.html");
    logEl = driver.findElementById("log");
  }

  @After
  public void tearDown() {
    driver.releaseKeys();
  }

  @Test
  public void testKey() throws Exception {
    driver.key("a");

    Assert.assertTrue("keyDown event fired", logContains("down, 65, A"));
    Assert.assertTrue("keyPress event fired", logContains("press"));
    Assert.assertTrue("keyUp event fired", logContains("up, 65, A"));
  }

  @Test
  public void testKeyDown() throws Exception {
    driver.keyDown("a");

    Assert.assertTrue("keyDown event fired", logContains("down, 65, A"));
  }

  @Test
  public void testKeyUp() throws Exception {
    driver.keyDown("a");
    driver.keyUp("a");

    Assert.assertTrue("keyUp event fired", logContains("up, 65, A"));
  }

  @Test
  public void testMultipleKeys() throws Exception {
    driver.keyDown("control");
    driver.keyDown("shift");
    driver.keyUp("control");

    Assert.assertTrue("control down", logContains("down, 17"));
    Assert.assertTrue("shift down", logContains("down, 16"));
    Assert.assertTrue("control up", logContains("up, 17"));
    // shift should not have come up
    Assert.assertFalse("shift up", logContains("up, 16"));
  }

  @Test
  public void testSpecialKeys() throws Exception {
    driver.key("f5");
    Assert.assertTrue("F5", logContains("press, 116"));

    driver.key("home");
    Assert.assertTrue("Home", logContains("press, 36"));

    driver.key("pagedown");
    Assert.assertTrue("page down", logContains("press, 34"));

    driver.key("del");
    Assert.assertTrue("delete", logContains("press, 46"));

    driver.key("backspace");
    Assert.assertTrue("backspace", logContains("press, 8"));
  }

  @Test
  public void testAffectsOpera() throws Exception {
    // Fixture that doesn't preventDefault
    getFixture("test.html");
    ((OperaWebElement) driver.findElementById("input_email")).sendKeys("before refresh");

    driver.key("f5");
    //driver.waitForLoadToComplete();
    Assert.assertEquals("", driver.findElementById("input_email").getAttribute("value"));
  }

  @Test
  public void testReleaseKeys() throws Exception {
    driver.keyDown("control");
    driver.keyDown("shift");

    driver.releaseKeys();

    Assert.assertTrue("released", logContains("up, 16"));
    Assert.assertTrue("released", logContains("up, 17"));
  }

  @Test
  public void testReleaseAndPressKey() throws Exception {
    driver.keyDown("control");
    driver.releaseKeys();

    Assert.assertTrue("released", logContains("down, 17"));
    Assert.assertTrue("released", logContains("up, 17"));

    driver.keyDown("control");

    Assert.assertTrue("released", logEl.getAttribute("value").endsWith("down, 17, , ctrl\n"));
  }

  // Pressing enter will wait for a page to load, check what happens when
  // it doesn't
  @Test
  public void testEnter() throws Exception {
    driver.key("Enter");
    Assert.assertTrue("released", logContains("up, 13"));
  }
}
