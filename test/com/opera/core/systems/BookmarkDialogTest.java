package com.opera.core.systems;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opera.core.systems.settings.OperaDriverSettings;

public class BookmarkDialogTest extends TestBase {
  private static OperaDesktopDriver driver;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    OperaDriverSettings settings = new OperaDriverSettings();
    // TODO make work everywhere
    settings.setOperaBinaryLocation(binary);
    settings.setOperaLauncherBinary(launcher);

    driver = new OperaDesktopDriver(settings);
  }

  // FIXME shutdown driver
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

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
