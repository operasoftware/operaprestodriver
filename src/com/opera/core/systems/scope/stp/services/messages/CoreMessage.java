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

package com.opera.core.systems.scope.stp.services.messages;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.Message;

import java.util.Map;

public enum CoreMessage implements Message {

  ONACTIVE(1),
  ONIDLE(2),
  GET_BROWSER_INFORMATION(3),
  CLEAR_PRIVATE_DATA(4),

  DEFAULT(-1);

  private static final Map<Integer, CoreMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<CoreMessage, Integer>() {
        public Integer apply(CoreMessage command) {
          return command.getID();
        }
      });

  private final int code;

  private CoreMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "core"; //"core-utils";
  }

  public static CoreMessage get(int code) {
    CoreMessage command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}