/*
Copyright 2008-2012 Opera Software ASA

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

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemInfo;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

/**
 * Represents a menu item in a menu in the Opera desktop UI.
 */
public class QuickMenuItem extends OperaUIElement {

  private final QuickMenuItemInfo info;
  private final String menu;

  /**
   * Constructor.
   *
   * @param info QuickMenuItemInfo of the menu item
   */
  public QuickMenuItem(QuickMenuItemInfo info, String menu, IDesktopUtils desktopUtils,
                       SystemInputManager systemInputManager) {
    super(systemInputManager, desktopUtils);
    this.info = info;
    this.menu = menu;
  }

  /**
   * Get name of the menu. This is a constructed name, it is either - for items with an action, its
   * action - for items with an action that has a parameter (such as Open link, 'bookmarkurl')
   * "action, parameter" - for items that opens a submenu, its submenuname - for separators,
   * 'Separator'
   *
   * @return name of menuItem
   */
  public String getName() {
    if (isSeparator()) {
      return "Separator";
    }
    if (getActionName().length() > 0) {
      if (getActionParameter() != null && getActionParameter().length() > 0) {
        return getActionName() + ", " + getActionParameter();
      } else {
        return getActionName();
      }
    } else {
      return this.getSubMenu();
    }
  }

  /**
   * Get Action name for action associated with menu item, if any.
   *
   * @return action name of action associated with the item, returns empty string for an item with
   *         no associated action
   */
  public String getActionName() {
    return info.getAction();
  }

  /**
   * @return true if item opens a submenu
   */
  public boolean hasSubMenu() {
    return info.getSubmenu() != null && info.getSubmenu().length() > 0;
  }

  /**
   * @return String action parameter to the action of the menu item, if any
   */
  public String getActionParameter() {
    return info.getActionParam();
  }

  /**
   * @return name of submenu, if any
   */
  public String getSubMenu() {
    return info.getSubmenu();
  }


  /**
   * @return text of menu item
   */
  public String getText() {
    return getDesktopUtils().removeCR(info.getText().replaceAll("&", ""));
  }


  /**
   * Checks if menu item is enabled.
   *
   * @return true if enabled, else false
   */
  public boolean isEnabled() {
    return info.getEnabled();
  }

  /**
   * @return true if item is checked, else false
   */
  public boolean isChecked() {
    return info.getChecked();
  }

  /**
   * @return true if item is a separator item, else false
   */
  public boolean isSeparator() {
    return info.getSeparator();
  }


  /**
   * @return true if the item text is bold, else false
   */
  public boolean isBold() {
    return info.getBold();
  }

  /**
   * Gets the row of the widget within its parent,
   *
   * @return row of menu item within menu
   */
  public int getRow() {
    return info.getRow();
  }

  /**
   * @return ShortcutLetter, or null if none
   */
  public String getShortcutLetter() {
    return info.getShortcutLetter();
  }

  /**
   * @return shortcut of item
   */
  public String getShortcut() {
    return info.getShortcut();
  }


  /**
   * @return DesktopWindowRect of the menu item
   */
  public DesktopWindowRect getRect() {
    return info.getRect();
  }

  @Override
  public String toString() {
    //TODO: What field to use if no action?
    String str = isSeparator() ? "Separator" :
                 ((getActionName() != null && getActionName().length() > 0) ? getActionName()
                                                                            : getSubMenu());
    return "QuickMenuItem " + str; //getActionName();
  }

  public String toFullString() {
    return "QuickMenuItem\n" +
           "       Item name: " + getName() + "\n"
           + "       text: " + getText() + "\n"
           + "    enabled: " + isEnabled() + "\n"
           + "          x: " + getRect().getX() + "\n"
           + "          y: " + getRect().getY() + "\n"
           + "      width: " + getRect().getWidth() + "\n"
           + "     height: " + getRect().getHeight() + " \n";
  }

  /**
   * Get name of containing menu
   *
   * @return name of the menu the menuItem is in
   */
  public String getMenu() {
    return menu;
  }

}