/*
Copyright 2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyEventsTest extends OperaDriverTestCase {

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

    assertTrue("keyDown event fired", logContains("down, 65, A"));
    assertTrue("keyPress event fired", logContains("press"));
    assertTrue("keyUp event fired", logContains("up, 65, A"));
  }

  @Test
  public void testKeyDown() throws Exception {
    driver.keyDown("a");

    assertTrue("keyDown event fired", logContains("down, 65, A"));
  }

  @Test
  public void testKeyUp() throws Exception {
    driver.keyDown("a");
    driver.keyUp("a");

    assertTrue("keyUp event fired", logContains("up, 65, A"));
  }

  @Test
  public void testMultipleKeys() throws Exception {
    driver.keyDown("control");
    driver.keyDown("shift");
    driver.keyUp("control");

    assertTrue("control down", logContains("down, 17"));
    assertTrue("shift down", logContains("down, 16"));
    assertTrue("control up", logContains("up, 17"));
    // shift should not have come up
    assertFalse("shift up", logContains("up, 16"));
  }

  @Test
  public void testSpecialKeys() throws Exception {
    driver.key("f5");
    assertTrue("F5", logContains("press, 116"));

    driver.key("home");
    assertTrue("Home", logContains("press, 36"));

    driver.key("pagedown");
    assertTrue("page down", logContains("press, 34"));

    driver.key("del");
    assertTrue("delete", logContains("press, 46"));

    driver.key("backspace");
    assertTrue("backspace", logContains("press, 8"));
  }

  @Test
  public void testAffectsOpera() throws Exception {
    // Fixture that doesn't preventDefault
    getFixture("test.html");
    ((OperaWebElement) driver.findElementById("input_email")).sendKeys("before refresh");

    driver.key("f5");
    //driver.waitForLoadToComplete();
    assertEquals("", driver.findElementById("input_email").getAttribute("value"));
  }

  @Test
  public void testReleaseKeys() throws Exception {
    driver.keyDown("control");
    driver.keyDown("shift");

    driver.releaseKeys();

    assertTrue("released", logContains("up, 16"));
    assertTrue("released", logContains("up, 17"));
  }

  @Test
  public void testReleaseAndPressKey() throws Exception {
    driver.keyDown("control");
    driver.releaseKeys();

    assertTrue("released", logContains("down, 17"));
    assertTrue("released", logContains("up, 17"));

    driver.keyDown("control");

    assertTrue("released", logEl.getAttribute("value").endsWith("down, 17, , ctrl\n"));
  }

  // Pressing enter will wait for a page to load, check what happens when it doesn't
  @Test
  public void testEnter() throws Exception {
    driver.key("Enter");
    assertTrue("released", logContains("up, 13"));
  }

}