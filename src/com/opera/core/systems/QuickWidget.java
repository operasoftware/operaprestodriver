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
public class QuickWidget {
  private final QuickWidgetInfo info;
  private final IDesktopUtils desktopUtils;
  private final SystemInputManager systemInputManager;
  private final int parentWindowId;

  public enum DropPosition {
    CENTER(0), EDGE(1), BETWEEN(2), ;
    private DropPosition(int value) {
      this.value = value;
    }

    private Integer value;

    public Integer getValue() {
      return value;
    }
  }

  /**
   * Constructor.
   * 
   * @param desktopUtils
   * @param inputManager
   * @param info QuickWidgetInfo of the widget
   * @param parentWindowId id of parent window
   */
  public QuickWidget(IDesktopUtils desktopUtils,
      SystemInputManager inputManager, QuickWidgetInfo info, int parentWindowId) {
    this.info = info;
    this.desktopUtils = desktopUtils;
    this.systemInputManager = inputManager;
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

  /**
   * Clicks this widget.
   * 
   * @param button button to click
   * @param numClicks number of clicks
   * @param modifiers modifiers held during click
   */
  public void click(MouseButton button, int numClicks,
      List<ModifierPressed> modifiers) {
    systemInputManager.click(getCenterLocation(), button, numClicks, modifiers);
  }

  private Point getCenterLocation() {
    DesktopWindowRect rect = getRect();
    Point topLeft = getLocation();
    return new Point(topLeft.x + rect.getWidth() / 2, topLeft.y
        + rect.getHeight() / 2);
  }

  // Intersect two lines
  private Point intersection(int x1, int y1, int x2, int y2, int x3, int y3,
      int x4, int y4) {
    double dem = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

    // Solve the intersect point
    double xi = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2)
        * (x3 * y4 - y3 * x4))
        / dem;
    double yi = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2)
        * (x3 * y4 - y3 * x4))
        / dem;

    // Check the point isn't off the ends of the lines
    if ((x1 - xi) * (xi - x2) >= 0 && (x3 - xi) * (xi - x4) >= 0
        && (y1 - yi) * (yi - y2) >= 0 && (y3 - yi) * (yi - y4) >= 0) {
      return new Point((int) xi, (int) yi);
    }
    return null;
  }

  // Intersect a line and a DesktopWindowRect
  private Point intersection(int x1, int y1, int x2, int y2,
      DesktopWindowRect rect) {
    Point bottom = intersection(x1, y1, x2, y2, rect.getX(), rect.getY(),
        rect.getX() + rect.getHeight(), rect.getY());
    if (bottom != null) return bottom;

    Point right = intersection(x1, y1, x2, y2, rect.getX() + rect.getWidth(),
        rect.getY(), rect.getX() + rect.getWidth(), rect.getY()
            + rect.getHeight());
    if (right != null) return right;

    Point top = intersection(x1, y1, x2, y2, rect.getX(), rect.getY()
        + rect.getHeight(), rect.getX() + rect.getWidth(), rect.getY()
        + rect.getHeight());
    if (top != null) return top;

    Point left = intersection(x1, y1, x2, y2, rect.getX(), rect.getY(),
        rect.getX(), rect.getY() + rect.getHeight());
    if (left != null) return left;

    return null;
  }

  /**
   * Drags this widget onto the specified widget at the given drop position
   * 
   * @param widget the widget to drop this widget onto
   * @param dropPos the position to drop this widget into, CENTER, EDGE or
   *          BETWEEN
   */
  public void dragAndDropOn(QuickWidget widget, DropPosition dropPos) {
    /*
     * FIXME: Handle MousePosition
     */
    Point currentLocation = this.getCenterLocation();
    Point dropPoint = getDropPoint(widget, dropPos);

    List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
    alist.add(ModifierPressed.NONE);

    systemInputManager.mouseDown(currentLocation, MouseButton.LEFT, alist);
    systemInputManager.mouseMove(dropPoint, MouseButton.LEFT, alist);
    systemInputManager.mouseUp(dropPoint, MouseButton.LEFT, alist);
  }

  // Gets the coordinates of the drop point between the two quick widgets
  // The drop point is on the quick widget passed in
  private Point getDropPoint(QuickWidget element, DropPosition pos) {
    Point dropPoint = new Point(element.getCenterLocation().x,
        element.getCenterLocation().y);
    if (pos == DropPosition.CENTER) return dropPoint;

    Point dragPoint = new Point(this.getCenterLocation().x,
        this.getCenterLocation().y);

    // Find the side of the DesktopWindowRect of the widget that as the
    // intersect
    Point dragIntersectPoint = intersection(dragPoint.x, dragPoint.y,
        dropPoint.x, dropPoint.y, this.getRect());

    if (dragIntersectPoint != null) {
      Point dropIntersectPoint = intersection(dragPoint.x, dragPoint.y,
          dropPoint.x, dropPoint.y, element.getRect());

      if (dropIntersectPoint != null) {
        if (pos == DropPosition.EDGE) {
          return dropIntersectPoint;
        }

        // Get the mid point of the line
        return new Point((dragIntersectPoint.x + dropIntersectPoint.x) / 2,
            (dragIntersectPoint.y + dropIntersectPoint.y) / 2);
      }
    }

    return null;
  }

  /**
   * Hovers this widget.
   */
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
   * Gets the visible text of the widget.
   * 
   * @return visible text of widget
   */
  public String getVisibleText() {
    return desktopUtils.removeCR(info.getVisibleText());
  }

  /**
   * Gets additional text of this widget. Used for widgets that have more text
   * attributes, e.g. for highlighted text in the address field
   * 
   * @return text of widget
   */
  public String getAdditionalText() {
    return desktopUtils.removeCR(info.getAdditionalText());
  }

  /**
   * Checks if widget text equals the text specified by the given string id
   * 
   * @return true if text specified by stringId equals widget text
   */
  public boolean verifyText(String stringId) {
    String text = desktopUtils.getString(stringId);
    // Remember to remove all CRLF
    return getText().indexOf(text) >= 0;
  }

  /**
   * Checks if widget text contains the text specified by the given string id
   * 
   * @param stringId String id of string
   * @return true if text specified by stringId is contained in widget text
   */
  public boolean verifyContainsText(String stringId) {
    String text = desktopUtils.getString(stringId);
    return getText().indexOf(text) >= 0;
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
    String text = desktopUtils.getString(stringId);
    return text.equals(info.getText());
  }

  /**
   * @return true if widget is visible, else false
   */
  public boolean isVisible() {
    return info.getVisible();
  }

  /**
   * 
   * @return QuickWidgetType type of this widget. See {@link QuickWidgetType}
   *         for the possible types
   */
  public QuickWidgetType getType() {
    return info.getType();
  }

  /**
   * @return DesktopWindowRect of the widget
   */
  public DesktopWindowRect getRect() {
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

  /**
   * Gets the row of the widget within its parent, e.g. of a treeviewitem in a
   * treeview. Returns 0 for all widgets that don't have a row within its parent
   * 
   * @return row of widget\
   */
  public int getRow() {
    return info.getRow();
  }

  /**
   * Gets the column of the widget within its parent, e.g. for a tab within the
   * pagebar, or a treeviewitem in a treeview returns 0 for widgets that don't
   * have a column number within its parent
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
    //  + "       type: " + getType()  + "\n" // TODO: FIXME
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



