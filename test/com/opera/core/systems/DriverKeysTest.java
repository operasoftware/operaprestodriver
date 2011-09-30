package com.opera.core.systems;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static org.junit.Assert.assertEquals;

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
    assertEquals("a", fieldOne.getAttribute("value"));
  }

  @Test
  public void testSequence() {
    driver.key("a");
    driver.key("b");
    driver.key("c");
    driver.key("d");
    driver.key("e");
    driver.key("f");
    assertEquals("abcdef", fieldOne.getAttribute("value"));
  }

  @Test
  public void testUpDown() {
    driver.keyDown("a");
    driver.keyUp("a");
    driver.keyDown("b");
    driver.keyUp("b");
    assertEquals("ab", fieldOne.getAttribute("value"));

  }

  @Test
  public void testMultiByte() {
    driver.key("ル");
    driver.key("ビ");
    driver.key("ー");
    driver.key(" ");
    driver.key("水");
    assertEquals("ルビー 水", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeSingle() {
    driver.type("a");
    assertEquals("a", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeMulti() {
    driver.type("abcdef");
    assertEquals("abcdef", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeMultiByte() {
    driver.type("ルビー 水");
    assertEquals("ルビー 水", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTypeNotModify() throws Exception {
    driver.type("ac");
    driver.type("left");
    driver.type("b");
    assertEquals("acleftb", fieldOne.getAttribute("value"));
  }

  @Test
  public void testLeftArrow() {
    driver.type("ac");
    driver.key("Left");
    driver.type("b");
    assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testname() throws Exception {
    driver.type("ac");
    driver.keyDown("Left");
    driver.keyUp("Left");
    driver.type("b");
    assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testCaseInsensitveLeftArrow() {
    driver.type("ac");
    driver.key("left");
    driver.key("b");
    assertEquals("abc", fieldOne.getAttribute("value"));
  }

  @Test
  public void testTab() {
    driver.type("ab");
    driver.key("tab");
    driver.type("c");
    assertEquals("ab", fieldOne.getAttribute("value"));
    assertEquals("c", fieldTwo.getAttribute("value"));
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

    assertEquals("ab", fieldOne.getAttribute("value"));
  }

  @Test
  public void testShiftCapitals() throws Exception {
    driver.type("a");
    driver.keyDown("shift");
    driver.type("b");
    driver.type("c");
    driver.keyUp("shift");
    driver.type("d");

    assertEquals("aBCd", fieldOne.getAttribute("value"));
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
    assertEquals("bc", fieldOne.getAttribute("value"));
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

    assertEquals("abc ", fieldOne.getAttribute("value"));
  }

  @Test
  public void testCopyPaste() throws Exception {
    driver.type("hello world");
    Actions action = new Actions(driver);
    action.sendKeys(Keys.CONTROL + "a");
    action.sendKeys(Keys.CONTROL + "c");
    action.sendKeys(fieldTwo, Keys.CONTROL + "v");

    action.perform();

    assertEquals("hello world", fieldTwo.getAttribute("value"));
  }

  @Test
  public void testCopyPasteLeftControl() throws Exception {
    driver.type("hello world");
    Actions action = new Actions(driver);
    action.sendKeys(Keys.LEFT_CONTROL + "a");
    action.sendKeys(Keys.LEFT_CONTROL + "c");
    action.sendKeys(fieldTwo, Keys.LEFT_CONTROL + "v");

    action.perform();

    assertEquals("hello world", fieldTwo.getAttribute("value"));
  }

}
