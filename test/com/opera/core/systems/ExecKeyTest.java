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
import com.opera.core.systems.scope.internal.OperaKey;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class ExecKeyTest extends OperaDriverTestCase {

  private WebElement logEl;

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.keys);
    logEl = driver.findElement(By.id("log"));
  }

  @After
  public void releaseModifiers() {
    keyUp(OperaKey.SHIFT);
    keyUp(OperaKey.CONTROL);
    keyUp(OperaKey.ALT);
  }

  @Test
  @Ignore(products = OperaProduct.MOBILE, value = "Needs investigation")
  public void testKey() {
    key("a");

    assertThat("keyDown event fired", log(), containsString("down, 65, A"));
    assertThat("keyUp event fired", log(), containsString("up, 65, A"));
  }

  @Test
  public void testKeyDown() {
    keyDown("a");

    assertThat("keyDown event fired", log(), containsString("down, 65, A"));
  }

  @Test
  public void testKeyUp() {
    keyDown("a");
    keyUp("a");

    assertThat("keyUp event fired", log(), containsString("up, 65, A"));
  }

  @Test
  public void testMultipleKeys() {
    keyDown(OperaKey.CONTROL);
    keyDown(OperaKey.SHIFT);
    keyUp(OperaKey.CONTROL);

    assertThat("control down", log(), containsString("down, 17"));
    assertThat("shift down", log(), containsString("down, 16"));
    assertThat("control up", log(), containsString("up, 17"));
    // shift should not have come up
    assertThat("shift up", log(), not(containsString(("up, 16"))));

    keyUp(OperaKey.SHIFT);
  }

  @Test
  @Ignore(products = OperaProduct.MOBILE)
  public void specialKeyF5() {
    key(OperaKey.F5);
    assertThat("F5 (down)", log(), containsString("down, 116"));
    assertThat("F5 (up)", log(), containsString("up, 116"));
  }

  @Test
  public void specialKeyHome() {
    key(OperaKey.HOME);
    assertThat("Home (down)", log(), containsString("down, 36"));
    assertThat("Home (up)", log(), containsString("up, 36"));
  }

  @Test
  public void specialKeyPageUp() {
    key(OperaKey.PAGE_UP);
    assertThat("page up (down)", log(), containsString("down, 33"));
    assertThat("page up (up)", log(), containsString("up, 33"));
  }

  @Test
  public void specialKeyPageDown() {
    key(OperaKey.PAGE_DOWN);
    assertThat("page down (down)", log(), containsString("down, 34"));
    assertThat("page down (up)", log(), containsString("up, 34"));
  }

  @Test
  public void specialKeyDelete() {
    key(OperaKey.DELETE);
    assertThat("delete (down)", log(), containsString("down, 46"));
    assertThat("delete (up)", log(), containsString("up, 46"));
  }

  @Test
  public void specialKeyBackspace() {
    key(OperaKey.BACK_SPACE);
    assertThat("backspace (down)", log(), containsString("down, 8"));
    assertThat("backspace (up)", log(), containsString("up, 8"));
  }

  @Test
  public void pressControl() {
    keyDown(OperaKey.CONTROL);
    assertThat(log(), containsString("down, 17, , ctrl"));
    keyUp(OperaKey.CONTROL);
    assertThat(log(), containsString("up, 17, ,"));
  }

  @Test
  public void pressShift() {
    keyDown(OperaKey.SHIFT);
    assertThat(log(), containsString("down, 16, , shift"));
    keyUp(OperaKey.SHIFT);
    assertThat(log(), containsString("up, 16, ,"));
  }

  // Pressing enter will wait for a page to load, check what happens when it doesn't
  @Test
  public void testEnter() {
    key(OperaKey.ENTER);
    assertThat("released", log(), containsString("up, 13"));
  }

  // Methods that access the exec service directly:

  public void key(OperaKey key) {
    if (key == OperaKey.ENTER) {
      driver.getServices().captureOperaIdle();
    }

    keyDown(key);
    keyUp(key);

    if (key == OperaKey.ENTER) {
      try {
        driver.waitForLoadToComplete();
      } catch (ResponseNotReceivedException e) {
        // These things happen
      }
    }
  }

  public void key(String key) {
    keyDown(key);
    keyUp(key);
  }

  public void keyDown(String key) {
    driver.getServices().getExec().key(key, false);
  }

  public void keyDown(OperaKey key) {
    driver.getServices().getExec().key(key.toScope(), false);
  }

  public void keyUp(String key) {
    driver.getServices().getExec().key(key, true);
  }

  public void keyUp(OperaKey key) {
    driver.getServices().getExec().key(key.toScope(), true);
  }

  // For verifying test outcome:

  private String log() {
    return logEl.getText();
  }

}