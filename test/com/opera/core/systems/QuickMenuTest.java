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

import com.opera.core.systems.scope.protos.SystemInputProtos;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDesktopDriverTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.Platform.ANDROID;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.MAC;

@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class QuickMenuTest extends OperaDesktopDriverTestCase {

  @Before
  public void setupEnvironment() {
    // Show the top menu bar, right after a fresh start it is disabled in favour of the "O-menu
    // button".
    driver.preferences().set("User Prefs", "Show Menu", true);
  }

  @Test
  public void quickMenuList() {
    List<QuickMenu> menus = driver.getQuickMenuList();
    assertEquals(1, menus.size());
    assertEquals("Main Menu", menus.get(0).getName());
  }

  @Test
  public void quickMenu() {
    List<QuickMenu> menus = driver.getQuickMenuList();
    QuickMenu mainMenuByList = menus.get(0);
    QuickMenu mainMenuGetQuickMenu = driver.getQuickMenu("Main Menu");

    assertNotNull(mainMenuByList);
    assertNotNull(mainMenuGetQuickMenu);
    assertEquals(mainMenuByList.toFullString(), mainMenuGetQuickMenu.toFullString());
  }

  @Test
  public void quickMenuItemByName() {
    assertNotNull(driver.getQuickMenuItemByName("Browser Tools Menu"));
  }

  @Test
  @Ignore(value = "Fails when mouse is not moved into browser viewport")
  public void clickMenuItemToOpenMenu() {
    QuickMenuItem fileMenuItem = driver.getQuickMenuItemByName("Browser View Menu");
    assertNotNull(fileMenuItem);

    Integer quickMenuItemListBefore = driver.getQuickMenuItemList().size();

    switch (currentPlatform()) {
      case MAC:
        driver.pressQuickMenuItem("View", false);
        driver.waitForMenuItemPressed("View");
        break;

      default:
        fileMenuItem.click(SystemInputProtos.MouseInfo.MouseButton.LEFT, 1,
                           Collections.<SystemInputProtos.ModifierPressed>emptyList());
    }

    assertTrue("Expected list to have grown",
               quickMenuItemListBefore < driver.getQuickMenuItemList().size());
  }

  @Test
  public void quickMenuItemByAction() {
    QuickMenuItem menuItemByName = driver.getQuickMenuItemByName("Show popup menu, Developer Menu");
    assertNotNull(menuItemByName);
    QuickMenuItem menuItemByAction = driver.getQuickMenuItemByAction("Show popup menu");
    assertNotNull(menuItemByAction);
    assertEquals(menuItemByName.toFullString(), menuItemByAction.toFullString());
  }

  @Test
  public void quickMenuBySubmenu() {
    assertEquals(driver.getQuickMenuItemByName("Show popup menu, Developer Menu").toFullString(),
                 driver.getQuickMenuItemBySubmenu("Developer Menu").toFullString());
  }

  @Test
  public void quickMenuItemByText() {
    assertEquals(driver.getQuickMenuItemByName("Show popup menu, Developer Menu").toFullString(),
                 driver.getQuickMenuItemByText("Developer Tools").toFullString());
  }

  @Test
  public void quickMenuItemByPosition() {
    assertEquals(driver.getQuickMenuItemByName("Show popup menu, Developer Menu").toFullString(),
                 driver.getQuickMenuItemByPosition(currentPlatform().is(MAC) ? 10 : 9,
                                                   "Browser View Menu").toFullString());
  }

  @Test
  @Ignore(platforms = {LINUX, ANDROID, MAC})
  public void quickMenuItemByAccKey() {
    assertEquals(driver.getQuickMenuItemByName("Enter fullscreen").toFullString(),
                 driver.getQuickMenuItemByAccKey("f", "Browser View Menu").toFullString());
  }

  @Test
  @Ignore(platforms = {LINUX, ANDROID, MAC})
  public void quickMenuItemByShortcut() {
    assertEquals(driver.getQuickMenuItemByName("Enter fullscreen").toFullString(),
                 driver.getQuickMenuItemByShortcut("F11").toFullString());
  }

  @Test
  public void waitForMenuClosed() {
    driver.keyPress("Esc", Collections.<SystemInputProtos.ModifierPressed>emptyList());
    driver.waitForMenuClosed("Browser View Menu");
  }

}