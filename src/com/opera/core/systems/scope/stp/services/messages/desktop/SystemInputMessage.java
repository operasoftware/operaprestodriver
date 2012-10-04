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

package com.opera.core.systems.scope.stp.services.messages.desktop;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.Message;
import com.opera.core.systems.scope.services.desktop.SystemInput;

import java.util.Map;

public enum SystemInputMessage implements Message {

  CLICK(1),
  KEYPRESS(2),
  KEYUP(3),
  KEYDOWN(4),
  MOUSEDOWN(5),
  MOUSEUP(6),
  MOUSEMOVE(7),
  DEFAULT(-1);

  private static final Map<Integer, SystemInputMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<SystemInputMessage, Integer>() {
        public Integer apply(SystemInputMessage message) {
          return message.getID();
        }
      });

  private final int code;

  private SystemInputMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return SystemInput.SERVICE_NAME;
  }

  public static SystemInputMessage get(int code) {
    SystemInputMessage command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}