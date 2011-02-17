package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Test;

import com.opera.core.systems.interaction.OperaAction;

public class IdleTest extends TestBase {
  // Make sure we're actually using Idle, and not hitting the timeout
  // this should be OperaIntervals.PAGE_LOAD_TIMEOUT, but JUnit requires
  // a constant. This timeout is the same for all tests.
  @Test(timeout=5000)
  public void testGet() throws Exception {
    getFixture("test.html");
    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testBack() throws Exception {
    getFixture("javascript.html");

    driver.navigate().back();
    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testForward() throws Exception {
    driver.navigate().forward();
    Assert.assertEquals(fixture("javascript.html"), driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testBack2() throws Exception {
    driver.navigate().back();
    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testRefresh() throws Exception {
    getFixture("test.html");
    ((OperaWebElement) driver.findElementById("input_email")).sendKeys("before refresh");

    driver.navigate().refresh();
    Assert.assertEquals("", driver.findElementById("input_email").getValue());
  }

  @Test(timeout=5000)
  public void testClick() throws Exception {
    getFixture("test.html");

    driver.findElementById("local").click();
    Assert.assertEquals(fixture("two_input_fields.html"), driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testClickXY() throws Exception {
    getFixture("test.html");

    ((OperaWebElement) driver.findElementById("local")).click(3, 5);
    Assert.assertEquals(fixture("two_input_fields.html"), driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testKeyEnter() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    driver.findElementById("one").click();
    // submit form
    driver.key("Enter");

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testSendKeysNewline() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    ((OperaWebElement) driver.findElementById("one")).sendKeys("\n");

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testSetSelected() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    ((OperaWebElement) driver.findElementById("check")).setSelected();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test(timeout=5000)
  public void testSubmit() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    ((OperaWebElement) driver.findElementById("test_form")).submit();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }
}
