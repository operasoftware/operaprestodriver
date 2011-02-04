package com.opera.core.systems;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.opera.core.systems.scope.exceptions.CommunicationException;

// FIXME make tests pass
public class CrashTest extends TestBase {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {
    super.setUpBeforeClass();
  }

  @After
  public void tearDown() throws Exception {
    super.tearDownAfterClass();
    // Make sure Opera is gone
    Runtime.getRuntime().exec("kill `pgrep opera").waitFor();
  }

  @Ignore
  @Test
  public void testCore34284() {
    Assert.assertTrue(driver.getRunner().isOperaRunning());

    try {
      driver.get("http://t/core/bts/crashers/visual/CORE-34284/001.html");
    } catch (CommunicationException e) {}

    Assert.assertFalse(driver.getRunner().isOperaRunning());
  }

  @Ignore
  @Test
  public void testCore32224() {
    Assert.assertTrue(driver.getRunner().isOperaRunning());

    try {
      driver.get("http://t/core/bts/crashers/visual/CORE-32224/001.html");
    } catch (CommunicationException e) {}

    Assert.assertFalse(driver.getRunner().isOperaRunning());
  }

}