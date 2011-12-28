/*
Copyright 2011 Opera Software ASA

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

/**
 * OperaStrings should be considered a supplement to {@link com.google.common.base.Strings} in
 * Guava.
 */
public class OperaStrings {

  /**
   * Checks whether given string has a numeric value.  A numeric value may be "0", "1" or "1234".
   *
   * @param string the string to check
   * @return true if string holds a numeric value, false otherwise
   */
  public static boolean isNumeric(String string) {
    try {
      double d = Double.parseDouble(string);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

}