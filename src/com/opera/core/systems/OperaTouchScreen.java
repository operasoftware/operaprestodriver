/*
Copyright 2012 Opera Software ASA

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

import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.interactions.internal.Coordinates;

// TODO(andreastt): This will eventually use the new bream-input service
public class OperaTouchScreen implements TouchScreen {

  private final OperaMouse mouse;

  public OperaTouchScreen(OperaMobileDriver driver) {
    this.mouse = (OperaMouse) driver.getMouse();
  }

  public void singleTap(Coordinates where) {
    mouse.click(where);
  }

  public void down(int x, int y) {
    throw new UnsupportedOperationException();
  }

  public void up(int x, int y) {
    throw new UnsupportedOperationException();
  }

  public void move(int x, int y) {
    throw new UnsupportedOperationException();
  }

  public void scroll(int xOffset, int yOffset) {
    throw new UnsupportedOperationException();
  }

  public void scroll(Coordinates where, int xOffset, int yOffset) {
    throw new UnsupportedOperationException();
  }

  public void doubleTap(Coordinates where) {
    mouse.doubleClick(where);
  }

  public void longPress(Coordinates where) {
    throw new UnsupportedOperationException();
  }

  public void flick(int xSpeed, int ySpeed) {
    throw new UnsupportedOperationException();
  }

  public void flick(Coordinates where, int xOffset, int yOffset, int speed) {
    throw new UnsupportedOperationException();
  }

}
