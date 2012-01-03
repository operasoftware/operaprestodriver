/*
Copyright 2011-2012 Opera Software ASA

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

public enum SelftestCommand implements ICommand {

  RUN_MODULES(1),
  OUTPUT(2),
  FINISHED(3);

  private int command;
  private static HashMap<Integer, SelftestCommand> lookup = new HashMap<Integer, SelftestCommand>();

  static {
    for (SelftestCommand command : EnumSet.allOf(SelftestCommand.class)) {
      lookup.put(command.getCommandID(), command);
    }
  }

  private SelftestCommand(int command) {
    this.command = command;
  }

  public int getCommandID() {
    return command;
  }

  public String getServiceName() {
    return "selftest";
  }

  public static SelftestCommand get(int id) {
    return lookup.get(id);
  }

}