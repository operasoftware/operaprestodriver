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

public enum PrefsCommand implements ICommand {
  GET_PREF(1), LIST_PREFS(2), SET_PREF(3), DEFAULT(-1);

  private int code;
  private static final Map<Integer, PrefsCommand> lookup = new HashMap<Integer, PrefsCommand>();

  static {
    for (PrefsCommand command : EnumSet.allOf(PrefsCommand.class)) {
      lookup.put(command.getCommandID(), command);
    }
  }

  private PrefsCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "prefs";
  }

  public static PrefsCommand get(int code) {
    PrefsCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}
