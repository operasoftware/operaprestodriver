/*
 * Copyright 2008-2011 Opera Software ASA
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

  public OperaMouse(OperaDriver parent) {
    this.parent = parent;
  }

  public void click(Coordinates where) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to click on.");
    }
    Point p = where.getLocationInViewPort();
    parent.exec.mouseAction(p.x, p.y, OperaMouseKeys.LEFT);
  }

  public void contextClick(Coordinates where) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to context click on.");
    }
    Point p = where.getLocationInViewPort();
    parent.exec.mouseAction(p.x, p.y, OperaMouseKeys.RIGHT);
  }

  public void doubleClick(Coordinates where) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to double click on.");
    }
    Point p = where.getLocationInViewPort();
    parent.exec.mouseAction(p.x, p.y, OperaMouseKeys.LEFT.getValue(), 2);
  }

  public void mouseDown(Coordinates where) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to mouse down on.");
    }
    Point p = where.getLocationInViewPort();
    parent.exec.mouseAction(p.x, p.y, OperaMouseKeys.LEFT_DOWN);
  }

  public void mouseUp(Coordinates where) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to mouse up on.");
    }
    Point p = where.getLocationInViewPort();
    parent.exec.mouseAction(p.x, p.y, OperaMouseKeys.LEFT_DOWN);
  }

  public void mouseMove(Coordinates where) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to mouse move to.");
    }
    Point p = where.getLocationInViewPort();
    parent.exec.mouseAction(p.x, p.y);
  }

  public void mouseMove(Coordinates where, long xOffset, long yOffset) {
    if (where == null) {
      throw new InvalidCoordinatesException("Invalid coordinates to mouse move to.");
    }

    Point p = where.getLocationInViewPort();

    /*
     * We can't compare against Integer.MAX_VALUE and throw, because this
     * method isn't defined as able to throw an Exception. Weird things will
     * just happen here...
     */
    int xO = (int) xOffset;
    int yO = (int) yOffset;
    parent.exec.mouseAction(p.x + xO, p.y + yO);
  }

}