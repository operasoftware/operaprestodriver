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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FindElementsTest extends OperaDriverTestCase {

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.test);
  }

  @Test
  public void testLinkText() {
    WebElement el = driver.findElementByLinkText("accumsan ante");
    assertEquals(el.getAttribute("id"), "local");
  }

  public void testElementsLinkText() {
    List<WebElement> els = driver.findElementsByLinkText("accumsan ante");

    for (WebElement el : els) {
      assertEquals(el.getText(), "accumsan ante");
      assertEquals(el.getTagName().toLowerCase(), "a");
    }
  }

  // Partial link text
  @Test
  public void testPartialLinkText() {
    WebElement el = driver.findElementByPartialLinkText("pell");
    assertEquals(el.getAttribute("id"), "external");
  }

  @Test
  public void testElementsPartialLinkText() {
    List<WebElement> els = driver.findElementsByPartialLinkText("te");

    for (WebElement el : els) {
      assertTrue(el.getText().contains("te"));
      assertEquals(el.getTagName().toLowerCase(), "a");
    }
  }

  // ID
  @Test
  public void testId() {
    WebElement el = driver.findElementById("call-to-action");
    assertEquals(el.getAttribute("id"), "call-to-action");
    assertEquals(el.getTagName().toLowerCase(), "p");
  }

  @Test
  public void testElementsId() {
    List<WebElement> els = driver.findElementsById("external");

    for (WebElement el : els) {
      assertEquals(el.getAttribute("id"), "external");
    }
  }

  @Test
  public void testElementId() throws Exception {
    RemoteWebElement contain = (RemoteWebElement) driver.findElementById("content");
    WebElement el = contain.findElementById("local");
    assertEquals(el.getText(), "accumsan ante");

  }

  // XPath
  @Test
  public void testXPath() {
    WebElement el = driver.findElementByXPath("(//span)[4]");
    assertEquals(el.getText(), "ante");
  }

  public void testElementsXPath() {
    List<WebElement> els = driver.findElementsByXPath("//span");
    for (WebElement el : els) {
      assertEquals(el.getTagName().toLowerCase(), "span");
    }
  }

  // Class
  @Test
  public void testClass() {
    WebElement el = driver.findElementByClassName("invert");
    assertEquals(el.getText(), "Ante");
  }

  @Test
  public void testElementsClass() {
    List<WebElement> els = driver.findElementsByClassName("invert");

    for (WebElement el : els) {
      assertTrue(el.getAttribute("class").contains("invert"));
    }
  }

  // Name
  @Test
  public void testName() {
    WebElement el = driver.findElementByName("radios");
    assertEquals(el.getAttribute("id"), "radio_little");
  }

  @Test
  public void testElementsName() {
    List<WebElement> els = driver.findElementsByName("radios");

    assertEquals(3, els.size());
    for (WebElement el : els) {
      assertEquals(el.getAttribute("name"), "radios");
    }
  }

  // Tag name
  @Test
  public void testTagName() {
    WebElement el = driver.findElementByTagName("label");
    assertEquals(el.getAttribute("for"), "input_email");
  }

  @Test
  public void testElementsTagName() {
    List<WebElement> els = driver.findElementsByTagName("label");

    assertEquals(4, els.size());
    for (WebElement el : els) {
      assertEquals(el.getTagName(), "LABEL");
    }
  }

  // CSS selector
  @Test
  public void testCssSelector() {
    WebElement el = driver.findElementByCssSelector("p > span + a");
    assertEquals(el.getAttribute("id"), "local");
  }

  @Test
  public void testElementsCssSelector() {
    List<WebElement> els = driver.findElementsByCssSelector("div input[name=radios]");

    assertEquals(3, els.size());
    for (WebElement el : els) {
      assertEquals(el.getAttribute("name"), "radios");
    }
  }

  @Test
  public void testMultipleElements() throws Exception {
    OperaWebElement form = (OperaWebElement) driver.findElementByTagName("form");
    List<WebElement> els = form.findElements(By.tagName("input"));

    Assert.assertEquals(5, els.size());
    for (WebElement el : els) {
      Assert.assertEquals(el.getTagName(), "INPUT");
    }
  }

  // Special characters
  @Test
  public void testFindsElementsWithSingleQuotes() {
    WebElement
        el =
        driver.findElement(
            By.xpath(".//div[normalize-space()=concat('single ',\"'\",'quotes',\"'\",'')]"));
    Assert.assertNotNull(el);
  }

}