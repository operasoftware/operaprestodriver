/*
 * Copyright 2008-2012 Opera Software ASA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public enum CoreUtilsCommand implements ICommand {

  ONACTIVE                ( 1),
  ONIDLE                  ( 2),
  GET_BROWSER_INFORMATION ( 3),
  DEFAULT                 (-1);

  private int code;
  private static final
  Map<Integer, CoreUtilsCommand>
      lookup =
      new HashMap<Integer, CoreUtilsCommand>();

  static {
    for (CoreUtilsCommand command : EnumSet.allOf(CoreUtilsCommand.class)) {
      lookup.put(command.getCommandID(), command);
    }
  }

  private CoreUtilsCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "core"; //"core-utils";
  }

  public static CoreUtilsCommand get(int code) {
    CoreUtilsCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}