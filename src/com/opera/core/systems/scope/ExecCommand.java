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
 * This enum maps the commands for the <a href=
 * "http://dragonfly.opera.com/app/scope-interface/services/Exec/Exec_2_0.html"
 * >Exec 2.0</a>.
 *
 */
public enum ExecCommand implements ICommand {
  EXEC(1), GET_ACTION_LIST(2), SETUP_SCREEN_WATCHER(3), SEND_MOUSE_ACTION(5), DEFAULT(
      -1);

  private int code;
  private static final Map<Integer, ExecCommand> lookup = new HashMap<Integer, ExecCommand>();

  static {
    for (ExecCommand command : EnumSet.allOf(ExecCommand.class))
      lookup.put(command.getCommandID(), command);
  }

  private ExecCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "exec";
  }

  public static ExecCommand get(int code) {
    ExecCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}
