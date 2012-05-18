/*
 * Copyright 2008-2012 Opera Software ASA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opera.core.systems;

import com.opera.core.systems.scope.internal.OperaMouseKeys;

import org.openqa.selenium.Mouse;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.InvalidCoordinatesException;
import org.openqa.selenium.interactions.internal.Coordinates;

public class OperaMouse implements Mouse {

  private final OperaDriver parent;
  private Point lastMousePosition;

  public OperaMouse(OperaDriver parent) {
    this.parent = parent;
  }

  public void click(Coordinates where) {
    Point p = getPoint(where, "click");
    parent.getExecService().mouseAction(p.x, p.y, OperaMouseKeys.LEFT);
  }

  public void contextClick(Coordinates where) {
    Point p = getPoint(where, "context click");
    parent.getExecService().mouseAction(p.x, p.y, OperaMouseKeys.RIGHT);
  }

  public void doubleClick(Coordinates where) {
    Point p = getPoint(where, "double click");
    parent.getExecService().mouseAction(p.x, p.y, 2, OperaMouseKeys.LEFT);
  }

  /**
   * Triple click is an Opera specific way of selecting a sentence.
   *
   * @param where to click
   */
  public void tripleClick(Coordinates where) {
    Point p = getPoint(where, "triple click");
    parent.getExecService().mouseAction(p.x, p.y, 3, OperaMouseKeys.LEFT);
  }

  /**
   * Quadruple click is an Opera specific way of selecting a paragraph.
   *
   * @param where to click
   */
  public void quadrupleClick(Coordinates where) {
    Point p = getPoint(where, "quadruple click");
    parent.getExecService().mouseAction(p.x, p.y, 4, OperaMouseKeys.LEFT);
  }

  public void mouseDown(Coordinates where) {
    Point p = getPoint(where, "mouse down");
    parent.getExecService().mouseAction(p.x, p.y, OperaMouseKeys.LEFT_DOWN);
  }

  public void mouseUp(Coordinates where) {
    Point p = getPoint(where, "mouse up");
    parent.getExecService().mouseAction(p.x, p.y, OperaMouseKeys.LEFT_UP);
  }

  public void mouseMove(Coordinates where) {
    Point p = getPoint(where, "mouse move");
    parent.getExecService().mouseAction(p.x, p.y);
  }

  public void mouseMove(Coordinates where, long xOffset, long yOffset) {
    Point p = getPoint(where, "mouse move");

    // We can't compare against Integer.MAX_VALUE and throw because this method isn't defined as
    // able to throw an Exception.  Weird things will just happen here...
    int xO = (int) xOffset;
    int yO = (int) yOffset;

    lastMousePosition = new Point(p.x + xO, p.y + yO);
    parent.getExecService().mouseAction(p.x + xO, p.y + yO);
  }

  private Point getPoint(Coordinates where, String action) {
    // If coordinates exist then we want to update the last known mouse position and then use it in
    // the action.
    if (where != null) {
      lastMousePosition = where.getLocationInViewPort();
    }

    if (lastMousePosition != null) {
      return lastMousePosition;
    }

    throw new InvalidCoordinatesException("Invalid coordinates to " + action + " on");
  }

}