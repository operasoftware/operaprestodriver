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

import com.opera.core.systems.scope.Message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CookieManagerMessage implements Message {

  GET_COOKIE          ( 1),
  REMOVE_COOKIE       ( 2),
  REMOVE_ALL_COOKIES  ( 3),
  GET_COOKIE_SETTINGS ( 4),
  DEFAULT             (-1);

  private int code;
  private static final
  Map<Integer, CookieManagerMessage>
      lookup =
      new HashMap<Integer, CookieManagerMessage>();

  static {
    for (CookieManagerMessage command : EnumSet.allOf(CookieManagerMessage.class)) {
      lookup.put(command.getID(), command);
    }
  }

  private CookieManagerMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "cookie-manager";
  }

  public static CookieManagerMessage get(int code) {
    CookieManagerMessage command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}