/*
Copyright 2008-2011 Opera Software ASA

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
package com.opera.core.systems.util;

/**
 * A replacement for a subset of com.sun.xml.ws.util.VersionUtil.
 * 
 * @author Jan Vidar Krey
 */
public class VersionUtil {

  /**
   * Compares the versions given in string format
   * 
   * @param a version1
   * @param b version2
   * @return -1, 0 or 1 based upon the comparison results -1 if version1 is less
   *         than version2 0 if version1 is equal to version2 1 if version1 is
   *         greater than version2
   */

  // This class should always be accessed through static methods
  private VersionUtil() {
  }

  public static int compare(String a, String b) {
    int minlen = (a.length() < b.length()) ? a.length() : b.length();

    for (int i = 0; i < minlen; i++) {
      if (a.charAt(i) < b.charAt(i)) return -1;
      else if (a.charAt(i) > b.charAt(i)) return 1;
    }

    // Check for more specific versions, assume the more specific
    // version to be higher.
    // So, "2.0.1" is higher than "2.0", and "2.0.0" is higher than "2.0".
    if (a.length() > b.length()) return 1;
    if (a.length() < b.length()) return -1;

    return 0;
  }

}
