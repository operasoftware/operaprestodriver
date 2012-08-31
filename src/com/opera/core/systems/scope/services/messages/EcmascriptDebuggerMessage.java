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
 * This enum maps the commands for the <a href="http://dragonfly.opera.com/app/scope-interface/services/EcmascriptDebugger/EcmascriptDebugger_6_0.html"
 * >EcmascriptDebugger 6.0</a>.
 */
public enum EcmascriptDebuggerMessage implements Message {

  LIST_RUNTIMES(1), EVAL(3), INSPECT_DOM(13), EXAMINE_OBJECTS(4), SET_CONFIGURATION(
      10), RELEASE_OBJECTS(29), RUNTIME_STARTED(14), RUNTIME_STOPPED(15), NEW_SCRIPT(
      16), THREAD_STARTED(17), THREAD_FINISHED(18), THREAD_STOPPED_AT(19), HANDLE_EVENT(
      20), OBJECT_SELECTED(21), PARSE_ERROR(28), DEFAULT(-1);

  private int code;
  private static final
  Map<Integer, EcmascriptDebuggerMessage>
      lookup =
      new HashMap<Integer, EcmascriptDebuggerMessage>();

  static {
    for (EcmascriptDebuggerMessage message : EnumSet.allOf(EcmascriptDebuggerMessage.class)) {
      lookup.put(message.getID(), message);
    }
  }

  private EcmascriptDebuggerMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "ecmascript-debugger";
  }

  public static EcmascriptDebuggerMessage get(int code) {
    EcmascriptDebuggerMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}