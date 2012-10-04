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

import java.util.Map;

public enum DesktopUtilsMessage implements Message {

  GET_STRING(1),
  GET_OPERA_PATH(2),
  GET_LARGE_PREFERENCES_PATH(3),
  GET_SMALL_PREFERENCES_PATH(4),
  GET_CACHE_PREFERENCES_PATH(5),
  GET_OPERA_PID(6),

  DEFAULT(-1);

  private static final Map<Integer, DesktopUtilsMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()),
                       new Function<DesktopUtilsMessage, Integer>() {
                         public Integer apply(DesktopUtilsMessage message) {
                           return message.getID();
                         }
                       });

  private final int code;

  private DesktopUtilsMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return "desktop-utils";
  }

  public static DesktopUtilsMessage get(int code) {
    DesktopUtilsMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}