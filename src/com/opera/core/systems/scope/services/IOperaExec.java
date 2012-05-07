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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaMouseKeys;

import java.util.Set;

public interface IOperaExec {

  void init();

  /**
   * Type a string, preserving space, sent to active window This command doesn't return a response
   * in STP/0 hence we sleep to be on the safe side.
   */
  void type(String using);

  /**
   * Create a mouse action on coordinates x,y with key(s) as specified in {@link OperaMouseKeys}.
   */
  void mouseAction(int x, int y, OperaMouseKeys... keys);

  /**
   * Create one or more mouse actions on coordinates x,y with key(s) as specified in {@link
   * OperaMouseKeys} and the number of repeated events as an integer.
   */
  void mouseAction(int x, int y, int count, OperaMouseKeys... keys);

  /**
   * Returns the list of possible Opera Actions on host.
   */
  Set<String> getActionList();

  /**
   * Execute an opera action with parameters (parameters are sent with commas).
   */
  void action(String using, String... param);

  void action(String using, int windowID, String... params);

  void action(String using, int data, String dataString, String dataStringParam);

  /**
   * Execute a key press (immediately send key down and up).
   */
  void key(String key);

  /**
   * Create a key event, this event controls if the key has been previously pressed before allowing
   * key up.
   *
   * @param up down if false
   */
  void key(String key, boolean up);

  /**
   * Whether the given key is currently pressed down.
   *
   * @return true if the key is currently down, false if not.
   */
  boolean keyIsPressed(String key);

  /**
   * Release all the pressed keys
   */
  void releaseKeys();

  /**
   * Check if the page contains color(s) as specified in {@link OperaColors}. <p/> TODO: Needs
   * abstraction layer
   */
  ScreenShotReply containsColor(Canvas canvas, long timeout, OperaColors... colors);

  /**
   * Take a screenshot of specified {@link com.opera.core.systems.scope.protos.ExecProtos.Area} and
   * compare it with the given hashes (keys).
   */
  ScreenShotReply screenWatcher(Canvas canvas, long timeout, boolean includeImage,
                                String... hashes);

}