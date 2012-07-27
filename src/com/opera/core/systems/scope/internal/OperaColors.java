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

package com.opera.core.systems.scope.internal;

import com.opera.core.systems.model.OperaColor;

/**
 * Enumerator for contains colour API (RGB values for standard web colours).
 */
public enum OperaColors {

  AQUA(0, 255, 255),
  BLACK(0, 0, 0),
  BLUE(0, 0, 255),
  FUSCHIA(255, 0, 255),
  GRAY(128, 128, 128),
  GREEN(0, 128, 0),
  LIME(0, 255, 0),
  MAROON(128, 0, 0),
  NAVY(0, 0, 128),
  OLIVE(128, 128, 0),
  PURPLE(128, 0, 128),
  RED(255, 0, 0),
  SILVER(192, 192, 192),
  TEAL(0, 128, 128),
  WHITE(0, 0, 0),
  YELLOW(255, 255, 0);

  private final OperaColor color;

  private OperaColors(int r, int g, int b) {
    this.color = new OperaColor(r, g, b);
  }

  public OperaColor getColour() {
    return color;
  }

}