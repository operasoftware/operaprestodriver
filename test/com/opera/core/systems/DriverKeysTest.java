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
    Assert.assertEquals("a", fieldOne.getValue());
  }

  @Test
  public void testSequence() {
    driver.key("a");
    driver.key("b");
    driver.key("c");
    driver.key("d");
    driver.key("e");
    driver.key("f");
    Assert.assertEquals("abcdef", fieldOne.getValue());
  }

  @Test
  public void testUpDown() {
    driver.keyDown("a");
    driver.keyUp("a");
    driver.keyDown("b");
    driver.keyUp("b");
    Assert.assertEquals("ab", fieldOne.getValue());

  }

  @Test
  public void testMultiByte() {
    driver.key("ル");
    driver.key("ビ");
    driver.key("ー");
    driver.key(" ");
    driver.key("水");
    Assert.assertEquals("ルビー 水", fieldOne.getValue());
  }

  @Test
  public void testTypeSingle() {
    driver.type("a");
    Assert.assertEquals("a", fieldOne.getValue());
  }

  @Test
  public void testTypeMulti() {
    driver.type("abcdef");
    Assert.assertEquals("abcdef", fieldOne.getValue());
  }

  @Test
  public void testTypeMultiByte() {
    driver.type("ルビー 水");
    Assert.assertEquals("ルビー 水", fieldOne.getValue());
  }

  @Test
  public void testTypeNotModify() throws Exception {
    driver.type("ac");
    driver.type("left");
    driver.type("b");
    Assert.assertEquals("acleftb", fieldOne.getValue());
  }

  @Test
  public void testLeftArrow() {
    driver.type("ac");
    driver.key("Left");
    driver.type("b");
    Assert.assertEquals("abc", fieldOne.getValue());
  }

  @Test
  public void testname() throws Exception {
    driver.type("ac");
    driver.keyDown("Left");
    driver.keyUp("Left");
    driver.type("b");
    Assert.assertEquals("abc", fieldOne.getValue());
  }

  @Test
  public void testCaseInsensitveLeftArrow() {
    driver.type("ac");
    driver.key("left");
    driver.key("b");
    Assert.assertEquals("abc", fieldOne.getValue());
  }

  @Test
  public void testTab() {
    driver.type("ab");
    driver.key("tab");
    driver.type("c");
    Assert.assertEquals("ab", fieldOne.getValue());
    Assert.assertEquals("c", fieldTwo.getValue());
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

    Assert.assertEquals("ab", fieldOne.getValue());
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
    Assert.assertEquals("bc", fieldOne.getValue());
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

    Assert.assertEquals("abc ", fieldOne.getValue());
  }

  @Test
  public void testLongHold() throws Exception {
    driver.keyDown("a");
    Thread.sleep(1000);
    driver.keyUp("a");

    ((OperaWebElement) fieldOne).sendKeys("a");

    Assert.assertTrue("multiple 'a's", fieldOne.getValue().contains("aa"));
  }

}
