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

package com.opera.core.systems.scope.stp.services.messages;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.Message;

import java.util.Map;

/**
 * This enum maps the commands for the <a href= "http://dragonfly.opera.com/app/scope-interface/services/Scope/Scope_1_0.html"
 * >Scope 1.0</a>.
 */
public enum ScopeMessage implements Message {

  CONNECT(3),
  DISCONNECT(4),
  ENABLE(5),
  DISABLE(6),
  INFO(7),
  QUIT(8),
  HOST_INFO(10),
  MESSAGE_INFO(11),
  DEFAULT(-1);

  private static final Map<Integer, ScopeMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<ScopeMessage, Integer>() {
        public Integer apply(ScopeMessage message) {
          return message.getID();
        }
      });

  private final int code;

  private ScopeMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "scope";
  }

  public static ScopeMessage get(int code) {
    ScopeMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}