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

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo.DesktopWindowType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;

import java.awt.*;

/**
 * Represents a window in the Desktop UI.
 */
public class QuickWindow {

  private final DesktopWindowInfo info;

  /**
   * Constructor.
   *
   * @param info DesktopWindowInfo describing the window
   */
  public QuickWindow(DesktopWindowInfo info) {
    this.info = info;
  }

  /**
   * @return window id of this window
   */
  public int getWindowID() {
    return info.getWindowID();
  }

  /**
   * @return title of this window
   */
  public String getTitle() {
    return info.getTitle();
  }

  /**
   * @return name of this window
   */
  public String getName() {
    return info.getName();
  }

  /**
   * @return true if window is active
   */
  public boolean isActive() {
    return info.getActive();
  }

  /**
   * @return true if the window is on screen, otherwise false
   */
  public boolean isOnScreen() {
    return info.getOnScreen();
  }

  /**
   * @return the type of the window
   */
  public DesktopWindowType getType() {
    return info.getWindowType();
  }

  /**
   * @return DesktopWindowRect of the window
   */
  public DesktopWindowRect getRect() {
    return info.getRect();
  }

  /**
   * @return Point describing location of window
   */
  public Point getLocation() {
    DesktopWindowRect rect = getRect();
    return new Point(rect.getX(), rect.getY());
  }

  /**
   * @return size of this window
   */
  public Dimension getSize() {
    DesktopWindowRect rect = getRect();
    return new Dimension(rect.getWidth(), rect.getHeight());
  }

  @Override
  public String toString() {
    return "QuickWindow " + getWindowID() + ", " + getName() + ", "
           + getTitle();
  }

}
