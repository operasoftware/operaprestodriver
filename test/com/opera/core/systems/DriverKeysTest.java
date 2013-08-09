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

import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Keyboard;

import static org.junit.Assert.assertEquals;
import static com.opera.core.systems.OperaProduct.MOBILE;

public class DriverKeysTest extends OperaDriverTestCase {

  public Keyboard keyboard;
  public WebElement fieldOne;
  public WebElement fieldTwo;

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.twoInputFields);

    keyboard = driver.getKeyboard();
    fieldOne = driver.findElement(By.name("one"));
    fieldTwo = driver.findElement(By.name("two"));
    fieldOne.click();
  }

  @Test
  public void testSingleCharacter() {
    keyboard.sendKeys("a");
    assertEquals("a", fieldOne.getAttribute("value"));
  }

  @Test
  public void testSequence() {
    new Actions(driver)
        .sendKeys("a")
        .sendKeys("b")
        .sendKeys("c")
        .sendKeys("d")
        .sendKeys("e")
        .sendKeys("f")
        .build().perform();
    assertEquals("abcdef", fieldOne.getAttribute("value"));
  }

  @Test
  public void testMultiByte() {
    // Note that this test will fail if you have the wrong charset setup on Windows.
    new Actions(driver).sendKeys("ル")
        .sendKeys("ビ")
        .sendKeys("ー")
        .sendKeys(" ")
        .sendKeys("水")
        .build().perform();
    assertEquals("Are you sure you have the right charset setup if you're on Windows?",
                 "ルビー 水", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeSingle() {
    keyboard.sendKeys("a");
    assertEquals("a", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeMulti() {
    keyboard.sendKeys("abcdef");
    assertEquals("abcdef", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeMultiByte() {
    new Actions(driver).sendKeys("ルビー 水").build().perform();
    assertEquals("ルビー 水", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeNotModify() throws Exception {
    keyboard.sendKeys("ac" + "left" + "b");
    assertEquals("acleftb", fieldOne.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testLeftArrow() {
    keyboard.sendKeys("ac" + Keys.LEFT + "b");
    assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testCaseInsensitiveLeftArrow() {
    keyboard.sendKeys("ac" + Keys.LEFT + "b");
    assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTab() {
    keyboard.sendKeys("ab" + Keys.TAB + "c");
    assertEquals("ab", fieldOne.getAttribute("value"));
    assertEquals("c", fieldTwo.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testHoldShift() {
    new Actions(driver).sendKeys("acd")
        .sendKeys(Keys.LEFT_SHIFT + "" + Keys.LEFT + Keys.LEFT)
        .sendKeys("b")
        .build().perform();
    assertEquals("ab", fieldOne.getAttribute("value"));
  }

  @Test
  public void testShiftCapitals() {
    keyboard.sendKeys("a", Keys.SHIFT + "bc", "d");
    assertEquals("aBCD", fieldOne.getAttribute("value"));
  }

  @Test
  public void depressShift() {
    keyboard.sendKeys(Keys.SHIFT, "a", Keys.SHIFT, "b");
    assertEquals("Ab", fieldOne.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testHoldControl() {
    // Control + A
    keyboard.sendKeys("a" + Keys.CONTROL + "a" + Keys.CONTROL + "bc");
    assertEquals("bc", fieldOne.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE)
  public void testMultipleModifiers() {
    if (Platform.getCurrent().is(Platform.MAC)) {
      new Actions(driver).sendKeys("abc defghij")
          .sendKeys(Keys.ALT, Keys.LEFT_SHIFT, Keys.LEFT)
          .sendKeys(Keys.BACK_SPACE)
          .build().perform();
    } else {
      new Actions(driver).sendKeys("abc defghij")
          .sendKeys(Keys.CONTROL, Keys.LEFT_SHIFT, Keys.LEFT)
          .sendKeys(Keys.BACK_SPACE)
          .build().perform();
    }

    assertEquals("abc ", fieldOne.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testCopyPaste() throws Exception {
    new Actions(driver).sendKeys("hello world")
        .sendKeys(Keys.CONTROL + "a")
        .sendKeys(Keys.CONTROL + "c")
        .sendKeys(fieldTwo, Keys.CONTROL + "v")
        .build().perform();

    assertEquals("hello world", fieldTwo.getAttribute("value"));
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testCopyPasteLeftControl() throws Exception {
    new Actions(driver).sendKeys("hello world")
        .sendKeys(Keys.LEFT_CONTROL + "a")
        .sendKeys(Keys.LEFT_CONTROL + "c")
        .sendKeys(fieldTwo, Keys.LEFT_CONTROL + "v")
        .build().perform();

    assertEquals("hello world", fieldTwo.getAttribute("value"));
  }

}