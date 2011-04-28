package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Test;

import com.opera.core.systems.settings.OperaDriverSettings;

public class CloseAllTest extends TestBase {

  @Test
  public void testCloseAll() throws Exception {
    try {
      driver.closeAll();
    } catch (Exception e) {}

    // 1 tab = Speed Dial page
    Assert.assertEquals(1, driver.getWindowCount());

    // Open Opera again with a tab that has a Javascript context. This will let
    // future tests run
    driver.quit();
    OperaDriverSettings settings = new OperaDriverSettings();
    settings.setOperaBinaryArguments("opera:debug");
    driver = new TestOperaDriver(settings);
  }
}

