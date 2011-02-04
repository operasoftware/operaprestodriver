package com.opera.core.systems;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class SecurityDialogs extends DesktopTestBase {
  @Test
  public void testShowSecurityInfo() {
    driver.waitStart();

    driver.operaDesktopAction("Open url in new page",
        0, "http://t/security/bts/164110/scary_file-keyboard.kini", null);

    // Add try-catch for timeout
    int win_id = driver.waitForWindowShown("Setup Apply Dialog Confirm Dialog");

    QuickWindow security_win = driver
        .getQuickWindow("Setup Apply Dialog Confirm Dialog");
    assertTrue(win_id == security_win.getWindowID());

    List<QuickWindow> windows = driver.getQuickWindowList();
    List<QuickWidget> widgets = driver
        .getQuickWidgetList("Setup Apply Dialog Confirm Dialog");

    assertTrue("Server is t", driver.findWidgetByName(win_id, "Simple_message")
        .getText().startsWith("t"));

    assertTrue("Has security warning message", driver.findWidgetByName(win_id,
        "Simple_message").verifyContainsText("D_SECURITYALERT_SETUPDOWNLOAD"));

    QuickWidget no_btn = driver.findWidgetByName(win_id, "button_Cancel");
    QuickWidget yes_btn = driver.findWidgetByName(win_id, "button_Ok");
    assertTrue("Yes is not default button", yes_btn != null
        && !yes_btn.isDefault());
    assertTrue("No is default button", no_btn.isDefault());

    driver.operaDesktopAction("Cancel", 0, null, null);
    // driver.keyPress("F1", ModifierPressed.NONE);
    // no_btn.click(MouseButton.LEFT, 1, ModifierPressed.NONE);
  }
}
