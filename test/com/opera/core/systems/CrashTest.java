package com.opera.core.systems;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    Runtime.getRuntime().exec("kill `pgrep opera`").waitFor();
  }

  @Test
  public void testCore32291() {
    driver.get("http://t/core/bts/crashers/visual/CORE-32291/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  @Test
  public void testCore32766() {
    driver.get("http://t/core/bts/crashers/visual/CORE-32766/002.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  @Test
  public void testCore33582() {
    driver.get("http://t/core/bts/crashers/visual/CORE-33582/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  @Test
  public void testCore34039() {
    driver.get("http://t/core/bts/crashers/visual/CORE-34039/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }

  @Test
  public void testCore34284() {

    driver.get("http://t/core/bts/crashers/visual/CORE-34284/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());

  }

  @Test
  public void testCore32224() {
    driver.get("http://t/core/bts/crashers/visual/CORE-32224/001.html");
    Assert.assertTrue(driver.getRunner().hasOperaCrashed());
  }
}