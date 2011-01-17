package com.opera.core.systems;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BookmarkDialogTest extends DesktopTestBase {
  @Test
  public void testBookmarkDialog() {
    driver.waitStart();
    driver.operaDesktopAction("Add to bookmarks");

    int win_id = driver.waitForWindowShown("Bookmark Properties Dialog");
    assertTrue("Got bookmark dialog", win_id >= 0);
    QuickWidget widget = driver.findWidgetByName(-1, "label_for_Nick_edit");
    assertTrue("Has name edit", widget != null);
    QuickWidget qw = driver.findWidgetByName(-1, "label_for_Name_edit");
    System.out.println("By name: " + qw.getText());
    qw.verifyText("DI_ID_HLFILEPROP_FNAME_LABEL");

    driver.findWidgetByName(win_id, "button_Cancel");

    driver.waitStart();
    // btn.click(MouseButton.LEFT,1,ModifierPressed.NONE);
    driver.operaDesktopAction("Cancel");
    System.out.println("--Wait for window to close--");
    driver.waitForWindowClose("Bookmark Properties Dialog");

  }

}
