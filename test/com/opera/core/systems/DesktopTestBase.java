package com.opera.core.systems;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DesktopTestBase extends TestBase {

  protected static OperaDesktopDriver driver;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    driver = new OperaDesktopDriver();
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    driver.quit();
  }

}