/*
Copyright 2012 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems;

import com.opera.core.systems.scope.protos.DesktopWmProtos;
import com.opera.core.systems.scope.protos.PrefsProtos;
import com.opera.core.systems.scope.protos.SystemInputProtos;
import org.junit.*;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;


/*
The OperaDesktopDriverTest tries hard to use the desktop driver in the very same way
as the operawatir RSpec suite does. The goal is to try to test the behavior when
the desktop driver is used by the operawatir suite, i.e. mimic the everyday use of the
desktop driver that is not used as it is but rather via the operawatir framework.

In order to be able to do that, you need to have a working Opera installation on the
system that you are running the tests on. The binary is looked for by the OperaPaths class.
*/

public class OperaDesktopDriverTest extends OperaDesktopDriverTestCase {
/*  @BeforeClass
  public static void beforeAll() throws Exception {
    System.out.printf("beforeAll\n");
  }

  @Before
  public void beforeEach() {
    System.out.printf("beforeEach\n");
  }

  @After
  public void afterEach() {
    System.out.printf("afterEach\n");
  }

  @AfterClass
  public static void afterAll() throws Exception {
    System.out.printf("afterAll\n");
  }*/

  @Test(expected = WebDriverException.class)
  @Ignore
  public void testSmokeNoAutoStartThrows() {
    OperaPaths.overrideOperaPathEnvVar(null);
    driver = new TestOperaDesktopDriver(getDefaultCaps());
    // The driver is supposed to wait for connection that never comes and throw
  }

  @Test
  public void testSmokeNoQuitStartsOK() {
    DesiredCapabilities caps = getDefaultCaps();
    caps.setCapability("opera.no_quit", true);

    setOperaPath();

    driver = new TestOperaDesktopDriver(caps);

    assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
  }

  @Test
  public void testSmokeNoQuitPidOK()
  {
    Integer browserPid = driver.getPid();

    assertTrue("PID reported by browser should > 0", browserPid > 0);
  }

  @Test
  public void testSmokeNoQuitBrowserIsRunning() throws IOException, InterruptedException {
    Integer browserPid = driver.getPid();

    assertTrue("The browser process should BE running.", isPidRunning(browserPid));
  }
  
  @Test
  public void testSmokeNoQuitCapsOK() {
    String binaryPath = setOperaPath();

    Map<String, String> expectedValues = new HashMap<String, String>();
    expectedValues.put("opera.binary", binaryPath);
    expectedValues.put("javascriptEnabled", "true");
    expectedValues.put("opera.host", "127.0.0.1");
    expectedValues.put("opera.idle", "false");
    expectedValues.put("browserName", "opera");
    expectedValues.put("opera.autostart", "true");
    expectedValues.put("opera.logging.file", "(null)");
    expectedValues.put("opera.guess_binary_path", "false");
    expectedValues.put("opera.logging.level", "INFO");
    expectedValues.put("opera.launcher", "(null)");
    expectedValues.put("opera.display", "(null)");
    expectedValues.put("opera.product", "desktop");
    expectedValues.put("opera.arguments", "");
    expectedValues.put("opera.no_quit", "true");
    expectedValues.put("opera.no_restart", "false");

    checkCaps(driver.getCapabilities(), expectedValues);
  }

  @Test
  public void testSmokeNoQuitBrowserQuits() throws IOException, InterruptedException {
    Integer browserPid = driver.getPid();
    driver.quit();

    Boolean isPidRunning = isPidRunning(browserPid);
    // Kill the browser process eventually.
    killPid(browserPid);

    assertTrue("The browser process should BE running.", isPidRunning);
  }

  @Test
  public void testSmokeAutoStartBrowserStarts() {
    DesiredCapabilities caps = getDefaultCaps();
    driver = new TestOperaDesktopDriver(caps);

    assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
  }

  @Test
  public void testSmokeAutoStartPidOK() {
    Integer browserPid = driver.getPid();

    assertTrue("PID reported by browser should > 0", browserPid > 0);
  }

  @Test
  public void testSmokeAutoStartPidRunningOK() throws IOException, InterruptedException {
    Integer browserPid = driver.getPid();

    assertTrue("The browser process should BE running.", isPidRunning(browserPid));
  }

  @Test
  public void testSmokeAutoStartOperaPathReportedOK()
  {
    String binaryPath = setOperaPath();
    String operaPathFromDriver = driver.getOperaPath();

    assertEquals(operaPathFromDriver, binaryPath);
  }

  @Test
  public void testSmokeAutoStartCapsOK() {
    String binaryPath = setOperaPath();

    Map<String, String> expectedValues = new HashMap<String, String>();
    expectedValues.put("opera.binary", binaryPath);
    expectedValues.put("javascriptEnabled", "true");
    expectedValues.put("opera.host", "127.0.0.1");
    expectedValues.put("opera.idle", "false");
    expectedValues.put("browserName", "opera");
    expectedValues.put("opera.autostart", "true");
    expectedValues.put("opera.logging.file", "(null)");
    expectedValues.put("opera.guess_binary_path", "false");
    expectedValues.put("opera.logging.level", "INFO");
    expectedValues.put("opera.launcher", "(null)");
    expectedValues.put("opera.display", "(null)");
    expectedValues.put("opera.product", "desktop");
    expectedValues.put("opera.arguments", "");
    expectedValues.put("opera.no_quit", "false");
    expectedValues.put("opera.no_restart", "false");

    checkCaps(driver.getCapabilities(), expectedValues);
  }

  @Test
  public void testSmokeAutoStartBrowserQuitsOK() throws IOException, InterruptedException {
    Integer browserPid = driver.getPid();
    driver.quit();
    assertFalse("The browser process should NOT be running.", isPidRunning(browserPid));
  }

  @Test
  public void testManualProfileBrowserStartsOK()
  {
    DesiredCapabilities caps = getDefaultCaps();
    profileTmpFolder = tmpFolder.newFolder("OperaDesktopDriver-junit-test");
    String profileTmpFolderPath = profileTmpFolder.getAbsolutePath();
    caps.setCapability("opera.profile", profileTmpFolderPath);
    setOperaPath();
    driver = new TestOperaDesktopDriver(caps);

    assertTrue(driver.isRunning());
  }

  @Test
  public void testManualProfileLargePrefsDirMatches() throws IOException {
    File profileTmpFolderPath = new File(profileTmpFolder.getAbsolutePath()).getCanonicalFile();
    File largePrefsPath = new File(driver.getLargePreferencesPath()).getCanonicalFile();

    assertEquals(profileTmpFolderPath, largePrefsPath);
  }
  
  @Test
  public void testManualProfileSmallPrefsDirMatches() throws IOException {
    File profileTmpFolderPath = new File(profileTmpFolder.getAbsolutePath()).getCanonicalFile();
    File smallPrefsPath = new File(driver.getSmallPreferencesPath()).getCanonicalFile();

    assertEquals(profileTmpFolderPath,  smallPrefsPath);
  }

  @Test
  public void testManualProfileCachePrefsDirMatches() throws IOException {
    File profileTmpDirPath = new File(profileTmpFolder.getAbsolutePath()).getCanonicalFile();
    File cachePrefsPath = new File(driver.getCachePreferencesPath()).getCanonicalFile();

    assertEquals(profileTmpDirPath, cachePrefsPath);
  }

  @Test
  public void testManualProfileProfileCapValueMatches() {
    // Strip the trailing slash/backslash - this is an unfortunate mismatch that the capability reported by the
    // driver doesn't have that while the paths reported by the browser do - at least on Windows.
    String prefsPathNoTrailingSlash = null;
/*
    if (largePrefsPath.endsWith("\\") || largePrefsPath.endsWith("/"))
      prefsPathNoTrailingSlash = largePrefsPath.substring(0, largePrefsPath.length() - 1);
    else
      prefsPathNoTrailingSlash = largePrefsPath;
*/

    String profileTmpDirPath = profileTmpFolder.getAbsolutePath();

    Map<String, String> expectedValues = new HashMap<String, String>();
    expectedValues.put("opera.profile", profileTmpDirPath);
    checkCaps(driver.getCapabilities(), expectedValues);
  }

  /*
  This test is supposed to stress test starting/stopping of Opera.
  This has known to be a problem with the desktop Opera due to unpredictable startup/shutdown times,
  different computer speeds, crashes and not the best synchronization between the driver and the launcher,
  that mainly depends on some sleep calls.
  This test is known to fail you and this problem needs to be approached by the desktop.
   */
  @Test
  @Ignore
  public void testStartQuitOpera() throws IOException, InterruptedException {
    if (driver != null)
      driver.quit();

    driver = new TestOperaDesktopDriver(getDefaultCaps());

    Integer browserPid = driver.getPid();
    assertTrue("PID reported by browser should > 0", browserPid > 0);
    assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
    assertTrue("The browser process should BE running.", isPidRunning(browserPid));

    for (int i = 0; i < 5; i ++)
    {
      driver.quitOpera();
      assertFalse("Driver doesn't report the Opera as stopped even thou it should.", driver.isOperaRunning());
      assertFalse("The browser process should NOT be running.", isPidRunning(browserPid));

      driver.resetOperaPrefs("");
      browserPid = driver.getPid();
      assertTrue("PID reported by browser should > 0", browserPid > 0);
      assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
      assertTrue("The browser process should BE running.", isPidRunning(browserPid));
    }
  }



/*  @Test
  public void testAutoProfile() throws IOException, InterruptedException {
    DesktopTestHelper helper = new DesktopTestHelper();
    DesiredCapabilities caps = helper.getDefaultCaps();

    helper.setOperaPath();

    driver = new TestOperaDesktopDriver(caps);

    String largePrefsPath = driver.getLargePreferencesPath();
    String smallPrefsPath = driver.getSmallPreferencesPath();
    String cachePrefsPath = driver.getCachePreferencesPath();

    assertTrue("largePrefsPath should be the same as smallPrefsPath", largePrefsPath.equals(smallPrefsPath));
    assertTrue("largePrefsPath should be the same as cachePrefsPath", largePrefsPath.equals(cachePrefsPath));

    // Strip the trailing slash/backslash - this is an unfortunate mismatch that the capability reported by the
    // driver doesn't have that while the paths reported by the browser do - at least on Windows.
    String prefsPathNoTrailingSlash = null;
    if (largePrefsPath.endsWith("\\") || largePrefsPath.endsWith("/"))
      prefsPathNoTrailingSlash = largePrefsPath.substring(0, largePrefsPath.length() - 1);
    else
      prefsPathNoTrailingSlash = largePrefsPath;

    Map<String, String> expectedValues = new HashMap<String, String>();
    expectedValues.put("opera.profile", prefsPathNoTrailingSlash);

    helper.checkCaps(driver.getCapabilities(), expectedValues);

    // Expect that the automatically created profile path has the 'opera-profile' string in it
    // At this point we are sure that all the three profile paths are the same and each of them
    // also matches the 'opera.profile' capability value.
    assertTrue("The prefs folder reported by the browser should contain the 'opera-profile' string.", largePrefsPath.matches(".*opera-profile.*"));
    driver.quit();
  }*/

  @Test
  public void testApiTestsStartBrowserBeforeTests() {
    setOperaPath();
    driver = new TestOperaDesktopDriver(getDefaultCaps());

    assertTrue(driver.isRunning());
  }

  /*
   This test is supposed to check the QuickWindow API of the OperaDesktopDriver.
   The API is currently inconsitent, it should most probably be depreceated and
   replaced with something more readable.
   Moreover, the API introduces a bit of uncertanity when we look for window by its
   name, the returned window is actually the first one with the matching name found.
   That will cause problems if we get more seriously into handling multiple windows.

   The test does its best to test the current API, it should be rewritten after replacing
   the API.
   */
  @Test
  public void testQuickWindowApiGetActiveQuickWindowID() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();

    assertTrue("getActiveQuickWindowID() should be > 0.", activeQuickWindowId > 0);
  }

  @Test
  public void testQuickWindowApiGetWindowCount() {
    Integer quickWindowCount = driver.getQuickWindowCount();

    assertTrue("getQuickWindowCount() should be > 0.", quickWindowCount > 0);
  }

  @Test
  public void testQuickWindowApiGetQuickWindowListSize() {
    List<QuickWindow> quickWindowList = driver.getQuickWindowList();
    Integer quickWindowCount = driver.getQuickWindowCount();
    Integer quickWindowListSize = quickWindowList.size();

    assertTrue("getQuickWindowList().size() should == getQuickWindowCount()", quickWindowListSize == quickWindowCount);
  }

  @Test
  public void testQuickWindowApiGetQuickWindowName() {
    List<QuickWindow> quickWindowList = driver.getQuickWindowList();

    for (QuickWindow w: quickWindowList) {
      String nameById = driver.getQuickWindowName(w.getWindowID());

      assertEquals(w.getName(), nameById);
    }
  }

  @Test
  public void testQuickWindowApiFindWindowById() {
    List<QuickWindow> quickWindowList = driver.getQuickWindowList();

    for (QuickWindow w: quickWindowList) {
      QuickWindow windowById = driver.findWindowById(w.getWindowID());

      assertEquals(w.getName(), windowById.getName());
      assertEquals(w.getWindowID(), windowById.getWindowID());
      assertEquals(w.getTitle(), windowById.getTitle());
    }
  }

  /*
  testQuickWidgetAPI

  Tries its best to test the QuickWidget API of the OperaDesktopDriver.

  Assumes that after starting Opera with a clean profile there is this widget available:

  Name: tbb_Stop_Reload
  Type: BUTTON
  Text: Stop
  Visible: true

   */
  @Test
  public void testQuickWidgetApiFindWidgetByName() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    QuickWidget widgetByName = driver.findWidgetByName(DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON, activeQuickWindowId, "tbb_Stop_Reload");

    assertNotNull(widgetByName);
    assertEquals("Stop", widgetByName.getText());
  }

  @Test
  public void testQuickWidgetApiFindWidgetByText() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    QuickWidget widgetByText = driver.findWidgetByText(DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON, activeQuickWindowId, "Stop");

    assertNotNull(widgetByText);
    assertEquals("tbb_Stop_Reload", widgetByText.getName());
  }

  @Test
  public void testQuickWidgetApiFindWidgetByStringId() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    QuickWidget widgetByByStringId = driver.findWidgetByStringId(DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON, activeQuickWindowId, "SI_STOP_BUTTON_TEXT");

    assertNotNull(widgetByByStringId);
    assertEquals("tbb_Stop_Reload", widgetByByStringId.getName());
  }

  @Test
  @Ignore
  public void testQuickWidgetApiFindWidgetByPosition() {
    // TODO: Needs an implementation
  }

  @Test
  public void testQuickWidgetApiGetQuickWidgetListSize() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    List<QuickWidget> quickWidgetList = driver.getQuickWidgetList(activeQuickWindowId);
    
    assertTrue(quickWidgetList.size() > 0);
  }

  @Test
  public void testQuickWidgetApiGetQuickWidgetList() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    List<QuickWidget> quickWidgetList = driver.getQuickWidgetList(activeQuickWindowId);

    for (QuickWidget quickWidget: quickWidgetList) {
      String quickWidgetName = quickWidget.getName();

      if (quickWidgetName.isEmpty()) {
        QuickWidget quickWidgetByName = driver.findWidgetByName(quickWidget.getType(), activeQuickWindowId, quickWidgetName);

        assertNotNull(quickWidgetByName);
        assertEquals(quickWidgetByName.toFullString(), quickWidget.toFullString());
      }
    }
  }

  /*
  testStringAPI

  This test is tricky in that it depends on the language setting in Opera.
  Since there doesn't seem to be any API in the OperaDesktopDriver to
  control the language or even check it, we use the prefs API to validate that
  the language file in use is "en.lng". Not the best option, this should be
  approached with some new language control API perhaps.
   */
  @Test
  public void testStringApiVerifyLanguageFileIsEnLng() {
    String languageFile = driver.getPref("User Prefs", "Language File");

    assertNotNull(languageFile);
    assertTrue("The language file should be 'en.lng'", languageFile.endsWith("en.lng"));
  }

  @Test
  public void testStringApiGetStringAmp() {
    String fetchedAmpStringAmp = driver.getString("SI_IDM_SEARCH_ENGINE6", false);

    assertNotNull(fetchedAmpStringAmp);
    assertEquals("Price &comparison", fetchedAmpStringAmp);
  }

  @Test
  public void testStringApiGetStringNoAmp() {
    String fetchedAmpStringAmp = driver.getString("SI_IDM_SEARCH_ENGINE6", true);

    assertNotNull(fetchedAmpStringAmp);
    assertEquals("Price comparison", fetchedAmpStringAmp);
  }

  @Test
  public void testStringApiGetSubstitutedStringNoParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS"};//, "one", "two", "three"};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded %1 of %2 at %3", fetchedStringFormattersRaw);
  }

  @Test
  public void testStringApiGetSubstitutedStringAllParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "one", "two", "three"};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded one of two at three", fetchedStringFormattersRaw);
  }

  @Test
  public void testStringApiGetSubstitutedStringAllAnyParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "", "", ""};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded _ANY_ of _ANY_ at _ANY_", fetchedStringFormattersRaw);
  }

  @Test
  public void testStringApiGetSubstitutedStringSomeAnyParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "one", "", "three"};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded one of _ANY_ at three", fetchedStringFormattersRaw);
  }

  @Test
  public void testStringApiGetSubstitutedStringSubstitutionOrder() {
    // TODO: Check the substitution order. Not possible since there doesn't seem to be any string in Opera that
    // would have the orderred formatters not in order (i.e. "Something %2 somethingelse %1").
  }

  @Test
  public void testQuickMenuApiGetQuickMenuList()
  {
    // Show the top menu bar, right after a fresh start it is disabled in favor of
    // the "O-menu button".
    driver.setPref("User Prefs", "Show Menu", "1");

    List<QuickMenu> menuList = driver.getQuickMenuList();
    assertTrue("Expected to only see the 'Main Menu'", menuList.size() == 1);
  }

  @Test
  public void testQuickMenuApiGetQuickMenu() {
    // The test expects both having Opera running and the "Show Menu" set

    List<QuickMenu> menuList = driver.getQuickMenuList();
    QuickMenu mainMenuByList = menuList.get(0);
    QuickMenu mainMenuByGetQuickMenu = driver.getQuickMenu("Main Menu");

    assertNotNull(mainMenuByList);
    assertNotNull(mainMenuByGetQuickMenu);
    assertEquals(mainMenuByList.toFullString(), mainMenuByGetQuickMenu.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByName() {
    QuickMenuItem fileMenuItem = driver.getQuickMenuItemByName("Browser Tools Menu");
    assertNotNull(fileMenuItem);
  }

  @Test
  //@Ignore
  public void testQuickMenuClickMenuItemToOpenMenu() throws Exception {
    QuickMenuItem fileMenuItem = driver.getQuickMenuItemByName("Browser View Menu");
    assertNotNull(fileMenuItem);
    Integer quickMenuItemListBefore = driver.getQuickMenuItemList().size();

    if (Platform.getCurrent() == Platform.MAC) {
      driver.pressQuickMenuItem("View", false);
      driver.waitForMenuItemPressed("View");
    }
    else
      fileMenuItem.click(SystemInputProtos.MouseInfo.MouseButton.LEFT, 1, Collections.<SystemInputProtos.ModifierPressed>emptyList());

    Integer quickMenuItemListAfter = driver.getQuickMenuItemList().size();

    assertTrue(quickMenuItemListBefore < quickMenuItemListAfter);
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByAction() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    assertNotNull(menuItemByName);
    QuickMenuItem menuItemByAction = driver.getQuickMenuItemByAction("Show popup menu");
    assertNotNull(menuItemByAction);

    assertEquals(menuItemByName.toFullString(), menuItemByAction.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemBySubmenu() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemBySubmenu = driver.getQuickMenuItemBySubmenu("Developer Menu");

    assertEquals(menuItemByName.toFullString(), menuItemBySubmenu.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByText() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByText = driver.getQuickMenuItemByText("Developer Tools");

    assertEquals(menuItemByName.toFullString(), menuItemByText.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByPosition() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByPosition = driver.getQuickMenuItemByPosition(10, "Browser View Menu");

    assertEquals(menuItemByName.toFullString(), menuItemByPosition.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByAccKey() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByAccKey = driver.getQuickMenuItemByAccKey("q", "Browser View Menu");

    assertEquals(menuItemByName.toFullString(), menuItemByAccKey.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByShortcut() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByShortcut = driver.getQuickMenuItemByShortcut("F12");

    assertEquals(menuItemByName.toFullString(), menuItemByShortcut.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByStringId() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByStringId = driver.getQuickMenuItemByStringId("D_DEVTOOLS_TITLE");

    assertEquals(menuItemByName.toFullString(), menuItemByStringId.toFullString());
  }

  @Test
  @Ignore
  public void testQuickMenuClickMenuItemToCloseMenu() {
    QuickMenuItem fileMenuItem = driver.getQuickMenuItemByName("Browser View Menu");
    assertNotNull(fileMenuItem);
    Integer quickMenuItemListBefore = driver.getQuickMenuItemList().size();

    if (Platform.getCurrent() == Platform.MAC) {
      driver.pressQuickMenuItem("View", false);
      driver.waitForMenuItemPressed("View");
    }
    else
      fileMenuItem.click(SystemInputProtos.MouseInfo.MouseButton.LEFT, 1, Collections.<SystemInputProtos.ModifierPressed>emptyList());

    Integer quickMenuItemListAfter = driver.getQuickMenuItemList().size();

    assertTrue(quickMenuItemListBefore > quickMenuItemListAfter);
  }


  @Test
  public void testKeyAPIKeyPress() {
    Integer startWindowCount = driver.getQuickWindowCount();
    driver.keyPress("t", ctrlModifier);

    assertEquals(startWindowCount + 1, driver.getQuickWindowCount());
  }

  @Test
  public void testKeyAPIKeyDownKeyUp() {
    Integer startWindowCount = driver.getQuickWindowCount();
    driver.keyDown("w", ctrlModifier);
    driver.keyUp("w", ctrlModifier);

    assertEquals(startWindowCount - 1, driver.getQuickWindowCount());
  }

  // TODO: Some test to check if the modifiers don't "stick" would be appriopiate as well.

  @Test
  public void testOperaAction()
  {
    String expectedValue = "http://opera.fake/";
    driver.operaDesktopAction("Set Preference", 0, "Auto Update|Autoupdate Server=" + expectedValue, "");
    String newValue = driver.getPref("Auto Update", "Autoupdate Server");

    assertEquals(newValue, expectedValue);
  }


}
