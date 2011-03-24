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

package com.opera.core.systems.scope.services;

public interface IDesktopUtils {
  void init();

  /**
   * @return Full path to the Opera binary
   */
  String getOperaPath();

  /**
   * @return Full path to large preferences folder
   */
  String getLargePreferencesPath();

  /**
   * @return Full path to small preferences folder
   */
  String getSmallPreferencesPath();

  /**
   * @return Full path to cache preferences folder
   */
  String getCachePreferencesPath();

  /**
   * Gets the string specified by the given string id.
   *
   * @param enumText
   * @return string corresponding to the enumText
   */
  String getString(String enumText, boolean stripAmpersand);

  /**
   * Used to stop problems comparing strings that have just \n with those with
   * \r\n.
   *
   * @param text to remove the \r's from
   * @return string without \r's
   */
  String removeCR(String text);

  /**
   * Gets process id of connected Opera instance.
   *
   * @return pid
   */
  int getOperaPid();

}
