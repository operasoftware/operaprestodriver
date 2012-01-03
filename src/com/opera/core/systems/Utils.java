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

/**
 * Interface for accessing the browser's meta- and utility information, such as the operating system
 * it's running on, its user agent string, &c.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public interface Utils {

  /**
   * Which Core version this instance of the browser is using, e.g. "2.8.119".
   *
   * @return version number
   */
  String getCoreVersion();

  /**
   * A string which describes the operating system, e.g. "Windows NT 6.1".
   *
   * @return operating system identifier
   */
  String getOS();

  /**
   * A string which describes the product.  For regular desktop builds this will be "desktop".
   * Other examples are "mobile" and "gogi".
   *
   * @return browser's product type
   */
  String getProduct();

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
   * @return User Agent string
   */
  String getUserAgent();

  /**
   * The ID of the process we're currently talking to.  Might not be present if the build does not
   * support retrieving process IDs.
   *
   * @return process ID
   */
  int getPID();

}