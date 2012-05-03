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

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElementsTest extends OperaDriverTestCase {

  @Test
  public void testClear() {
    driver.get(pages.javascript);
    driver.findElementById("input_email").clear();
    assertTrue(driver.findElementById("log").getAttribute("value").contains("email changed"));
  }

  @Test
  public void testSelect() {
    driver.get(pages.select);
    WebElement el = driver.findElementById("dog");
    el.click();
    assertTrue(el.isSelected());
  }

  @Test
  public void testGetCssValue() {
    driver.get(pages.test);
    assertEquals("#000000",
                 driver.findElementByClassName("invert").getCssValue("background-color"));
  }

  @Test
  public void testGetCssValueWithNonRGBValue() {
    driver.get(pages.test);
    assertEquals("transparent",
                 driver.findElementById("transparent").getCssValue("background-color"));
  }

}