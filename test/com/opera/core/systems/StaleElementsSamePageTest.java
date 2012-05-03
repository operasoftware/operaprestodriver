/*
Copyright 2012 Opera Software ASA

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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public class StaleElementsSamePageTest extends OperaDriverTestCase {

  public WebElement test;

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.test);
    test = driver.findElementById("header");
    driver.navigate().to(pages.test);
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetAttribute() {
    test.getAttribute("foo");
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetText() {
    test.getText();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testIsDisplayed() {
    test.isDisplayed();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testIsEnabled() {
    test.isEnabled();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testIsSelected() {
    test.isSelected();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testClear() {
    test.clear();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetLocation() {
    test.getLocation();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetSize() {
    test.getSize();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testTagName() {
    test.getTagName();
  }

}