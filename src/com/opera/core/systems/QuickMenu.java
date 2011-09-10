/*
Copyright 2008-2011 Opera Software ASA

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

import java.util.List;
import java.util.ArrayList;

import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

/**
 * Represents a menu in the Opera desktop UI.
 *
 * @author Karianne Ekern.
 */
public class QuickMenu {

  private final QuickMenuInfo info;
  private final IDesktopUtils desktopUtils;
  private final SystemInputManager systemInputManager;

  /**
   * Constructor.
   *
   * @param info QuickMenuInfo of the menu
   */
  public QuickMenu(QuickMenuInfo info, IDesktopUtils desktopUtils,
                   SystemInputManager inputManager) {
    this.info = info;
    this.desktopUtils = desktopUtils;
    this.systemInputManager = inputManager;
  }

  /**
   * Get the name of the menu (as represented in e.g. standard_menu.ini)
   *
   * @return the name of the menu
   */
  public String getName() {
    return info.getMenuId().getMenuName();
  }

  /**
   * Get the windowId of the window the Menu is attached to. Note: Only set for menubars, and only
   * on windows and *nix.
   *
   * @return window id
   */
  public int getParentWindowId() {
    return info.getWindowId().getWindowID();
  }

  /**
   * @return List of QuickMenuItems in this menu
   */
  public List<QuickMenuItem> getItemList() {
    List<QuickMenuItem> itemList = new ArrayList<QuickMenuItem>();
    for (QuickMenuItemInfo itemInfo : info.getMenuItemListList()) {
      QuickMenuItem item = new QuickMenuItem(itemInfo, getName(), desktopUtils, systemInputManager);

      itemList.add(item);
    }
    return itemList;
  }

  /**
   * @return true if this menu has one or more submenus, else false
   */
  public boolean hasSubmenu() {
    // Checks items in menu for submenu, return true
    // on first true else false if none.
    List<QuickMenuItem> items = getItemList();
    for (QuickMenuItem item : items) {
      if (item.hasSubMenu()) {
        return true;
      }
    }
    return false;
  }


  /**
   * @return DesktopWindowRect of the menu
   */
  public DesktopWindowRect getRect() {
    return info.getRect();
  }

  @Override
  public String toString() {
    return "QuickMenu " + getName();
  }

  public String toFullString() {
    return "QuickMenu\n" +
           "       Menu name: " + getName() + "\n"
           + "          x: " + getRect().getX() + "\n"
           + "          y: " + getRect().getY() + "\n"
           + "      width: " + getRect().getWidth() + "\n"
           + "     height: " + getRect().getHeight() + " \n";
  }

}



