package com.opera.core.systems;

import com.opera.core.systems.testing.Ignore;

import org.junit.Test;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class QuickWindowTest extends OperaDesktopDriverTestCase {

  @Test
  public void activeQuickWindowID() {
    assertTrue("getActiveQuickWindowID() should be greater than 0",
               driver.getActiveQuickWindowID() > 0);
  }

  @Test
  public void windowCount() {
    assertTrue("getQuickWindowCount() should be greater than 0",
               driver.getQuickWindowCount() > 0);
  }

  @Test
  public void windowListSize() {
    assertEquals(driver.getQuickWindowCount(), driver.getQuickWindowList().size());
  }

  @Test
  public void windowName() {
    for (QuickWindow window : driver.getQuickWindowList()) {
      assertEquals(window.getName(), driver.getQuickWindowName(window.getWindowID()));
    }
  }

}