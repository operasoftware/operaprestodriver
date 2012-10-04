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
import com.opera.core.systems.scope.services.desktop.DesktopWindowManager;

import java.util.Map;

/**
 * This enum maps the commands for the <a href="http://dragonfly.opera.com/app/scope-interface/services/ScopeDesktopWindowManager/DesktopWindowManager_2_0.html"
 * >Window Manager 2.0</a>.
 */
public enum DesktopWindowManagerMessage implements Message {

  GET_ACTIVE_WINDOW(1),
  LIST_WINDOWS(2),
  LIST_QUICK_WIDGETS(3),
  GET_QUICK_WIDGET(4),
  WINDOW_SHOWN(5), // event
  WINDOW_UPDATED(6), // event
  WINDOW_CLOSED(7), // event
  WINDOW_ACTIVATED(8), // event
  WINDOW_LOADED(9), // event
  LIST_QUICK_MENUS(10),
  MENU_SHOWN(11), // event
  MENU_CLOSED(12), // event
  PRESS_QUICK_MENU(13),
  MENU_PRESSED(14), // event
  WINDOW_PAGE_CHANGED(15),

  DEFAULT(-1);

  private static final Map<Integer, DesktopWindowManagerMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()),
                       new Function<DesktopWindowManagerMessage, Integer>() {
                         public Integer apply(DesktopWindowManagerMessage message) {
                           return message.getID();
                         }
                       });

  private final int code;

  private DesktopWindowManagerMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return DesktopWindowManager.SERVICE_NAME;
  }

  public static DesktopWindowManagerMessage get(int code) {
    DesktopWindowManagerMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}
