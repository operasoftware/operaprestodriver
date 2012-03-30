/*
Copyright 2012 Opera Software ASA

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

package com.opera.core.systems.common.lang;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extension to {@link java.lang.Boolean} which implements some convenience methods for interacting
 * with "truthy" values from other, typically weakly typed, languages.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public final class OperaBoolean {

  /**
   * Parses given string and returns its value as a boolean.  A string which equals "1" or "true" is
   * considered to be truthy.
   *
   * @param string string to parse
   * @return true if string is truthy, false otherwise
   * @throws IllegalArgumentException if parameter is not a boolesque value ("1", "true", "0",
   *                                  "false")
   * @throws NullPointerException     if parameter is null
   */
  public static boolean parseBoolean(String string) {
    checkNotNull(string);
    assertBoolesque(string);
    return isTruthy(string);
  }

  /**
   * Whether string holds a boolean-like value.  It should equal "0", "1", "true" or "false".  This
   * method says nothing about whether the object is true or false.
   *
   * @param string string to check
   * @return true if value is "boolesque", false otherwise
   * @throws IllegalArgumentException if parameter is not a boolesque value ("1", "true", "0",
   *                                  "false")
   * @throws NullPointerException     if parameter is null
   */
  public static boolean isBoolesque(String string) {
    checkNotNull(string);
    assertBoolesque(string);
    return isFalsy(string) || isTruthy(string);
  }

  private static boolean isTruthy(String string) {
    return string.equals("1") || string.equals("true");
  }

  private static boolean isFalsy(String string) {
    return string.equals("0") || string.equals("false");
  }

  private static void assertBoolesque(String string) {
    checkArgument((isTruthy(string) || isFalsy(string)), "Not a boolesque value: " + string);
  }

}