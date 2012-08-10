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

package com.opera.core.systems.scope.internal;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.openqa.selenium.Keys;

import java.util.Map;

/**
 * Maps Selenium WebDriver {@link Keys} to keys supported and recognized by Opera.
 */
public enum OperaKey {

  NULL("null"),
  CANCEL("cancel"),  // ^break
  HELP("help"),
  BACK_SPACE("backspace"),
  TAB("Tab"),
  CLEAR("clear"),
  RETURN("return"),
  ENTER("enter"),
  SHIFT("Shift"),
  LEFT_SHIFT(SHIFT.value),  // alias
  CONTROL("control"),
  LEFT_CONTROL(CONTROL.value),  // alias
  ALT("alt"),
  LEFT_ALT(ALT.value),  // alias
  PAUSE("pause"),
  ESCAPE("escape"),
  SPACE(" "),
  PAGE_UP("page up"),
  PAGE_DOWN("page down"),
  END("end"),
  HOME("home"),
  LEFT("left"),
  ARROW_LEFT(LEFT.value),  // alias
  UP("up"),
  ARROW_UP(UP.value),  // alias
  RIGHT("right"),
  ARROW_RIGHT(RIGHT.value),  // alias
  DOWN("down"),
  ARROW_DOWN(DOWN.value),  // alias
  INSERT("insert"),
  DELETE("del"),
  SEMICOLON(";"),
  EQUALS("="),

  // Number pad keys
  NUMPAD0("0"),
  NUMPAD1("1"),
  NUMPAD2("2"),
  NUMPAD3("3"),
  NUMPAD4("4"),
  NUMPAD5("5"),
  NUMPAD6("6"),
  NUMPAD7("7"),
  NUMPAD8("8"),
  NUMPAD9("9"),
  MULTIPLY("*"),
  ADD("+"),
  SEPARATOR(","),
  SUBTRACT("-"),
  DECIMAL("."),
  DIVIDE("/"),

  // Function keys
  F1("f1"),
  F2("f2"),
  F3("f3"),
  F4("f4"),
  F5("f5"),
  F6("f6"),
  F7("f7"),
  F8("f8"),
  F9("f9"),
  F10("f10"),
  F11("f11"),
  F12("f12");

  private static final Map<Keys, OperaKey> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<OperaKey, Keys>() {
        public Keys apply(OperaKey key) {
          return Keys.valueOf(key.name());
        }
      });

  private final String value;

  private OperaKey(String value) {
    this.value = value;
  }

  // TODO: rename to something better
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%s -> %s", name(), getValue());
  }

  /*
  private static final Map<String, String> lookup = Maps.newHashMap();

  static {
    for (OperaKey key : EnumSet.allOf(OperaKey.class)) {
      lookup.put(key.name(), key.value);
    }
  }

  public static String get(String key) {
    return lookup.get(key);
  }
  */

  public static OperaKey get(Keys key) {
    return lookup.get(key);
  }

}