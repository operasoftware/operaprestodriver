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

public enum EcmascriptMessage implements Message {

  LIST_RUNTIMES(1),
  EVAL(2),
  EXAMINE_OBJECTS(3),
  RELEASE_OBJECTS(4),
  ON_READY_STATE_CHANGED(5),
  SET_FORM_ELEMENT_VALUE(6),

  DEFAULT(-1);

  private static final Map<Integer, EcmascriptMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<EcmascriptMessage, Integer>() {
        public Integer apply(EcmascriptMessage command) {
          return command.getID();
        }
      });

  private final int code;

  private EcmascriptMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public static EcmascriptMessage get(int code) {
    EcmascriptMessage command = lookup.get(code);
    if (command == null) {
      return DEFAULT;
    }
    return command;
  }

  public String getServiceName() {
    return "ecmascript";
  }

}