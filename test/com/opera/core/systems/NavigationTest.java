package com.opera.core.systems;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NavigationTest extends TestBase {
  @Test
  public void testBack() {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");

    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testForward() {
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().indexOf("keys.html") > 0);
  }

  @Test
  public void testBack2() {
    driver.navigate().back();
    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().indexOf("javascript.html") > 0);
  }

  @Test
  public void testForward2() {
    driver.navigate().forward();
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().indexOf("keys.html") > 0);
  }

  @Test
  public void testHttpRedirect() throws Exception {
    driver.get("http://t/core/bts/javascript/CORE-26410/003-2.php");

    // Wait for redirect
    Thread.sleep(1000);

    assertEquals("http://t/core/bts/javascript/CORE-26410/001-3.php",
                 driver.getCurrentUrl());
  }
}
