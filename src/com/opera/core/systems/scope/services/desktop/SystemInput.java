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

package com.opera.core.systems.scope.services.desktop;

import com.opera.core.systems.scope.Service;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

import java.awt.*;
import java.util.List;

/**
 * Interface for SystemInput.
 */
public interface SystemInput extends Service {

  static final String SERVICE_NAME = "system-input";

  /**
   * Clicks MouseButton a specified number of times with zero or more modifiers held down.
   *
   * @param location  - Point to click
   * @param button    - MouseButton to click
   * @param numClicks - number of clicks
   * @param modifiers - modifiers to hold down during click
   */
  void click(Point location, MouseButton button, int numClicks,
             List<ModifierPressed> modifiers);

  /**
   * Presses key, optionally with one or more modifiers held down.
   *
   * @param key       - key to press
   * @param modifiers - modifiers to hold
   */
  void keyPress(String key, List<ModifierPressed> modifiers);

  void mouseMove(Point location, MouseButton button, List<ModifierPressed> modifiers);

  void mouseUp(Point location, MouseButton button, List<ModifierPressed> modifiers);

  void mouseDown(Point location, MouseButton button, List<ModifierPressed> modifiers);

  void keyDown(String key, List<ModifierPressed> modifiers);

  void keyUp(String key, List<ModifierPressed> modifiers);

}