package com.opera.core.systems;

import junit.framework.Assert;
import org.junit.Test;

// TODO make tests pass
public class CrashTest extends TestBase {
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