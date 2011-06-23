package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;


public class DriverKeysTest extends TestBase {
  private WebElement fieldOne;
  private WebElement fieldTwo;

  @Before
  public void setUp() {
    getFixture("two_input_fields.html");
    fieldOne = driver.findElementByName("one");
    fieldTwo = driver.findElementByName("two");

    fieldOne.click();
  }

  @Test
  public void testSingleCharacter() {
    driver.key("a");
    Assert.assertEquals("a", fieldOne.getAttribute("value"));
  }

  @Test
  public void testSequence() {
    driver.key("a");
    driver.key("b");
    driver.key("c");
    driver.key("d");
    driver.key("e");
    driver.key("f");
    Assert.assertEquals("abcdef", fieldOne.getAttribute("value"));
  }

  @Test
  public void testUpDown() {
    driver.keyDown("a");
    driver.keyUp("a");
    driver.keyDown("b");
    driver.keyUp("b");
    Assert.assertEquals("ab", fieldOne.getAttribute("value"));

  }

  @Test
  public void testMultiByte() {
    driver.key("ル");
    driver.key("ビ");
    driver.key("ー");
    driver.key(" ");
    driver.key("水");
    Assert.assertEquals("ルビー 水", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeSingle() {
    driver.type("a");
    Assert.assertEquals("a", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeMulti() {
    driver.type("abcdef");
    Assert.assertEquals("abcdef", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeMultiByte() {
    driver.type("ルビー 水");
    Assert.assertEquals("ルビー 水", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeNotModify() throws Exception {
    driver.type("ac");
    driver.type("left");
    driver.type("b");
    Assert.assertEquals("acleftb", fieldOne.getAttribute("value"));
  }

  @Test
  public void testLeftArrow() {
    driver.type("ac");
    driver.key("Left");
    driver.type("b");
    Assert.assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testname() throws Exception {
    driver.type("ac");
    driver.keyDown("Left");
    driver.keyUp("Left");
    driver.type("b");
    Assert.assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testCaseInsensitveLeftArrow() {
    driver.type("ac");
    driver.key("left");
    driver.key("b");
    Assert.assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTab() {
    driver.type("ab");
    driver.key("tab");
    driver.type("c");
    Assert.assertEquals("ab", fieldOne.getAttribute("value"));
    Assert.assertEquals("c", fieldTwo.getAttribute("value"));
  }

  @Test
  public void testHoldShift2() throws Exception {
    driver.key("a");
    driver.key("c");
    driver.key("d");
    driver.keyDown("shift");
    driver.key("left");
    driver.key("left");
    driver.keyUp("shift");
    driver.key("b");

    Assert.assertEquals("ab", fieldOne.getAttribute("value"));
  }

  @Test
  public void testShiftCapitals() throws Exception {
    driver.type("a");
    driver.keyDown(OperaKeys.SHIFT.getValue());
    driver.type("b");
    driver.type("c");
    driver.keyUp(OperaKeys.SHIFT.getValue());
    driver.type("d");

    Assert.assertEquals("aBCd", fieldOne.getAttribute("value"));
  }

  @Test
  public void testHoldControl() {
    driver.key("a");
    // Ctrl+A, select all
    driver.keyDown("control");
    driver.type("a");
    driver.keyUp("control");
    driver.key("b");
    driver.key("c");
    Assert.assertEquals("bc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testMultipleModifiers() throws Exception {
    driver.type("abc defghij");
    driver.keyDown("control");
    driver.keyDown("shift");
    driver.key("left");
    driver.keyUp("control");
    driver.keyUp("shift");
    driver.key("backspace");

    Assert.assertEquals("abc ", fieldOne.getAttribute("value"));
  }

}
