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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

@RunWith(Enclosed.class)
public class StaleElementsTest extends OperaDriverTestCase {

  public static class DynamicallyAddedElement {

    public WebElement div;

    @Before
    public void beforeEach() {
      driver.navigate().to("data:text/html;charset-utf8,<p>hoobaflooba</p>");
      div = (WebElement) driver.executeScript("return document.createElement('div')");
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testGetAttribute() {
      div.getAttribute("foo");
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testGetText() {
      div.getText();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testIsDisplayed() {
      div.isDisplayed();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testIsEnabled() {
      div.isEnabled();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testIsSelected() {
      div.isSelected();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testClear() {
      div.clear();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testGetLocation() {
      div.getLocation();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testGetSize() {
      div.getSize();
    }

    @Test(expected = StaleElementReferenceException.class)
    public void testTagName() {
      div.getTagName();
    }

  }

  public static class NavigatingBetweenSamePage {

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

  public static class NavigatingBetweenDifferentPages {

    public WebElement test;

    @Before
    public void beforeEach() {
      driver.navigate().to(pages.test);
      test = driver.findElementById("header");
      driver.navigate().to(pages.twoInputFields);
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

}