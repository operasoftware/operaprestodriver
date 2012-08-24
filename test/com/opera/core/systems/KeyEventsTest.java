/*
Copyright 2011-2012 Opera Software ASA

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

import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyEventsTest extends OperaDriverTestCase {

  private WebElement logEl;

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.keys);
    logEl = driver.findElement(By.id("log"));
  }

  @After
  public void afterEach() {
    releaseKeys();
  }

  @Test
  @Ignore(products = OperaProduct.MOBILE, value = "Needs investigation")
  public void testKey() {
    key("a");

    assertTrue("keyDown event fired", logContains("down, 65, A"));
    assertTrue("keyPress event fired", logContains("press"));
    assertTrue("keyUp event fired", logContains("up, 65, A"));
  }

  @Test
  public void testKeyDown() {
    keyDown("a");

    assertTrue("keyDown event fired", logContains("down, 65, A"));
  }

  @Test
  public void testKeyUp() {
    keyDown("a");
    keyUp("a");

    assertTrue("keyUp event fired", logContains("up, 65, A"));
  }

  @Test
  public void testMultipleKeys() {
    keyDown("control");
    keyDown("shift");
    keyUp("control");

    assertTrue("control down", logContains("down, 17"));
    assertTrue("shift down", logContains("down, 16"));
    assertTrue("control up", logContains("up, 17"));
    // shift should not have come up
    assertFalse("shift up", logContains("up, 16"));
  }

  @Test
  @Ignore(products = OperaProduct.MOBILE, value = "Needs investigation")
  public void testSpecialKeys() {
    key("f5");
    assertTrue("F5", logContains("press, 116"));

    key("home");
    assertTrue("Home", logContains("press, 36"));

    key("pagedown");
    assertTrue("page down", logContains("press, 34"));

    key("del");
    assertTrue("delete", logContains("press, 46"));

    key("backspace");
    assertTrue("backspace", logContains("press, 8"));
  }

  @Test
  public void testReleaseKeys() {
    keyDown("control");
    keyDown("shift");

    releaseKeys();

    assertTrue("released", logContains("up, 16"));
    assertTrue("released", logContains("up, 17"));
  }

  @Test
  public void testReleaseAndPressKey() {
    keyDown("control");
    releaseKeys();

    assertTrue("released", logContains("down, 17"));
    assertTrue("released", logContains("up, 17"));

    keyDown("control");

    assertTrue("released", logEl.getText().endsWith("down, 17, , ctrl"));
  }

  // Pressing enter will wait for a page to load, check what happens when it doesn't
  @Test
  public void testEnter() {
    key("Enter");
    assertTrue("released", logContains("up, 13"));
  }

  // Methods that access the exec service directly:

  private void key(String key) {
    if (key.equalsIgnoreCase("enter")) {
      driver.getServices().captureOperaIdle();
    }

    keyDown(key);
    keyUp(key);

    if (key.equalsIgnoreCase("enter")) {
      try {
        driver.waitForLoadToComplete();
      } catch (ResponseNotReceivedException e) {
        // These things happen
      }
    }
  }

  private void keyDown(String key) {
    driver.getServices().getExec().key(key, false);
  }

  public void keyUp(String key) {
    driver.getServices().getExec().key(key, true);
  }

  public void releaseKeys() {
    driver.getServices().getExec().releaseKeys();
  }

  // For verifying test outcome:

  private boolean logContains(CharSequence s) {
    return logEl.getText().contains(s);
  }

}