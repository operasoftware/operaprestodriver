package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import org.openqa.selenium.ElementNotVisibleException;

import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

public class QuickWidget {
		private final QuickWidgetInfo info; 
		private final IDesktopUtils desktopUtils;
		private final SystemInputManager systemInputManager;
		private final int parentWindowId;
		
		public enum DropPosition {
			CENTER(0),
			EDGE(1),
			BETWEEN(2),
			;
			private DropPosition(int value) {
				this.value = value;
			}
			
			private Integer value;

			public Integer getValue() {
				return value;
			}
		}

		public QuickWidget(IDesktopUtils desktopUtils, SystemInputManager inputManager, QuickWidgetInfo info, int parentWindowId) {
	        this.info = info;
	        this.desktopUtils = desktopUtils;
	        this.systemInputManager = inputManager;
	        this.parentWindowId = parentWindowId;
	    }
		
		public int getParentWindowId() {
			return parentWindowId;
		}
		
		public void click(MouseButton button, int numClicks, List<ModifierPressed> modifiers) {
			//System.out.println(" Click  "+ info.getName() + "!");
			if (OperaFlags.ENABLE_CHECKS){
				if(!isVisible())
					throw new ElementNotVisibleException("You can't click an element that is not displayed");
			}

			systemInputManager.click(getCenterLocation(), button, numClicks, modifiers);
		}
		
		public void dragAndDropOn(QuickWidget element, DropPosition dropPos) {
			/*
			 * FIXME: Handle MousePosition
			 * 
			 */
			Point currentLocation = this.getCenterLocation();
			Point dropPoint = getDropPoint(element, dropPos);
			
			List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
			alist.add(ModifierPressed.NONE);
			
			systemInputManager.mouseDown(currentLocation, MouseButton.LEFT, alist);
			systemInputManager.mouseMove(dropPoint, MouseButton.LEFT, alist);
			systemInputManager.mouseUp(dropPoint, MouseButton.LEFT, alist);
		}

		// Intersect two lines
		private Point intersection(int x1,int y1,int x2,int y2, int x3, int y3, int x4,int y4) {
			double dem = (x1 -x2) * (y3 -y4) - (y1 - y2) * (x3 -x4);

			// Solve the intersect point
			double xi = ((x1*y2 - y1*x2) * (x3 - x4) - (x1 - x2) * (x3*y4 - y3*x4)) / dem;
			double yi = ((x1*y2 - y1*x2) * (y3 - y4) - (y1 - y2) * (x3*y4 - y3*x4)) / dem;

			// Check the point isn't off the ends of the lines
			if ((x1-xi)*(xi-x2)>=0 && (x3-xi)*(xi-x4)>=0 && (y1-yi)*(yi-y2)>=0 && (y3-yi)*(yi-y4)>=0)
				return new Point((int)xi, (int)yi);
			
			return null;
		}

		// Intersect a line and a DesktopWindowRect
		private Point intersection(int x1,int y1,int x2,int y2, DesktopWindowRect rect) {
			Point bottom = intersection(x1, y1, x2, y2, rect.getX(), rect.getY(), rect.getX() + rect.getHeight(), rect.getY());
			if (bottom != null)
				return bottom;
			
			Point right = intersection(x1, y1, x2, y2, rect.getX() + rect.getWidth(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
			if (right != null)
				return right;

			Point top = intersection(x1, y1, x2, y2, rect.getX(), rect.getY() + rect.getHeight(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
			if (top != null)
				return top;

			Point left = intersection(x1, y1, x2, y2, rect.getX(), rect.getY(), rect.getX(), rect.getY() + rect.getHeight());
			if (left != null)
				return left;

			return null;
		}
		
		// Gets the coordinates of the drop point between the two quick widgets
		// The drop point is on the quick widget passed in
		public Point getDropPoint(QuickWidget element, DropPosition pos) {
			Point dropPoint = new Point(element.getCenterLocation().x, element.getCenterLocation().y);
			if (pos == DropPosition.CENTER) 
				return dropPoint;
			
			Point dragPoint = new Point(this.getCenterLocation().x, this.getCenterLocation().y);
			
			// Find the side of the DesktopWindowRect of the widget that as the intersect
			Point dragIntersectPoint = intersection(dragPoint.x, dragPoint.y, dropPoint.x, dropPoint.y, this.getRect());
			
			if (dragIntersectPoint != null) {
				Point dropIntersectPoint = intersection(dragPoint.x, dragPoint.y, dropPoint.x, dropPoint.y, element.getRect());
			
				if (dropIntersectPoint != null) {
					if (pos == DropPosition.EDGE)
					{
						//System.out.println(" DropPoint  x: " + dropIntersectPoint.x + " y: " + dropIntersectPoint.y);
						return dropIntersectPoint;
					}

					// Get the mid point of the line
					return new Point(dragIntersectPoint.x - dropIntersectPoint.x, dragIntersectPoint.y - dropIntersectPoint.y);
				}
			}
			
			return null;
		}

		public void hover() {
			List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
			alist.add(ModifierPressed.NONE);
			systemInputManager.mouseMove(getCenterLocation(), MouseButton.LEFT, alist);
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
			return desktopUtils.removeCR(info.getText());
		}
		
		/**
	     * Check if widget text equals the text specified by @param string_id
	     * 
	     * @return true if text specified by string_id equals widget text
	     */
		public boolean verifyText(String string_id) {
			String text = desktopUtils.getString(string_id);
			// Remember to remove all CRLF
			return getText().indexOf(text) >= 0;
		}
		
		/**
	     * Check if widget text contains the text specified by @param string_id
	     * 
	     * @param String string_id - id of string 
	     * @return true if text specified by string_id is contained in widget text
	     */
		public boolean verifyContainsText(String string_id) {
			String text = desktopUtils.getString(string_id);
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
		
		public boolean isSelected(String string_id) {
			String text = desktopUtils.getString(string_id);
			return text.equals(info.getText());
		}
		
		/**
		 * @return true if widget is visible
		 */
		public boolean isVisible(){
			return info.getVisible(); 
		}
		
		/**
		 * 
		 * @return 
		 */
		public QuickWidgetType getType(){
			return info.getType(); 
		}
		
		/**
		 * @return DesktopWindowRect of the widget
		 */
		public DesktopWindowRect getRect() {
			return info.getRect();
		}
		
		private Point getCenterLocation() {
			DesktopWindowRect rect = getRect();
			Point top_left = getLocation();
			return new Point(top_left.x + rect.getWidth() / 2, top_left.y + rect.getHeight() / 2);
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

		public int getRow() {
			return info.getRow();
		}

		public int getColumn() {
			return info.getCol();
		}

		protected int getValue() {
			return info.getValue();
		}
		
		public String getParentName() {
			return info.getParent();
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
		//	+ "       type: " + getType()  + "\n" // TODO: FIXME
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



