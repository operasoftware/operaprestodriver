package com.opera.core.systems;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.opera.core.systems.settings.OperaDriverSettings;

public class DesktopTestBase extends TestBase {

  protected static OperaDesktopDriver driver;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    OperaDriverSettings settings = new OperaDriverSettings();
    setUpSettings(settings);

    driver = new OperaDesktopDriver(settings);
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    driver.shutdown();
  }

}