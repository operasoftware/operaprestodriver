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

package com.opera.core.systems.scope.services.messages;

import com.opera.core.systems.scope.Message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands for the <a href= "http://dragonfly.opera.com/app/scope-interface/services/Exec/Exec_2_0.html"
 * >Exec 2.0</a>.
 */
public enum ExecMessage implements Message {

  EXEC(1),
  GET_ACTION_LIST(2),
  SETUP_SCREEN_WATCHER(3),
  SEND_MOUSE_ACTION(5),
  DEFAULT(-1);

  private int code;
  private static final Map<Integer, ExecMessage> lookup = new HashMap<Integer, ExecMessage>();

  static {
    for (ExecMessage message : EnumSet.allOf(ExecMessage.class)) {
      lookup.put(message.getID(), message);
    }
  }

  private ExecMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "exec";
  }

  public static ExecMessage get(int code) {
    ExecMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}