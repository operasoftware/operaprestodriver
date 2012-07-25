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

import org.openqa.selenium.TouchScreen;
import org.openqa.selenium.interactions.internal.Coordinates;

public class OperaTouchScreen implements TouchScreen {

  private final OperaMobileDriver driver;

  public OperaTouchScreen(OperaMobileDriver driver) {
    this.driver = driver;
  }

  public void singleTap(Coordinates where) {

  }

  public void down(int x, int y) {

  }

  public void up(int x, int y) {

  }

  public void move(int x, int y) {

  }

  public void scroll(Coordinates where, int xOffset, int yOffset) {

  }

  public void doubleTap(Coordinates where) {

  }

  public void longPress(Coordinates where) {

  }

  public void scroll(int xOffset, int yOffset) {

  }

  public void flick(int xSpeed, int ySpeed) {

  }

  public void flick(Coordinates where, int xOffset, int yOffset, int speed) {

  }

}