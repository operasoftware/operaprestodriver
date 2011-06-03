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

package com.opera.core.systems.scope.services;

import java.util.List; //import java.util.concurrent.atomic.AtomicInteger;

import com.opera.core.systems.OperaUIElement;
import com.opera.core.systems.QuickMenu;
import com.opera.core.systems.QuickMenuItem;
import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;

public interface IDesktopWindowManager {

	/**
	 * Does initalization neccessary to set up the service.
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
	 * Gets window id of window with the given name
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
	 * @param windowId window id of the window containing the widget
	 * @param property property specified for the search
	 * @param value value of property given
	 * @return QuickWidget matching the property, or null if no such widget
	 */
	QuickWidget getQuickWidget(QuickWidgetType type, int windowId, QuickWidgetSearchType property, String value);

	/**
	 * Gets the quick widget in a specified window based on a specific
	 * property and its parent widgets name.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param property - property specified for the search
	 * @param value - value of property given
	 * @param parentName - Name of the widgets parent widget
	 * @return QuickWidget matching the property, or null if no such widget
	 */
	QuickWidget getQuickWidget(QuickWidgetType type, int windowId, QuickWidgetSearchType property, String value, String parentName);

	/**
	 * Gets the quick widget based on its position.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param row - row of the widget
	 * @param column - column of the widget
	 * @return QuickWidget matching the parameters, or null if no such widget
	 */
	QuickWidget getQuickWidgetByPos(QuickWidgetType type, int windowId, int row, int column);

	/**
	 * Gets the quick widget based on its position and
	 * its parent widgets name.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param row - row of the widget
	 * @param column - column of the widget
	 * @param parentName - Name of the widgets parent widget
	 * @return QuickWidget matching the parameters, or null if no such widget
	 */
	QuickWidget getQuickWidgetByPos(QuickWidgetType type, int windowId, int row, int column, String parentName);

	/**
	 * Gets the quick window based on a specific property.
	 *
	 * @param property - property to search by
	 * @param value - value of property given
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

	void pressQuickMenuItem(String menuItemText);

	// TODO: DOCUMENT
	
	/**
	 * Gets the menu with name name.
	 *
	 * @param name menu name
	 * @return QuickMenu
	 */
	QuickMenu getQuickMenu(String menuName);
	
	OperaUIElement getQuickMenuItemByAction(String action);

	OperaUIElement getQuickMenuItemByText(String text);

	OperaUIElement getQuickMenuItemByPosition(int row, String parentName);

	OperaUIElement getQuickMenuItemByAccKey(String key, String parentName);

	OperaUIElement getQuickMenuItemByShortcut(String shortcut);

	OperaUIElement getQuickMenuItemBySubmenu(String submenu);

	List<QuickMenu> getQuickMenuList();

	List<QuickMenuItem> getQuickMenuItemList();

	OperaUIElement getQuickMenuItemByName(String name);
	
}
