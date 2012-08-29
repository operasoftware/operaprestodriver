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

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

/**
 * Represents a widget in the Opera desktop UI.
 *
 * @author Adam Minchinton, Karianne Ekern.
 *
 */
public class QuickWidget extends OperaUIElement {
		private final QuickWidgetInfo info;
		private final int parentWindowId;

		protected final Logger logger = Logger.getLogger(this.getClass().getName());

		public enum DropPosition {
			CENTER,
			EDGE,
			BETWEEN
		}

		/**
		 * Constructor.
		 *
		 * @param desktopUtils
		 * @param inputManager
		 * @param info QuickWidgetInfo of the widget
		 * @param parentWindowId id of parent window
		 */
		public QuickWidget(IDesktopUtils desktopUtils, SystemInputManager inputManager,
				QuickWidgetInfo info, int parentWindowId) {
			super(inputManager, desktopUtils);
	        this.info = info;
	        this.parentWindowId = parentWindowId;
	    }

		/**
		 * Gets window id of this widgets parent window.
		 *
		 * @return window id of parent window of this QuickWidget
		 */
		public int getParentWindowId() {
			return parentWindowId;
		}

		// Intersect two lines
		private Point intersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
			double dem = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

			// Solve the intersect point
			double xi = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / dem;
			double yi = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / dem;

			// Check the point isn't off the ends of the lines
			if ((x1 - xi) * (xi - x2) >= 0 && (x3 - xi) * (xi - x4) >= 0 && (y1 - yi) * (yi - y2) >= 0 && (y3 - yi) * (yi - y4) >= 0) {
				return new Point((int) xi, (int) yi);
			}

			logger.finest(String.format("Line segments (%dx%d)->(%dx%d) and (%d,%d)x(%d,%d) don't seem to intersect.", x1, y1, x2, y2, x3, y3, x4, y4));

			return null;
		}

		// Intersect a line and a DesktopWindowRect
		private Point intersection(int x1,int y1,int x2,int y2, DesktopWindowRect rect) {

			logger.fine("Line segment: (" + x1 + "," + y1 + ")->(" + x2 + "," + y2 + "); Rectangle: (" + rect.getX() + "," + rect.getY() + ")x(" + rect.getWidth() + "," + rect.getHeight() + ")");

			logger.fine("Checking bottom");
			Point bottom = intersection(x1, y1, x2, y2, rect.getX(), rect.getY() + rect.getHeight(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
			if (bottom != null)
			{
				logger.fine("Intersection found: bottom = " + bottom);
				return bottom;
			}

			logger.fine("Checking right");
			Point right = intersection(x1, y1, x2, y2, rect.getX() + rect.getWidth(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
			if (right != null)
			{
				logger.fine("Intersection found: right = " + right);
				return right;
			}

			logger.fine("Checking top");
			Point top = intersection(x1, y1, x2, y2, rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY());
			if (top != null)
			{
				logger.fine("Intersection found: top = " + top);
				return top;
			}

			logger.fine("Checking left");
			Point left = intersection(x1, y1, x2, y2, rect.getX(), rect.getY(), rect.getX(), rect.getY() + rect.getHeight());
			if (left != null)
			{
				logger.fine("Intersection found: left = " + left);
				return left;
			}

			logger.warning("NO intersection found! Line segment: (" + x1 + "," + y1 + ")->(" + x2 + "," + y2 + "); Rectangle: (" + rect.getX() + "," + rect.getY() + ")x(" + rect.getWidth() + "," + rect.getHeight() + ")");
			return null;
		}

		/**
		 * Drags this widget onto the specified widget at the given drop position
		 *
		 * @param widget the widget to drop this widget onto
		 * @param dropPos the position to drop this widget into, CENTER, EDGE or BETWEEN
		 */
		public void dragAndDropOn(QuickWidget widget, DropPosition dropPos) {
			/*
			 * FIXME: Handle MousePosition
			 */
			Point currentLocation = getCenterLocation();
			Point dropPoint = getDropPoint(widget, dropPos);

			if (null == dropPoint)
			{
				logger.warning("Could not drop " + this + " onto " + widget + " at " + dropPos + ". The drop point could not be found.");
				logger.warning("Attempted drag from " + asString(currentLocation) + " to " + asString(widget.getRect()) + "@" + dropPos);
				return;
			}

			List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
			alist.add(ModifierPressed.NONE);

			getSystemInputManager().mouseDown(currentLocation, MouseButton.LEFT, alist);

			/*
			This has to actually emulate the full drag, not just the end points. The UI may and will
			behave differently if you don't do this.
			*/
			final Integer xDragDistance = dropPoint.x - currentLocation.x;
			final Integer yDragDistance = dropPoint.y - currentLocation.y;
			final Integer xDragDistanceAbs = Math.abs(xDragDistance);
			final Integer yDragDistanceAbs = Math.abs(yDragDistance);

			Double shorterStep = 0.0;
			final Integer xDirection = (xDragDistance >= 0)?1:-1;
			final Integer yDirection = (yDragDistance >= 0)?1:-1;

			if (xDragDistanceAbs >= yDragDistanceAbs)
			{
				shorterStep = (double)yDragDistanceAbs / xDragDistanceAbs;
				for (int i = 1; i <= xDragDistanceAbs; i++)
				{
					int curX = currentLocation.x + (xDirection*i);
					int curY = currentLocation.y + (int)((yDirection*i) * shorterStep);
					Point curPoint = new Point(curX, curY);

					getSystemInputManager().mouseMove(curPoint, MouseButton.LEFT, alist);
				}
			}
			else
			{
				shorterStep = (double)xDragDistanceAbs / yDragDistanceAbs;
				for (int i = 1; i <= yDragDistanceAbs; i++)
				{
					int curY = currentLocation.y + (yDirection*i);
					int curX = currentLocation.x + (int)((xDirection*i) * shorterStep);
					Point curPoint = new Point(curX, curY);

					getSystemInputManager().mouseMove(curPoint, MouseButton.LEFT, alist);
				}
			}

			getSystemInputManager().mouseUp(dropPoint, MouseButton.LEFT, alist);
		}

		// Gets the coordinates of the drop point between the two quick widgets
		// The drop point is on the quick widget passed in
		private Point getDropPoint(QuickWidget element, DropPosition pos) {
			Point dropPoint = new Point(element.getCenterLocation().x, element.getCenterLocation().y);
			if (pos == DropPosition.CENTER)
				return dropPoint;

			Point dragPoint = new Point(this.getCenterLocation().x, this.getCenterLocation().y);

			// Find the side of the DesktopWindowRect of the widget that as the intersect
			logger.fine("Looking for drag intersection point...");
			Point dragIntersectPoint = intersection(dragPoint.x, dragPoint.y, dropPoint.x, dropPoint.y, this.getRect());

			if (dragIntersectPoint != null) {
				logger.fine("Looking for drop intersection point...");
				Point dropIntersectPoint = intersection(dragPoint.x, dragPoint.y, dropPoint.x, dropPoint.y, element.getRect());

				logger.fine("dropIntersectPoint=" + dropIntersectPoint);

				if (dropIntersectPoint != null) {
					if (pos == DropPosition.EDGE)
					{
						return dropIntersectPoint;
					}

					// Get the mid point of the line
					Point midPoint = new Point((dragIntersectPoint.x + dropIntersectPoint.x) / 2, (dragIntersectPoint.y + dropIntersectPoint.y) / 2);
					return midPoint;
				}
			}
			logger.warning("Could not drop " + this + " onto " + element + " at " + pos + ". The drop point could not be found.");
			logger.warning("start=" + asString(dragPoint) + " drop=" + asString(dropPoint) + " rect=" + asString(this.getRect()));


			return null;
		}

		/**
	     *
	     * @return name of widget
	     */
		public String getName() {
			return info.getName();
		}

		protected String asString(Point point)
		{
			String ret;
			if (point == null)
				ret = "<NULL>";
			else
			  ret = "(" + point.getX() + "," + point.getY() + ")";
			return ret;
		}

		protected String asString(DesktopWindowRect rect)
		{
			String ret;
			if (rect == null)
				ret = "<NULL>";
			else
			  ret = "(" + rect.getX() + "," + rect.getY() + ")x(" + rect.getWidth() + "," + rect.getHeight() + ")";
			return ret;
		}

		/**
	     *
	     * @return text of widget
	     */
		public String getText() {
			return getDesktopUtils().removeCR(info.getText());
		}

		/**
	     * Gets the visible text of the widget.
	     *
	     * @return visible text of widget
	     */
		public String getVisibleText() {
			return getDesktopUtils().removeCR(info.getVisibleText());
		}

		/**
	     * Gets additional text of this widget.
	     * Used for widgets that have more text attributes, e.g.
	     * for highlighted text in the address field
	     *
	     * @return text of widget
	     */
		public String getAdditionalText() {
			return getDesktopUtils().removeCR(info.getAdditionalText());
		}

		/**
		 * Checks if widget is default.
		 *
		 * Typically used to check if e.g. a button is the default pressed button
		 * among buttons in a dialog, and such.
		 *
		 * @return true if widget is default, else false
		 */
		public boolean isDefault() {
			return info.getDefaultLook();
		}

		/**
		 *
		 * @return true if this widget has focused look
		 */
		public boolean hasFocusedLook() {
			return info.getFocusedLook();
		}

		/**
		 * Checks if widget is enabled.
		 *
		 * @return true if enabled, else false
		 */
		public boolean isEnabled() {
			return info.getEnabled();
		}

		/**
		 *
		 * @return true if widget is selected, else false
		 */
		public boolean isSelected() {
			return info.getValue() == 1;
		}

		/**
		 *
		 * @param stringId stringid of entry to check if is selected
		 * @return true if the entry given by stringId is selected, else false
		 */
		public boolean isSelected(String stringId) {
			String text = getDesktopUtils().getString(stringId, true);
			return text.equals(info.getText());
		}

		/**
		 * @return true if widget is visible, else false
		 */
		public boolean isVisible(){
			return info.getVisible();
		}

		/**
		 *
		 * @return QuickWidgetType type of this widget. See {@link QuickWidgetType}
		 * 			for the possible types
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

		/**
		 * Gets the row of the widget within its parent,
		 * e.g. of a treeviewitem in a treeview.
		 * Returns 0 for all widgets that don't have a row
		 * within its parent
		 *
		 * @return row of widget\
		 */
		public int getRow() {
			return info.getRow();
		}

		/**
 		 * Gets the column of the widget within its parent,
		 * e.g. for a tab within the pagebar, or a treeviewitem in a treeview
		 * returns 0 for widgets that don't have a column number within its
		 * parent
		 *
		 * @return column of widget
		 */
		public int getColumn() {
			return info.getCol();
		}

		/**
		 * @return value
		 */
		protected int getValue() {
			return info.getValue();
		}

		/**
		 * Gets name of parent widget of this widget
		 *
		 * @return name of parent widget
		 */
		public String getParentName() {
			return info.getParent();
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



