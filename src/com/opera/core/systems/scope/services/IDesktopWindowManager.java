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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.QuickMenu;
import com.opera.core.systems.QuickMenuItem;
import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;

import java.util.List;

/**
 * @author Adam Minchinton <adamm@opera.com>, Karianne Ekern <karie@opera.com>
 */
public interface IDesktopWindowManager {

  /**
   * Does initialization necessary to set up the service.
   */
  void init();

  /**
   * Gets id of active window.
   *
   * @return id of active window
   */
  int getActiveQuickWindowId();

  /**
   * Gets number of open windows.
   *
   * @return number of open windows
   */
  int getOpenQuickWindowCount();

  /**
   * Gets list of all QuickWidgets in the window.
   *
   * @param windowId id of window
   * @return list of QuickWidgets in the window given by id id
   */
  List<QuickWidget> getQuickWidgetList(int windowId);

  /**
   * Gets window id of window with the given name.
   *
   * @param name window name
   * @return id of window with the given name
   */
  int getQuickWindowID(String name);

  /**
   * Gets a list of all open windows.
   *
   * @return list of all open windows
   */
  List<QuickWindow> getQuickWindowList();

  /**
   * Gets the name of the window with windowId.
   *
   * @param windowId id of window
   * @return name of the window
   */
  String getQuickWindowName(int windowId);

  /**
   * Gets a QuickWidget based on a specific property.
   *
   * @param type     what type
   * @param windowId window id of the window containing the widget
   * @param property property specified for the search
   * @param value    value of property given
   * @return QuickWidget matching the property, or null if no such widget
   */
  QuickWidget getQuickWidget(QuickWidgetType type, int windowId, QuickWidgetSearchType property,
                             String value);

  /**
   * Gets the quick widget in a specified window based on a specific property and its parent widgets
   * name.
   *
   * @param type       what type
   * @param windowId   window id of the window containing the widget
   * @param property   property specified for the search
   * @param value      value of property given
   * @param parentName Name of the widgets parent widget
   * @return QuickWidget matching the property, or null if no such widget
   */
  QuickWidget getQuickWidget(QuickWidgetType type, int windowId, QuickWidgetSearchType property,
                             String value, String parentName);

  /**
   * Gets the quick widget based on its position.
   *
   * @param type     what type
   * @param windowId window id of the window containing the widget
   * @param row      row of the widget
   * @param column   column of the widget
   * @return QuickWidget matching the parameters, or null if no such widget
   */
  QuickWidget getQuickWidgetByPos(QuickWidgetType type, int windowId, int row, int column);

  /**
   * Gets the quick widget based on its position and its parent widgets name.
   *
   * @param type       what type
   * @param windowId   window id of the window containing the widget
   * @param row        row of the widget
   * @param column     column of the widget
   * @param parentName Name of the widgets parent widget
   * @return QuickWidget matching the parameters, or null if no such widget
   */
  QuickWidget getQuickWidgetByPos(QuickWidgetType type, int windowId, int row, int column,
                                  String parentName);

  /**
   * Gets the quick window based on a specific property.
   *
   * @param property - property to search by
   * @param value    - value of property given
   * @return QuickWindow matching the parameters, or null if no such widget
   */
  QuickWindow getQuickWindow(QuickWidgetSearchType property, String value);

  /**
   * Gets the quick window with the given window id.
   *
   * @param windowId id of the window
   * @return QuickWindow with id windowId, if any, else null.
   */
  QuickWindow getQuickWindowById(int windowId);

  /**
   * Gets the window with name name.
   *
   * @param name window name
   * @return QuickWindow
   */
  QuickWindow getQuickWindowByName(String name);

  void pressQuickMenuItem(String menuItemText, boolean popMenu);

  /**
   * Gets the menu with name name.
   *
   * @param menuName the menu's name
   * @return QuickMenu
   */
  QuickMenu getQuickMenu(String menuName);

  /**
   * Gets the menu with the action specified, if any.
   *
   * @param action of the menu item to search for
   * @return QuickMenuItem with action, or null if the item doesn't exist
   */
  QuickMenuItem getQuickMenuItemByAction(String action);

  /**
   * Gets the menu with the text specified, if any.
   *
   * @param text of the menu item to search for
   * @return QuickMenuItem with the given text, or null if no such item exists
   */
  QuickMenuItem getQuickMenuItemByText(String text);

  /**
   * Gets the menu item with the given position in the menu with name menuName.
   *
   * @param row      the row of the item, counting from zero, and including both normal items and
   *                 menu separators
   * @param menuName name of the menu to lookup the menu item in
   * @return QuickMenuItem, or null if the item doesn't exist
   */
  QuickMenuItem getQuickMenuItemByPosition(int row, String menuName);

  /**
   * Gets the menu item with the given accelerator key in the menu with name menuName.
   *
   * @param key      the shortcut key of the menu item to search for (the character that's
   *                 underlined/marked in the menu item text)
   * @param menuName name of the menu of the menu item
   * @return QuickMenuItem or null if the item doesn't exist
   */
  QuickMenuItem getQuickMenuItemByAccKey(String key, String menuName);

  /**
   * Gets the Menu item with the given shortcut.
   *
   * @param shortcut string representation of the shortcut of the menuitem (i.e. "Ctrl+O")
   * @return QuickMenuItem, or null if the item doesnt exist
   */
  QuickMenuItem getQuickMenuItemByShortcut(String shortcut);

  /**
   * Gets the menu item that opens the sub-menu.
   *
   * @param submenu the name of the sub-menu the menu item opens when its selected/clicked
   * @return QuickMenuItem or null if the item doesn't exist
   */
  QuickMenuItem getQuickMenuItemBySubmenu(String submenu);

  /**
   * @return list of all menus that are open (including menu bar(s))
   */
  List<QuickMenu> getQuickMenuList();

  /**
   * @return list containing all menu items in open menus (including menu bar(s))
   */
  List<QuickMenuItem> getQuickMenuItemList();

  /**
   * @param name name of the item. This is either the name of the action the item executes when
   *             selected The name of the sub-menu the item opens when selected "Separator" for
   *             items that are separators
   * @return the menu item
   */
  QuickMenuItem getQuickMenuItemByName(String name);

  /**
   * @param menuName name of the menu (as specified in standard_menu.ini)
   * @param windowId id of the window the menu is attached to (note: only makes sense for menu bar)
   * @return QuickMenu with name menuName in windowId if it exists, else null
   */
  QuickMenu getQuickMenu(String menuName, int windowId);

  /**
   * @param name      Name of the menuItem
   * @param window_id windowId of the window the menu of the item is in (note: only makes sense for
   *                  menu bar items)
   * @return the menu item
   */
  QuickMenuItem getQuickMenuItemByName(String name, int window_id);

}