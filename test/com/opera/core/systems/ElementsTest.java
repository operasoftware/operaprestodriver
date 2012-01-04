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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.WebElement;

public class ElementsTest extends OperaDriverTestCase {

  @Test
  public void testClear() {
    driver.get(fixture("javascript.html"));
    driver.findElementById("input_email").clear();
    assertTrue(driver.findElementById("log").getAttribute("value").contains("email changed"));
  }

  /*
  // TODO(andreastt): Should be converted to an OperaMouse test
  @Test
  public void testMouseOver() {
    driver.get(fixture("mouse.html"));

    OperaWebElement test = ((OperaWebElement) driver.findElementById("test"));
    String hash = test.getImageHash();
    test.mouseOver();
    assertNotSame(hash, test.getImageHash());
  }
  */

  @Test
  public void testSelect() throws Exception {
    getFixture("select.html");
    WebElement el = driver.findElementById("dog");
    el.click();
    assertTrue(el.isSelected());
  }

  @Test
  public void testGetCssValue() {
    driver.navigate().to(fixture("test.html"));
    assertEquals("#000000",
                 driver.findElementByClassName("invert").getCssValue("background-color"));
  }

  /*
  TODO(andreastt): Convert it to an Actions or OperaMouse test

  // This test sometimes causes problems because a context menu is opened on Desktop, ensure that it
  // is last.
  @Test
  public void testRightClick() {
    driver.get(fixture("mouse.html"));

    // Cast as OperaWebElement to make rightClick available
    ((OperaWebElement) driver.findElementById("test")).rightClick();
    assertTrue(driver.findElementById("log").getAttribute("value").contains("mousedown 2"));
    assertTrue(driver.findElementById("log").getAttribute("value").contains("mouseup 2"));
  }
  */

}