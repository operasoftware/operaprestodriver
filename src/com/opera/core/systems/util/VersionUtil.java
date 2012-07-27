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

package com.opera.core.systems.util;

/**
 * A replacement for a subset of {@link com.sun.xml.internal.ws.util.VersionUtil}.
 */
public class VersionUtil {

  /**
   * Returns a negative number if the first version is lower than the second, zero if they are
   * equal, and a positive number if the first version is higher than the second.
   *
   * @param a first version
   * @param b second version
   * @return -1, 0 or 1 based upon the comparison results:  -1 if a is less than b, 0 if a is equal
   *         to b 1 if a is greater than b
   */
  public static int compare(String a, String b) {
    int shortestLength = (a.length() < b.length()) ? a.length() : b.length();

    for (int i = 0; i < shortestLength; i++) {
      if (a.charAt(i) < b.charAt(i)) {
        return -1;
      } else if (a.charAt(i) > b.charAt(i)) {
        return 1;
      }
    }

    // Check for more specific versions, assume the more specific version to be higher.  So, "2.0.1"
    // is higher than "2.0", and "2.0.0" is higher than "2.0".
    if (a.length() > b.length()) {
      return 1;
    } else if (a.length() < b.length()) {
      return -1;
    }

    return 0;
  }

}