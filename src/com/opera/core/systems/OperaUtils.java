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

package com.opera.core.systems;

import com.opera.core.systems.OperaDriver.PrivateData;

import org.openqa.selenium.Platform;

/**
 * Interface used for accessing the browser's meta- and utility information, such as the operating
 * system it's running on, its user agent string, &c.
 */
public interface OperaUtils {

  /**
   * Which Core version this instance of the browser is using, e.g. "2.8.119".
   *
   * @return version number
   */
  String getCoreVersion();

  /**
   * The platform currently used.
   *
   * @return operating system identifier
   */
  Platform getPlatform();

  /**
   * Gets the current product.  For regular desktop builds this will be {@link
   * OperaProduct#DESKTOP}.  Other examples are {@link OperaProduct#MOBILE} and {@link
   * OperaProduct#CORE}.
   */
  OperaProduct getProduct();

  /**
   * The full path to the currently running binary.
   *
   * @return full path to browser
   */
  String getBinaryPath();

  /**
   * The User-Agent string.  Typically something like <code>Opera/9.80 (Windows NT 6.1; U; en)
   * Presto/2.7.62 Version/11.01</code>.
   *
   * @return User-Agent string
   */
  String getUserAgent();

  /**
   * The ID of the process we're currently talking to.  Might not be present if the build does not
   * support retrieving process IDs.
   *
   * @return process ID, or null if not available
   */
  Integer getPID();

  /**
   * Clear private data stored by Opera by type of data or by all.
   *
   * @param flags a variable list of flags can be used, or the value ALL to clean all stored data
   */
  void clearPrivateData(PrivateData... flags);

}