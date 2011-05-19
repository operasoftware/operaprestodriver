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
package com.opera.core.systems.scope.services.ums;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;
import com.opera.core.systems.OperaUIElement;
import com.opera.core.systems.QuickMenu;
import com.opera.core.systems.QuickMenuItem;
import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.ScopeServices;

//import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfoList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo; //import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IDesktopUtils;

/**
 *
 * @author Adam Minchinton, Karianne Ekern
 *
 */

public class DesktopWindowManager extends AbstractService implements IDesktopWindowManager {

	private int activeWindowId = 0;
	private final SystemInputManager systemInputManager;
	private final IDesktopUtils desktopUtils;
	
	 public DesktopWindowManager(IDesktopUtils desktopUtils, SystemInputManager inputManager, ScopeServices services, String version) {
			super(services, version);

			this.systemInputManager = inputManager;
			this.desktopUtils = desktopUtils;
			String serviceName = "desktop-window-manager";

			if (!isVersionInRange(version, "3.0", serviceName)) {
				throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
			}

			services.setDesktopWindowManager(this);
		}

	 public void init() { }
	 
	 // Window

	 public int getActiveQuickWindowId() {
		Response response = executeCommand(DesktopWindowManagerCommand.GET_ACTIVE_WINDOW, null);
		DesktopWindowID.Builder builder = DesktopWindowID.newBuilder();
		buildPayload(response, builder);
		activeWindowId = builder.build().getWindowID();
		return activeWindowId;
	}

	public int getOpenQuickWindowCount() {
		List<DesktopWindowInfo> windows = getDesktopWindowInfoList();
		return windows.size();
	}
	
	// Note: This grabs the first window with a matching name, there might be more
	public QuickWindow getQuickWindow(QuickWidgetSearchType property, String value)
	{
		List<QuickWindow> windows = getQuickWindowList();
		for (QuickWindow window : windows) {
			if (property.equals(QuickWidgetSearchType.NAME)) {
				if (window.getName().equals(value)) {
					return window;
				}
			}
		}
		return null;
	}
	
	public List<QuickWindow> getQuickWindowList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_WINDOWS, null);
		DesktopWindowList.Builder builder = DesktopWindowList.newBuilder();
		builder.clear();
		buildPayload(response, builder);
		DesktopWindowList list = builder.build();

		List<DesktopWindowInfo> windowInfoList = list.getWindowListList();
		List<QuickWindow> windowList = new LinkedList<QuickWindow>();

		for (DesktopWindowInfo windowInfo : windowInfoList) {
			windowList.add(new QuickWindow(windowInfo));
		}
		return windowList;
	}
	
	// -- Window

	public List<DesktopWindowInfo> getDesktopWindowInfoList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_WINDOWS, null);
		DesktopWindowList.Builder builder = DesktopWindowList.newBuilder();
		buildPayload(response, builder);
		DesktopWindowList list = builder.build();

		List<DesktopWindowInfo> windowList = list.getWindowListList();
		return windowList;
	}

	public int getQuickWindowID(String name) {
		QuickWindow win = getQuickWindowByName(name);
		if (win != null) {
			return win.getWindowID();
		} else {
			return -1;
		}
	}

	public QuickWindow getQuickWindowByName(String name) {
		List<DesktopWindowInfo> windowList = getDesktopWindowInfoList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getName().equals(name)) {
				return new QuickWindow(window);
			}
		}
		return null;
	}

	public QuickWindow getQuickWindowById(int windowId) {
		List<DesktopWindowInfo> windowList = getDesktopWindowInfoList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getWindowID() == windowId) {
				return new QuickWindow(window);
			}
		}
		return null;
	}

	public String getQuickWindowName(int windowId) {
		QuickWindow window = getQuickWindowById(windowId);
		return (window == null ? "" : window.getName());
	}


	// Widget
	
	public QuickWidget getQuickWidget(QuickWidgetType type, int windowId, QuickWidgetSearchType property, String value)
	{
		return getQuickWidget(type, windowId, property, value, "");
	}

	// parentName is set to name, pos or text depending on widget.getParentType
	public QuickWidget getQuickWidget(QuickWidgetType type, int windowId, QuickWidgetSearchType property, String value, String parentName)
	{
		if (windowId < 0) {
			windowId = getActiveQuickWindowId();
		}
		
		List<QuickWidget> widgets = getQuickWidgetList(windowId);
		for (QuickWidget widget : widgets) {
			if (widget.getType() == type) {
				if (property.equals(QuickWidgetSearchType.NAME)) {
					if ((parentName.length() == 0 || widget.getParentName().equals(parentName))
							&& widget.getName().equals(value)) {
						return widget;
					}
				} else if (property.equals(QuickWidgetSearchType.TEXT)) {
					if ((parentName.length() == 0 || widget.getParentName().equals(parentName))
							&& widget.getText().trim().equals(value.trim())) {
						return widget;
					}
				}
			}
		}
		return null;
	}

	public QuickWidget getQuickWidgetByPos(QuickWidgetType type, int id, int row, int column) {
		return getQuickWidgetByPos(type, id, row, column, "");
	}

	public QuickWidget getQuickWidgetByPos(QuickWidgetType type, int windowId, int row, int column, String parentName)
	{
		if (windowId < 0) {
			windowId = getActiveQuickWindowId();
		}
		
		List<QuickWidget> widgets = getQuickWidgetList(windowId);
		for (QuickWidget widget : widgets) {
			if ((parentName.length() == 0 || widget.getParentName().equals(parentName))
					&& widget.getType() == type
					// Position is only set on tabbuttons and treeitems
					// thumbnails and buttons (in e.g. bookmarks bar)
					// so only look for these
					&& (widget.getType() == QuickWidgetType.TABBUTTON
					|| widget.getType() == QuickWidgetType.TREEITEM
					|| widget.getType() == QuickWidgetType.THUMBNAIL
					|| widget.getType() == QuickWidgetType.BUTTON)
					&& widget.getRow() == row && widget.getColumn() == column) {
				return widget;
			}
		}
		return null;
	}

	public List<QuickWidget> getQuickWidgetList(int windowId) {
		if (windowId <= 0) {
			windowId = getActiveQuickWindowId();
		}

		DesktopWindowID.Builder winBuilder = DesktopWindowID.newBuilder();
		winBuilder.clearWindowID();
		if (windowId >= 0) {
			winBuilder.setWindowID(windowId);
		} else {
			winBuilder.setWindowID(activeWindowId);
			windowId = activeWindowId;
		}

		Response response = executeCommand(DesktopWindowManagerCommand.LIST_QUICK_WIDGETS, winBuilder);
		QuickWidgetInfoList.Builder builder = QuickWidgetInfoList.newBuilder();
		builder.clear();
		buildPayload(response, builder);
		QuickWidgetInfoList list = builder.build();

		List<QuickWidgetInfo> widgetList = list.getQuickwidgetListList();
		List<QuickWidget> quickWidgetList = new LinkedList<QuickWidget>();

		for (QuickWidgetInfo widgetInfo : widgetList) {
			quickWidgetList.add(new QuickWidget(desktopUtils, systemInputManager, widgetInfo, windowId));
		}
		return quickWidgetList;
	}
	
	//  Menu

	public List<QuickMenu> getQuickMenuList() {
		List<QuickMenuInfo> menuInfoList = getQuickMenuInfoList();//list.getMenuListList();
		List<QuickMenu> menuList = new LinkedList<QuickMenu>();

		for (QuickMenuInfo menuInfo : menuInfoList) {
			menuList.add(new QuickMenu(menuInfo, desktopUtils, systemInputManager));
		}
		
		return menuList;
	}
	
	private List<QuickMenuInfo> getQuickMenuInfoList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_QUICK_MENUS, null);
		QuickMenuList.Builder builder = QuickMenuList.newBuilder();
		builder.clear();
		buildPayload(response, builder);
		QuickMenuList list = builder.build();

		List<QuickMenuInfo> menuInfoList = list.getMenuListList();
		return menuInfoList;
	}
	
	public List<QuickMenuItem> getQuickMenuItemList() {
		List<QuickMenuItem> list = new ArrayList<QuickMenuItem>();
		List<QuickMenuInfo> menus = getQuickMenuInfoList();
		for (QuickMenuInfo info : menus) {
			for (QuickMenuItemInfo infoItem : info.getMenuItemListList()) {
				QuickMenuItem item = new QuickMenuItem(infoItem, info.getMenuId().getMenuName(), desktopUtils, systemInputManager);
				list.add(item);
			}
		}
		return list;
	}
	
	
	// -- Menu
	
	public QuickMenu getQuickMenu(String menuName) {
		
		/*QuickMenuID.Builder idBuilder = QuickMenuID.newBuilder();
		idBuilder.clearMenuName();
		idBuilder.setMenuName(menuName);
		Response response = executeCommand(DesktopWindowManagerCommand.GET_QUICK_MENU, idBuilder);
		QuickMenuInfo.Builder builder = QuickMenuInfo.newBuilder();
		builder.clear();
		buildPayload(response, builder);
		QuickMenuInfo info = builder.build();
		return new QuickMenu(info, desktopUtils, systemInputManager);
		*/

		// Using ListMenus. GetMenu will raise error 
		List<QuickMenuInfo> list = getQuickMenuInfoList();
		for (QuickMenuInfo info : list) {
			if (info.getMenuId().getMenuName().equals(menuName)) {
				return new QuickMenu(info, desktopUtils, systemInputManager);		
			}
		}
		return null;
	}
	
	// Functions to get a specific QuickMenuItem -----------
	// These should be unique given one of action, submenu, text(?), shortcut
	// and unique within a single menu given either shortcutletter or pos
	
	public OperaUIElement getQuickMenuItemByAction(String action) {
		List<QuickMenuItem> itemList = getQuickMenuItemList();
		for (QuickMenuItem item : itemList) {
			if (item.getActionName().equals(action))
				return item;
		}
		return null;
	}
	
	public OperaUIElement getQuickMenuItemBySubmenu(String submenu) {
		List<QuickMenu> menus = getQuickMenuList();
		for (QuickMenu menu : menus) {
			List<QuickMenuItem> itemList = menu.getItemList();
			for (QuickMenuItem item : itemList) {
				if (item.getSubMenu().equals(submenu))
					return item;
			}
		}
		return null;
	}
	
	public OperaUIElement getQuickMenuItemByText(String text) {
		List<QuickMenu> menus = getQuickMenuList();
		for (QuickMenu menu : menus) {
			List<QuickMenuItem> itemList = menu.getItemList();
			for (QuickMenuItem item : itemList) {
				if (item.getText().replaceAll("&", "").equals(text))
					return item;
			}
		}
		return null;
	}
	
	public OperaUIElement getQuickMenuItemByAccKey(String key, String menuName) {
		List<QuickMenu> menus = getQuickMenuList();
		for (QuickMenu menu : menus) {
			List<QuickMenuItem> itemList = menu.getItemList();
			for (QuickMenuItem item : itemList) {
				if (item.getShortcutLetter().equalsIgnoreCase(key) && item.getMenu().equals(menuName))
					return item;
			}
		}
		return null;
	}
	
	public OperaUIElement getQuickMenuItemByShortcut(String shortcut){
		List<QuickMenu> menus = getQuickMenuList();
		for (QuickMenu menu : menus) {
			List<QuickMenuItem> itemList = menu.getItemList();
			for (QuickMenuItem item : itemList) {
				if (item.getShortcut().equals(shortcut))
					return item;
			}
		}
		return null;
	}

	
	public OperaUIElement getQuickMenuItemByPosition(int row, String menuName) {
		List<QuickMenu> menus = getQuickMenuList();
		for (QuickMenu menu : menus) {
			List<QuickMenuItem> itemList = menu.getItemList();
			for (QuickMenuItem item : itemList) {
				if (item.getRow() == row && item.getMenu().equals(menuName))
					return item;
			}
		}
		return null;
	}

	public OperaUIElement getQuickMenuItemByName(String name) {
		List<QuickMenu> menus = getQuickMenuList();
		for (QuickMenu menu : menus) {
			List<QuickMenuItem> itemList = menu.getItemList();
			for (QuickMenuItem item : itemList) {
				if (item.getActionName().equals(name) ||
						item.getSubMenu().equals(name))
					return item;
			}
		}
		return null;
	}
}
