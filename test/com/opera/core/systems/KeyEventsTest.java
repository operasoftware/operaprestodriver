package com.opera.core.systems;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class KeyEventsTest extends TestBase {
  private WebElement logEl;

  private boolean logContains(CharSequence s) {
    return logEl.getValue().contains(s);
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
  public void testRepeatedKeys() throws Exception {
    driver.keyDown("a");
    Thread.sleep(1000);
    driver.keyUp("a");

    Assert.assertTrue("repeated press events",
        logContains("down, 65, A, \npress, 0, , \npress, 0, ,"));
  }

  @Test
  public void testMultipleKeys() throws Exception {
    driver.keyDown("control");
    driver.keyDown("shift");
    driver.keyUp("control");

    Assert.assertTrue("control down", logContains("down, 17"));
    Assert.assertTrue("shift down", logContains("down, 16"));
    Assert.assertTrue("control up", logContains("up, 17"));
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
  public void testReleaseKeys() throws Exception {
    driver.keyDown("control");
    driver.keyDown("shift");

    driver.releaseKeys();
    Assert.assertTrue("released", logContains("up, 16"));
    Assert.assertTrue("released", logContains("up, 17"));
  }
}
