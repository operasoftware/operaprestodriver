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

import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

/**
 * Represents a menuitem in a menu in the Opera desktop UI.
 *
 * @author Karianne Ekern.
 *
 */
public class QuickMenuItem {
		private final QuickMenuItemInfo info;
		private final String menu;
		private SystemInputManager systemInputManager;
		private final IDesktopUtils desktopUtils;
		
		
		/**
		 * Constructor.
		 *
		 * @param info QuickMenuItemInfo of the menu item
		 */
		public QuickMenuItem(QuickMenuItemInfo info, String menu,IDesktopUtils desktopUtils, SystemInputManager systemInputManager) {
	        this.info = info;
	        this.menu = menu;
	        this.systemInputManager = systemInputManager;
	        this.desktopUtils = desktopUtils;
	    }

		/**
		 * 
		 * @return actionName or SubmenuName of menuItem
		 */
		public String getName() {
			if (getActionName().length() > 0)
				return getActionName();
			else
				return this.getSubMenu();
		}

		/**
		 * Get Action name for action associated with menu item, if any.
		 * 
		 * @return action name of action associated with the item,
		 *         returns empty string for an item with no associated action
		 */
		public String getActionName() {
			return info.getAction();
		}
		
		/**
		 * 
		 * @return true if item opens a submenu
		 */
		public boolean hasSubMenu() {
			return info.getSubmenu() != null;
		}
		
		/**
		 * 
		 * @return name of submenu, if any 
		 */
		public String getSubMenu() {
			return info.getSubmenu();
		}
		
		
		/**
		 * 
		 * @return StringId of menu item
		 */
	    public String getStringId() {
	    	 return info.getStringId();
	     }
	    
		/**
	     *
	     * @return (language dependent) text of menuitem
	     */
		public String getText() {
			// TODO: Should remove &?
			return desktopUtils.removeCR(info.getText());
		}
		

		/**
		 * Checks if menuitem is enabled.
		 *
		 * @return true if enabled, else false
		 */
		public boolean isEnabled() {
			return info.getEnabled();
		}

		/**
		 *
		 * @return true if the menuentry is selected, else false
		 */
		public boolean isSelected() {
			return info.getSelected();
		}

		/**
		 *
		 * @return true if item is checked, else false
		 */
		public boolean isChecked() {
			return info.getChecked();
		}

		
		/**
		 * @return true if menuentry is visible, else false
		 */
		public boolean isVisible(){
			return true;//info.getVisible();
		}
		
		public boolean isBold() {
			return info.getBold();
		}
		
		/**
		 * Gets the row of the widget within its parent,
		 *
		 * @return row of menuitem within menu
		 */
		public int getRow() {
			return info.getRow();
		}
		
		/**
		 * 
		 * @return ShortcutLetter, or null if none
		 */
		public String getShortcutLetter() {
			return info.getShortcutletter();
		}
		
		public String getShortcut() {
			return info.getShortcut();
		}
		
		/**
		 * @return DesktopWindowRect of the menuitem
		 */
		public DesktopWindowRect getRect() {
			return info.getRect();
		}

		// TODO: FIXME: ALl those functions just using the rect can be moved to common
		// between menu, menuitem and widget
		
		/**
		 * @return Point describing location of this menuitem
		 */
		public Point getLocation() {
			DesktopWindowRect rect = getRect();
			return new Point(rect.getX(), rect.getY());
		}

		/**
		 * @return size of menuitem
		 */
		public Dimension getSize() {
			DesktopWindowRect rect = getRect();
			return new Dimension(rect.getWidth(), rect.getHeight());
		}

		@Override
		public String toString() {
			//TODO: FIXME: What field to use if no action?
			String str = getActionName() != null ? getActionName() : getText();
			return "QuickWidget " + getActionName();
		}
		
		// Needs cleaning up!!
		public void click(MouseButton button, int numClicks, List<ModifierPressed> modifiers) {
			systemInputManager.click(getCenterLocation(), button, numClicks, modifiers);
		}

		private Point getCenterLocation() {
			DesktopWindowRect rect = getRect();
			Point topLeft = getLocation();
			Point center = new Point(topLeft.x + rect.getWidth() / 2, topLeft.y + rect.getHeight() / 2);
			return new Point(topLeft.x + rect.getWidth() / 2, topLeft.y + rect.getHeight() / 2);
		}

		/**
		 * Hovers this menuItem
		 */
		public void hover() {
			List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
			alist.add(ModifierPressed.NONE);
			systemInputManager.mouseMove(getCenterLocation(), MouseButton.LEFT, alist);
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

		public String getMenu() {
			return menu;
		}

		// Will be moved to common superclass
		/**
	     * Checks if widget text equals the text specified by the given string id
	     *
	     * @return true if text specified by stringId equals widget text
	     */
		public boolean verifyText(String stringId) {
			String text = desktopUtils.getString(stringId, true /* skipAmpersand */);
			
			return getText().equals(text);
		}
}



