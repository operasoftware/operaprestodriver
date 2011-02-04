package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Test;

public class OperaActionTest extends TestBase {

  @Test(expected=org.openqa.selenium.WebDriverException.class)
  public void testDoesntExist() throws Exception {
    driver.operaAction("this action does exist");
  }

  // I think the next two tests should throw exceptions... but I'm not sure
  @Test(expected=org.openqa.selenium.WebDriverException.class)
  public void testMissingParam() throws Exception {
    driver.operaAction("Set preference");
  }
  @Test(expected=org.openqa.selenium.WebDriverException.class)
  public void testExtraParam() throws Exception {
    driver.operaAction("Go to end", "an", "extra", "param");
  }

  /*
   * The tests below test some of the commonly used Opera actions
   */
  @Test
  public void testGo() throws Exception {
    driver.operaAction("Go", fixture("test.html"));
    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testCase() throws Exception {
    driver.operaAction("GO", fixture("keys.html"));
    Assert.assertEquals(fixture("keys.html"), driver.getCurrentUrl());

    driver.operaAction("go", fixture("javascript.html"));
    Assert.assertEquals(fixture("javascript.html"), driver.getCurrentUrl());
  }

  @Test
  public void testNavigateUp() throws Exception {
    getFixture("two_input_fields.html");
    driver.findElementByName("two").click();

    driver.operaAction("Navigate up");
    driver.key("enter");

    driver.type("hello");

    Assert.assertEquals("hello", driver.findElementByName("one").getValue());
  }

  @Test
  public void testNavigateDown() throws Exception {
    getFixture("two_input_fields.html");
    driver.findElementByName("one").click();

    driver.operaAction("Navigate down");
    driver.key("enter");

    driver.type("hello");

    Assert.assertEquals("hello", driver.findElementByName("two").getValue());
  }

  @Test
  public void testNavigateRight() throws Exception {
    getFixture("test.html");
    driver.findElementById("radio_little").click();

    driver.operaAction("Navigate right");
    driver.key("enter");

    Assert.assertEquals(true,
        ((OperaWebElement) driver.findElementById("radio_some")).isSelected()
    );
  }

  @Test
  public void testNavigateLeft() throws Exception {
    getFixture("test.html");
    driver.findElementById("radio_little").click();

    driver.operaAction("Navigate left");
    driver.key("enter");

    driver.type("hello");

    Assert.assertEquals("hello", driver.findElementById("input_email").getValue());
  }

}
