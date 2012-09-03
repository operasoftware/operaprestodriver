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
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.desktop.DesktopUtils;
import com.opera.core.systems.scope.services.desktop.SystemInput;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user-interface element in the Opera Desktop browser.
 */
public abstract class OperaUIElement {

  private SystemInput systemInputManager;
  private DesktopUtils desktopUtils;

  public OperaUIElement(SystemInput systemInputManager, DesktopUtils desktopUtils) {
    super();
    this.systemInputManager = systemInputManager;
    this.desktopUtils = desktopUtils;
  }

  protected SystemInput getSystemInputManager() {
    return systemInputManager;
  }

  protected DesktopUtils getDesktopUtils() {
    return desktopUtils;
  }

  public abstract DesktopWindowRect getRect();

  /**
   * @return Point describing location of this element
   */
  public Point getLocation() {
    DesktopWindowRect rect = getRect();
    return new Point(rect.getX(), rect.getY());
  }

  /**
   * @return true if widget is visible, else false
   */
  public boolean isVisible() {
    return true; // default
  }

  /**
   * @return size of Element
   */
  public Dimension getSize() {
    DesktopWindowRect rect = getRect();
    return new Dimension(rect.getWidth(), rect.getHeight());
  }

  public abstract String toFullString();

  public void click(MouseButton button, int numClicks, List<ModifierPressed> modifiers) {
    systemInputManager.click(getCenterLocation(), button, numClicks, modifiers);
  }

  protected Point getCenterLocation() {
    DesktopWindowRect rect = getRect();
    Point topLeft = getLocation();
    //Point center = new Point(topLeft.x + rect.getWidth() / 2, topLeft.y + rect.getHeight() / 2);
    return new Point(topLeft.x + rect.getWidth() / 2, topLeft.y + rect.getHeight() / 2);
  }

  /**
   * Hovers Element
   */
  public void hover() {
    List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
    alist.add(ModifierPressed.NONE);
    systemInputManager.mouseMove(getCenterLocation(), MouseButton.LEFT, alist);
  }


  /**
   * Checks if widget text equals the text specified by the given string id
   *
   * @return true if text specified by stringId equals widget text
   */
  public boolean verifyText(String stringId) {
    String text = desktopUtils.getString(stringId, true /* skipAmpersand */);

    return getText().equals(text);
  }

  /**
   * Checks if widget text contains the text specified by the given string id
   *
   * @param stringId String id of string
   * @return true if text specified by stringId is contained in widget text
   */
  public boolean verifyContainsText(String stringId) {
    String text = desktopUtils.getString(stringId, true);
    return getText().indexOf(text) >= 0;
  }

  public abstract String getText();

}