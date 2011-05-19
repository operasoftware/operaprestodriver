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
*/package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

/**
 * Represents a menu in the Opera desktop UI.
 *
 * @author Karianne Ekern.
 *
 */
public class QuickMenu {
		private final QuickMenuInfo info;
		private final IDesktopUtils desktopUtils;
		private final SystemInputManager systemInputManager;
		// Would need id to be able to distinguish two main menus in different windows
		//private final int parentWindowId;
		
		// The list of items is not initialized till the first call to get the list
		// private ArrayList<QuickMenuItem> menuItemList;

		/**
		 * Constructor.
		 *
		 * @param desktopUtils
		 * @param inputManager
		 * @param info QuickMenuInfo of the menu
		 */
		public QuickMenu(QuickMenuInfo info, IDesktopUtils desktopUtils, SystemInputManager inputManager) {
	        this.info = info;
	        this.desktopUtils = desktopUtils;
	        this.systemInputManager = inputManager;
	        //this.menuItemList = new ArrayList<QuickMenuItem>();
	    }

		/**
	     * Get the name of the menu (as represented in e.g. standard_menu.ini)
	     *
	     * @return the name of the menu
	     */
		public String getName() {
			return info.getMenuId().getMenuName();
		}
		
		public List<QuickMenuItem> getItemList() {
			List<QuickMenuItem> itemList = new ArrayList<QuickMenuItem>();
			for (QuickMenuItemInfo itemInfo : info.getMenuItemListList()) {
				QuickMenuItem item = new QuickMenuItem(itemInfo, getName(), desktopUtils, systemInputManager);
				
				itemList.add(item);
			}
			return itemList;
		}
		
		/**
		 * @return true if the menu is visible, else false
		 */
		public boolean isVisible(){
			return true; //info.getVisible();
		}
		
		/**
		 * 
		 * @return true if this menu has one or more submenus, else false
		 */
		public boolean hasSubmenu() {
			// Checks items in menu for submenu, return true
			// on first true else false if none.
			List<QuickMenuItem> items = getItemList();
			for (QuickMenuItem item : items) {
				if (item.hasSubMenu())
					return true;
			}
			return false;
		}
		
		/**
		 * @return true if this is a submenu, else false
		 *
		public boolean isSubmenu() {
			return info.getParentMenu() != null;
		}*/

		/**
		 * @return DesktopWindowRect of the menu
		 */
		public DesktopWindowRect getRect() {
			return info.getRect();
		}
		
		/////////////////////////////////////////

		// TODO: FIXME: Move to common to widget/menu
		/**
		 * @return Point describing location of widget
		 */
		/*public Point getLocation() {
			DesktopWindowRect rect = getRect();
			return new Point(rect.getX(), rect.getY());
		}*/

		/**
		 * @return size of widget
		 */
		/*public Dimension getSize() {
			DesktopWindowRect rect = getRect();
			return new Dimension(rect.getWidth(), rect.getHeight());
		}*/

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



