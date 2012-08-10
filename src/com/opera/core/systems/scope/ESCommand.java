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

package com.opera.core.systems.scope;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.model.ICommand;

import java.util.Map;

public enum ESCommand implements ICommand {

  LIST_RUNTIMES(1),
  EVAL(2),
  EXAMINE_OBJECTS(3),
  RELEASE_OBJECTS(4),
  ON_READY_STATE_CHANGED(5),
  SET_FORM_ELEMENT_VALUE(6),

  DEFAULT(-1);

  private static final Map<Integer, ESCommand> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<ESCommand, Integer>() {
        public Integer apply(ESCommand command) {
          return command.getCommandID();
        }
      });

  private final int code;

  private ESCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public static ESCommand get(int code) {
    ESCommand command = lookup.get(code);
    if (command == null) {
      return DEFAULT;
    }
    return command;
  }

  public String getServiceName() {
    return "ecmascript";
  }

}