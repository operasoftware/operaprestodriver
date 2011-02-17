package com.opera.core.systems;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.opera.core.systems.scope.internal.OperaIntervals;

public class IdleTest extends TestBase {
  // Timeout vars for every test
  private static long start, end;
  private static long timout = OperaIntervals.PAGE_LOAD_TIMEOUT.getValue();

  @After
  public void tearDown() {
    // Make sure the test hasn't passed because we hit the page load
    // timeout instead of using OperaIdle
    Assert.assertTrue("Timout", end - start < timout);
  }

  // Make sure we're actually using Idle, and not hitting the timeout
  // this should be OperaIntervals.PAGE_LOAD_TIMEOUT, but JUnit requires
  // a constant. This timeout is the same for all tests.
  @Test
  public void testGet() throws Exception {
    start = System.currentTimeMillis();
    getFixture("test.html");
    end = System.currentTimeMillis();

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testBack() throws Exception {
    getFixture("javascript.html");

    start = System.currentTimeMillis();
    driver.navigate().back();
    end = System.currentTimeMillis();

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testForward() throws Exception {
    start = System.currentTimeMillis();
    driver.navigate().forward();
    end = System.currentTimeMillis();

    Assert.assertEquals(fixture("javascript.html"), driver.getCurrentUrl());
  }

  @Test
  public void testBack2() throws Exception {
    start = System.currentTimeMillis();
    driver.navigate().back();
    end = System.currentTimeMillis();

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testRefresh() throws Exception {
    getFixture("test.html");
    ((OperaWebElement) driver.findElementById("input_email")).sendKeys("before refresh");

    start = System.currentTimeMillis();
    driver.navigate().refresh();
    end = System.currentTimeMillis();

    Assert.assertEquals("", driver.findElementById("input_email").getValue());
  }

  @Test
  public void testClick() throws Exception {
    getFixture("test.html");

    start = System.currentTimeMillis();
    driver.findElementById("local").click();
    end = System.currentTimeMillis();

    Assert.assertEquals(fixture("two_input_fields.html"), driver.getCurrentUrl());
  }

  @Test
  public void testClickXY() throws Exception {
    getFixture("test.html");

    start = System.currentTimeMillis();
    ((OperaWebElement) driver.findElementById("local")).click(3, 5);
    end = System.currentTimeMillis();

    Assert.assertEquals(fixture("two_input_fields.html"), driver.getCurrentUrl());
  }

  @Test
  public void testKeyEnter() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    driver.findElementById("one").click();

    // submit form
    start = System.currentTimeMillis();
    driver.key("Enter");
    end = System.currentTimeMillis();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test
  public void testSendKeysNewline() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    start = System.currentTimeMillis();
    ((OperaWebElement) driver.findElementById("one")).sendKeys("\n");
    end = System.currentTimeMillis();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test
  public void testSetSelected() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start = System.currentTimeMillis();
    ((OperaWebElement) driver.findElementById("check")).setSelected();
    end = System.currentTimeMillis();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test
  public void testSubmit() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start = System.currentTimeMillis();
    ((OperaWebElement) driver.findElementById("test_form")).submit();
    end = System.currentTimeMillis();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }
}
