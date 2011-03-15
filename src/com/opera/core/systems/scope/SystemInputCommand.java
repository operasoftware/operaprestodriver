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

public enum SystemInputCommand implements ICommand {
  CLICK(1), KEYPRESS(2), KEYUP(3), KEYDOWN(4), MOUSEDOWN(5), MOUSEUP(6), MOUSEMOVE(
      7), DEFAULT(-1);

  private int code;
  private static final Map<Integer, SystemInputCommand> lookup = new HashMap<Integer, SystemInputCommand>();

  static {
    for (SystemInputCommand command : EnumSet.allOf(SystemInputCommand.class))
      lookup.put(command.getCommandID(), command);
  }

  private SystemInputCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "system-input";
  }

  public static SystemInputCommand get(int code) {
    SystemInputCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}
