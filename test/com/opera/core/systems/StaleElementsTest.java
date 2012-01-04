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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public class StaleElementsTest extends OperaDriverTestCase {
  
  public WebElement div;
  
  @Before
  public void beforeEach() {
    driver.navigate().to("data:text/html;charset-utf8,<p>hoobaflooba</p>");
    div = (WebElement) driver.executeScript("return document.createElement('div')");
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetAttribute() throws Exception {
    div.getAttribute("foo");
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetText() throws Exception {
    div.getText();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testIsDisplayed() throws Exception {
    div.isDisplayed();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testIsEnabled() throws Exception {
    div.isEnabled();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testIsSelected() throws Exception {
    div.isSelected();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testClear() throws Exception {
    div.clear();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetLocation() throws Exception {
    div.getLocation();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testGetSize() throws Exception {
    div.getSize();
  }

  @Test(expected = StaleElementReferenceException.class)
  public void testTagName() throws Exception {
    div.getTagName();
  }
  
}