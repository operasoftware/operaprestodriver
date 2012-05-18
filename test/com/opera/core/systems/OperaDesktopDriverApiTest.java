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
import com.opera.core.systems.scope.protos.SystemInputProtos;
import org.junit.*;
import org.openqa.selenium.Platform;
import java.util.*;

import static org.junit.Assert.*;

/*
The OperaDesktopDriverTest* classes try hard to use the desktop driver in the very same way
as the desktopwatir RSpec suite does. The goal is to try to test the behavior when
the desktop driver is used by the desktopwatir suite, i.e. mimic the everyday use of the
desktop driver that is not used as it is but rather via the desktopwatir framework.

In order to be able to do that, you need to have a working Opera installation on the
system that you are running the tests on. The binary is looked for by the OperaPaths class.
*/
public class OperaDesktopDriverApiTest extends OperaDesktopDriverTestCase {
  /**
   * Start API tests, run a new browser, check that it is running.
   */
  @Test
  public void testStartBrowser() {
    setOperaPath();
    driver = new TestOperaDesktopDriver(getDefaultCaps());
    browserPid = driver.getPid();

    assertTrue(driver.isRunning());
  }

  /**
   * The tests using the 'text' property of anything, and also the string tests, depend on the
   * language setting of Opera.
   * Since there doesn't seem to be any API in the OperaDesktopDriver to
   * control the language or even check it, we use the prefs API to validate that
   * the language file in use is "en.lng". Not the best option, this should be
   * approached with some new language control API perhaps.
   */
  @Test
  public void testVerifyLanguageFileIsEnLng() {
    String languageFile = (String) driver.preferences().get("User Prefs", "Language File").getValue();

    assertNotNull(languageFile);
    assertTrue("The language file should be 'en.lng'", languageFile.endsWith("en.lng"));
  }

  /**
   * The testQuickWindowApi*() tests are supposed to test the Quick Window API of the
   * OperaDesktopDriver.
   * The API seems a bit inconsistent.
   * The API should be redesigned and refactored by the desktop team.
   * The tests below should be adapted afterwards.
   */

  /**
   * Check that getActiveQuickWindowID() returns SOME window.
   */
  @Test
  public void testQuickWindowApiGetActiveQuickWindowID() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();

    assertTrue("getActiveQuickWindowID() should be > 0.", activeQuickWindowId > 0);
  }

  /**
   * Check that the window count reported by the driver looks fine.
   */
  @Test
  public void testQuickWindowApiGetWindowCount() {
    Integer quickWindowCount = driver.getQuickWindowCount();

    assertTrue("getQuickWindowCount() should be > 0.", quickWindowCount > 0);
  }

  /**
   * Check that getQuickWindowList() reports the same number of windows as getQuickWindowCount().
   */
  @Test
  public void testQuickWindowApiGetQuickWindowListSize() {
    List<QuickWindow> quickWindowList = driver.getQuickWindowList();
    Integer quickWindowCount = driver.getQuickWindowCount();
    Integer quickWindowListSize = quickWindowList.size();

    assertTrue("getQuickWindowList().size() should == getQuickWindowCount()", quickWindowListSize == quickWindowCount);
  }

  /**
   * Check that getQuickWindowName() works. Silently assumes that getQuickWindowList() returns sane data.
   */
  @Test
  public void testQuickWindowApiGetQuickWindowName() {
    List<QuickWindow> quickWindowList = driver.getQuickWindowList();

    for (QuickWindow w: quickWindowList) {
      String nameById = driver.getQuickWindowName(w.getWindowID());

      assertEquals(w.getName(), nameById);
    }
  }

  /**
   * Check that findWindowById() works. Silently assumes that getQuickWindowList() returns sane data.
   */
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

  /**
   * The testQuickWidgetApi*() methods attempt to verify the Quick Widget API of
   * OperaDesktopDriver.
   *
   * The API should be looked into and refactored.
   *
   * The tests assume the there is a following widget available after starting Opera
   * with a clean profile:
   *
   * Name: tbb_Stop_Reload
   * Type: BUTTON
   * Text: Stop
   * Visible: true
   */

  /**
   * Verify that findWidgetByName() works OK.
   */
  @Test
  public void testQuickWidgetApiFindWidgetByName() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    QuickWidget widgetByName = driver.findWidgetByName(DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON, activeQuickWindowId, "tbb_Stop_Reload");

    assertNotNull(widgetByName);
    assertEquals("Stop", widgetByName.getText());
  }

  /**
   * Verify that findWidgetByText() works OK.
   */
  @Test
  public void testQuickWidgetApiFindWidgetByText() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    QuickWidget widgetByText = driver.findWidgetByText(DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON, activeQuickWindowId, "Stop");

    assertNotNull(widgetByText);
    assertEquals("tbb_Stop_Reload", widgetByText.getName());
  }

  /**
   * Verify that findWidgetByStringId() works OK.
   */
  @Test
  public void testQuickWidgetApiFindWidgetByStringId() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    QuickWidget widgetByByStringId = driver.findWidgetByStringId(DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON, activeQuickWindowId, "SI_STOP_BUTTON_TEXT");

    assertNotNull(widgetByByStringId);
    assertEquals("tbb_Stop_Reload", widgetByByStringId.getName());
  }

  /**
   * Um, verify that findWidgetByPosition() works OK.
   * Will probably be possible in future Opera releases.
   */
  @Test
  @Ignore
  public void testQuickWidgetApiFindWidgetByPosition() {
    // TODO: Needs an implementation
  }

  /**
   * Verify that getQuickWidgetList() returns any widgets for the active window.
   */
  @Test
  public void testQuickWidgetApiGetQuickWidgetListSize() {
    Integer activeQuickWindowId = driver.getActiveQuickWindowID();
    List<QuickWidget> quickWidgetList = driver.getQuickWidgetList(activeQuickWindowId);
    
    assertTrue(quickWidgetList.size() > 0);
  }

  /**
   * Verify that each widget from the list returned by getQuickWidgetList() contains the very
   * same information when fetched by findWidgetByName().
   *
   * This test is known to fail, the API should be fixed by the desktop team.
   */
  @Test
  @Ignore
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

  /**
   * Verify that getString() works fine for fetching a string with the ampersand.
   */
  @Test
  public void testStringApiGetStringAmp() {
    String fetchedAmpStringAmp = driver.getString("SI_IDM_SEARCH_ENGINE6", false);

    assertNotNull(fetchedAmpStringAmp);
    assertEquals("Price &comparison", fetchedAmpStringAmp);
  }

  /**
   * Verify that getString() works fine for stripping the ampersand.
   */
  @Test
  public void testStringApiGetStringNoAmp() {
    String fetchedAmpStringAmp = driver.getString("SI_IDM_SEARCH_ENGINE6", true);

    assertNotNull(fetchedAmpStringAmp);
    assertEquals("Price comparison", fetchedAmpStringAmp);
  }

  /**
   * Verify that getSubstitutedString() behaves as expected when no subsitution arguments are given.
   */
  @Test
  public void testStringApiGetSubstitutedStringNoParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS"};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded %1 of %2 at %3", fetchedStringFormattersRaw);
  }

  /**
   * Verify that getSubstitutedString() behaves as expected when all the substitution arguments are given.
   */
  @Test
  public void testStringApiGetSubstitutedStringAllParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "one", "two", "three"};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded one of two at three", fetchedStringFormattersRaw);
  }

  /**
   * Verify that getSubstitutedString() behaves as expected when all of the substitution arguments are empty.
   */
  @Test
  public void testStringApiGetSubstitutedStringAllAnyParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "", "", ""};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded _ANY_ of _ANY_ at _ANY_", fetchedStringFormattersRaw);
  }

  /**
   * Verify that getSubstitutedString() behaves as expected when some of the substitution arguments are empty.
   */
  @Test
  public void testStringApiGetSubstitutedStringSomeAnyParams() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "one", "", "three"};
    String fetchedStringFormattersRaw = driver.getSubstitutedString(inputParam, true);

    assertNotNull(fetchedStringFormattersRaw);
    assertEquals("Downloaded one of _ANY_ at three", fetchedStringFormattersRaw);
  }

  /**
   * Verify that getSubstitutedString() behaves as expected for ordered substitution.
   * Not possible currently since there is no string in Opera that would have the orderred formatters not in order
   * (i.e. "Something %2 somethingelse %1").
   */
  @Test
  public void testStringApiGetSubstitutedStringSubstitutionOrder() {
    // TODO: Implement.
  }

  @Test
  public void testKeyAPIKeyPress() {
    Integer startWindowCount = driver.getQuickWindowCount();
    driver.keyPress("t", ctrlModifier);
    driver.waitForWindowActivated();

    assertEquals(startWindowCount + 1, driver.getQuickWindowCount());
  }

  @Test
  public void testKeyAPIKeyDownKeyUp() {
    Integer startWindowCount = driver.getQuickWindowCount();
    driver.keyDown("w", ctrlModifier);
    driver.keyUp("w", ctrlModifier);
    driver.waitForWindowClose();

    assertEquals(startWindowCount - 1, driver.getQuickWindowCount());
  }

  // TODO: Some test to check if the modifiers don't "stick" would be appriopiate as well.

  @Test
  public void testOperaAction()
  {
    String expectedValue = "http://opera.fake/";
    driver.operaDesktopAction("Set Preference", 0, "Auto Update|Autoupdate Server=" + expectedValue, "");
    String newValue = (String) driver.preferences().get("Auto Update", "Autoupdate Server").getValue();

    assertEquals(newValue, expectedValue);
  }

  @Test
  public void testQuickMenuApiGetQuickMenuList()
  {
    // Show the top menu bar, right after a fresh start it is disabled in favor of
    // the "O-menu button".
    driver.preferences().set("User Prefs", "Show Menu", 1);

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
    Integer desiredPosition = null;
    if (Platform.getCurrent() == Platform.MAC)
      desiredPosition = 10;
    else
      desiredPosition = 9;

    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByPosition = driver.getQuickMenuItemByPosition(desiredPosition, "Browser View Menu");

    assertEquals(menuItemByName.toFullString(), menuItemByPosition.toFullString());
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByAccKey() {
    String itemName = null;
    String accKey = null;
    switch (Platform.getCurrent()) {
      case MAC:
        break;
      case WINDOWS:
      case XP:
      case VISTA:
        itemName = "Enter fullscreen";
        accKey = "f";
        break;
      case UNIX:
      case LINUX:
        break;
      case ANDROID:
        break;
      default:
      case ANY:
        assertNotNull("Unknown OS", null);
        break;
    }

    if (itemName != null && accKey != null)
    {
      QuickMenuItem menuItemByName = driver.getQuickMenuItemByName(itemName);
      QuickMenuItem menuItemByAccKey = driver.getQuickMenuItemByAccKey(accKey, "Browser View Menu");

      assertEquals(menuItemByName.toFullString(), menuItemByAccKey.toFullString());
    }
    else
    {
      // Well, not implemented for this OS.
    }
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByShortcut() {
    String itemName = null;
    String shortcutKey = null;
    switch (Platform.getCurrent()) {
      case MAC:
        break;
      case WINDOWS:
      case XP:
      case VISTA:
        itemName = "Enter fullscreen";
        shortcutKey = "F11";
        break;
      case UNIX:
      case LINUX:
        break;
      case ANDROID:
        break;
      default:
      case ANY:
        assertNotNull("Unknown OS", null);
        break;
    }

    if (itemName != null && shortcutKey != null)
    {
      QuickMenuItem menuItemByName = driver.getQuickMenuItemByName(itemName);
      QuickMenuItem menuItemByShortcut = driver.getQuickMenuItemByShortcut(shortcutKey);

      assertEquals(menuItemByName.toFullString(), menuItemByShortcut.toFullString());
    }
    else
    {
      // Well, not implemented for this OS.
    }
  }

  @Test
  public void testQuickMenuApiGetQuickMenuItemByStringId() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    QuickMenuItem menuItemByStringId = driver.getQuickMenuItemByStringId("D_DEVTOOLS_TITLE");

    assertEquals(menuItemByName.toFullString(), menuItemByStringId.toFullString());
  }

  @Test
  public void testCloseMenuAfterwards() {
    driver.keyPress("Esc", Collections.<SystemInputProtos.ModifierPressed>emptyList());
    driver.waitForMenuClosed("Browser View Menu");
  }
}
