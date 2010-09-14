package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;

import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.services.IDesktopWindowManager;

public class QuickWidget {
		private final QuickWidgetInfo info; 
		private final IDesktopWindowManager desktopWindowManager;
		
		public QuickWidget(IDesktopWindowManager wm, QuickWidgetInfo info) {
	        this.info = info;
	        this.desktopWindowManager = wm;
	    }
		
		/**
	     * 
	     * @return name of widget
	     */
		public String getName() {
			return info.getName();
		}

		/**
	     * 
	     * @return text of widget
	     */
		public String getText() {
			return info.getText();
		}
		
		/**
	     * Check if widget text equals the text specified by @param string_id
	     * 
	     * @return true if text specified by string_id equals widget text
	     */
		public boolean verifyText(String string_id) {
			String text = desktopWindowManager.getString(string_id);
			return getText().equals(text);
		}
		
		/**
	     * Check if widget text contains the text specified by @param string_id
	     * 
	     * @param String string_id - id of string 
	     * @return true if text specified by string_id is contained in widget text
	     */
		public boolean verifyContainsText(String string_id) {
			String text = desktopWindowManager.getString(string_id);
			return getText().indexOf(text) >= 0;
		}
		
		/** 
		 * @return true if widget is default, else false
		 */
		public boolean isDefault() {
			return info.getDefaultLook();
		}
		
		/** 
		 *  
		 * @return
		 */
		public boolean hasFocusedLook() {
			return info.getFocusedLook();
		}

		/** 
		 * Check if widget is enabled  
		 * @return true if enabled, else false
		 */
		public boolean isEnabled() {
			return info.getEnabled();
		}
		
		/**
		 * @return if widget is selected, else false
		 */
		public boolean isSelected() {
			return info.getValue() == 1;
		}
		
		/**
		 * @return true if widget is visible
		 */
		public boolean isVisible(){
			return info.getVisible(); 
		}
		
		/**
		 * @return string which is the type of the widget
		 */
		public String getType(){
			return info.getType(); 
		}
		
		/**
		 * @return DesktopWindowRect of the widget
		 */
		private DesktopWindowRect getRect() {
			return info.getRect();
		}
		
		/**
		 * @return Point describing location of widget
		 */
		public Point getLocation() {
			DesktopWindowRect rect = getRect();
			return new Point(rect.getX(), rect.getY());
		}

		/**
		 * @return size of widget
		 */
		public Dimension getSize() {
			DesktopWindowRect rect = getRect();
			return new Dimension(rect.getWidth(), rect.getHeight());
		}
		
		protected int getValue() {
			return info.getValue();
		}

		protected String getType() {
			return info.getType();
		}
		@Override
		// TODO: FIXME
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof QuickWidget)) return false;
			
			QuickWidget ref = (QuickWidget) obj;
			return (ref.getName().equals(this.getName()));
		}
		
		@Override
		// TODO: FIXME
		public int hashCode() {
			int result = 42;
			result = 31 * result + getName().hashCode();
			return result;
		}
		
		@Override
		public String toString() {
			return "QuickWidget " + getName();
		}
		
		public String toFullString() {
			return "QuickWidget\n" +  
			  "       Widget name: " + getName() + "\n"
			+ "       type: " + getType()  + "\n"
			+ "    visible: " + isVisible() + "\n"
			+ "       text: " + getText() + "\n"
			+ "      state: " + getValue() + "\n"
			+ "    enabled: " + isEnabled() + "\n"
			+ "    default: " + isDefault() + "\n"
			+ "    focused: " + hasFocusedLook() + "\n"
			+ "          x: " + getRect().getX() + "\n"
			+ "          y: " + getRect().getY() + "\n"
			+ "      width: " + getRect().getWidth() + "\n"
			+ "     height: " + getRect().getHeight() + " \n";
		}
}



