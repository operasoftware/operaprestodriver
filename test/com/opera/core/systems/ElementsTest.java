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

import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class ElementsTest extends OperaDriverTestCase {

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
    assertNotSame(hash, test.getImageHash());
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