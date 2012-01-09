/*
Copyright 2011-2012 Opera Software ASA

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OperaStrings should be considered a supplement to {@link com.google.common.base.Strings} in
 * Guava.
 */
public class OperaStrings {

  /**
   * Checks whether given string has a numeric value. A numeric value may be "0", "1" or "1234".
   * 
   * @param string the string to check
   * @return true if string holds a numeric value, false otherwise
   */
  public static boolean isNumeric(String string) {
    if (string == null) return false;
    try {
      double d = Double.parseDouble(string);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   * Escape characters for safe insertion in a Javascript string contained by double quotes (").
   *
   * @param string the string to escape
   * @return an escaped string
   */
  public static String escapeJsString(String string) {
    return escapeJsString(string, "\"");
  }

  /**
   * Escape characters for safe insertion in a JavaScript string.
   *
   * @param string the string to escape
   * @param quote  the type of quote to escape. Either " or '
   * @return the escaped string
   */
  public static String escapeJsString(String string, String quote) {

    /*
     * This should be expanded to match all invalid characters (e.g. newlines) but for the moment
     * we'll trust we'll only get quotes.
     */
    Pattern escapePattern = Pattern.compile("([^\\\\])" + quote);

    /*
     * Prepend a space so that the regex can match quotes at the beginning of the string.
     */
    Matcher m = escapePattern.matcher(" " + string);
    StringBuffer sb = new StringBuffer();

    while (m.find()) {
      /*
       * $1 -> inserts the character before the quote \\\\\" -> \\", apparently just \" isn't
       * treated literally.
       */
      m.appendReplacement(sb, "$1\\\\" + quote);
    }

    m.appendTail(sb);

    // Remove the prepended space.
    return sb.substring(1);
  }

}