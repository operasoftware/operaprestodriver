package com.opera.core.systems;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opera.core.systems.settings.OperaDriverSettings;

public class PreferencesDialogTest extends TestBase {
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
  public void testPreferencesDialog() {
    driver.waitStart();
    driver.operaDesktopAction("Show preferences");

    int win_id = driver.waitForWindowShown("New Preferences Dialog");
    QuickWidget qw = driver.findWidgetByName(-1, "Startup_mode_dropdown");
    // assertTrue(qw != null);
    assertTrue("Drop down is visible", qw.isVisible());
    assertTrue("Chosen entry is startup with homepage", qw
        .verifyText("D_STARTUP_WITH_HOMEPAGE"));
    assertTrue("Startup w/home is selected", qw
        .isSelected("D_STARTUP_WITH_HOMEPAGE"));

    driver.waitStart();

    driver.operaDesktopAction("Cancel");
    driver.waitForWindowClose("New Preferences Dialog");
  }

}
