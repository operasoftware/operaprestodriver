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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.Message;

import java.util.Map;

/**
 * This enum maps the commands for the <a href="http://dragonfly.opera.com/app/scope-interface/services/ScopeWindowManager/WindowManager_2_0.html"
 * >Window Manager 2.0</a>.
 */
public enum WindowManagerMessage implements Message {

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

  private static final Map<Integer, WindowManagerMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()),
                       new Function<WindowManagerMessage, Integer>() {
                         public Integer apply(WindowManagerMessage message) {
                           return message.getID();
                         }
                       });

  private final int code;

  private WindowManagerMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "window-manager";
  }

  public static WindowManagerMessage get(int code) {
    WindowManagerMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}