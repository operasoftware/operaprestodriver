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
*/
package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands for the <a href="http://dragonfly.opera.com/app/scope-interface/services/WindowManager/WindowManager_2_0.html"
 * >Window Manager 2.0</a>.
 */
public enum WindowManagerCommand implements ICommand {
  // Commands
  GET_ACTIVE_WINDOW(1),
  LIST_WINDOWS(2),
  MODIFY_FILTER(3),
  // Events
  WINDOW_UPDATED(4),
  WINDOW_CLOSED(5),
  WINDOW_ACTIVATED(6),
  WINDOW_LOADED(7),
  // Commands
  CREATE_WINDOW(8),
  CLOSE_WINDOW(9),
  OPEN_URL(10),
  MODIFY_TYPE_FILTER(11),

  DEFAULT(-1);

  private int code;
  private static final
  Map<Integer, WindowManagerCommand>
      lookup =
      new HashMap<Integer, WindowManagerCommand>();

  static {
    for (WindowManagerCommand command : EnumSet.allOf(WindowManagerCommand.class)) {
      lookup.put(command.getCommandID(), command);
    }
  }

  private WindowManagerCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "window-manager";
  }

  public static WindowManagerCommand get(int code) {
    WindowManagerCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}
