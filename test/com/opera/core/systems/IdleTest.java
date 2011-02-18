package com.opera.core.systems;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.opera.core.systems.scope.internal.OperaIntervals;

public class IdleTest extends TestBase {
  // Timeout vars for every test
  private static long start, end;
  // Make sure we're actually using Idle, and not hitting the timeout
  private static long timeout = OperaIntervals.PAGE_LOAD_TIMEOUT.getValue();

  @Before
  public void setUp() {
    start = end = 0;
  }

  @After
  public void tearDown() {
    // Make sure the test hasn't passed because we hit the page load
    // timeout instead of using OperaIdle
    Assert.assertTrue("Took longer than page timeout", end - start < timeout);
  }

  private void start() { start = System.currentTimeMillis(); }
  private void stop() { end = System.currentTimeMillis(); }

  @Test
  public void testGet() throws Exception {
    start();
    getFixture("test.html");
    stop();

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testBack() throws Exception {
    getFixture("javascript.html");

    start();
    driver.navigate().back();
    stop();

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testForward() throws Exception {
    start();
    driver.navigate().forward();
    stop();

    Assert.assertEquals(fixture("javascript.html"), driver.getCurrentUrl());
  }

  @Test
  public void testBack2() throws Exception {
    start();
    driver.navigate().back();
    stop();

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }

  @Test
  public void testRefresh() throws Exception {
    getFixture("test.html");
    ((OperaWebElement) driver.findElementById("input_email")).sendKeys("before refresh");

    start();
    driver.navigate().refresh();
    stop();

    Assert.assertEquals("", driver.findElementById("input_email").getValue());
  }

  @Test
  public void testClick() throws Exception {
    getFixture("test.html");

    start();
    driver.findElementById("local").click();
    stop();

    Assert.assertEquals(fixture("two_input_fields.html"), driver.getCurrentUrl());
  }

  @Test
  public void testClickXY() throws Exception {
    getFixture("test.html");

    start();
    ((OperaWebElement) driver.findElementById("local")).click(3, 5);
    stop();

    Assert.assertEquals(fixture("two_input_fields.html"), driver.getCurrentUrl());
  }

  @Test
  public void testKeyEnter() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    driver.findElementById("one").click();

    // submit form
    start();
    driver.key("Enter");
    stop();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test
  public void testSendKeysNewline() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    start();
    ((OperaWebElement) driver.findElementById("one")).sendKeys("\n");
    stop();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test
  public void testSetSelected() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start();
    ((OperaWebElement) driver.findElementById("check")).setSelected();
    stop();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  @Test
  public void testSubmit() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start();
    ((OperaWebElement) driver.findElementById("test_form")).submit();
    stop();

    // +"?" for submitted query string
    Assert.assertEquals(fixture("test.html")+"?", driver.getCurrentUrl());
  }

  /* Begin testing OperaIdle conditions */

  @Test
  public void testEcmascriptLoop() throws Exception {
    start();
    getFixture("idle/ecmascript-loop.html");
    stop();

    Assert.assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testEcmascriptTimeout() throws Exception {
    start();
    getFixture("idle/ecmascript-timeout.html");
    stop();

    Assert.assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testReflow() throws Exception {
    // Need #box to activate the :target pseudo class
    start();
    getFixture("idle/reflow.html#box");
    stop();

    OperaWebElement box = (OperaWebElement) driver.findElementById("box");
    Assert.assertEquals(200, box.getSize().width);
  }

  @Test
  public void testMetarefresh() throws Exception {
    getFixture("idle/metarefresh.html");

    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }
}
