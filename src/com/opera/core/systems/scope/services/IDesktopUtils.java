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

/**
 * @author Adam Minchinton, Karianne Ekern
 */

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
   * @return string corresponding to the enumText
   */
  String getString(String enumText, boolean stripAmpersand);

  /**
   * Used to stop problems comparing strings that have just \n with those with \r\n.
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

  /**
   * Fetches a translated string fetched basing on a string id and substitutes the printf formatters in it.
   * Each substitution argument needs to be a string. The printf formatter types are ignored, and the given
   * string argument is substituted.
   * The method distinguishes between ordered substitution (i.e. %1, %2) and a standard substitution (i.e.
   * %s, %d).
   * In case the given argument list is too long or too short for the number of arguments in the fetched
   * translated string, this method triggers a warning. In case the given argument list is too short,
   * the method substitutes as many formatters as possible and leaves the rest of them intact.
   * "%%" is substituted to "%".
   * If a given argument is empty, i.e. "", it will be substituted to ANY_MATCHER, allowing later searching
   * for the text without specific formatter values, i.e. matching "Show _ANY_ more..." to any string like
   * "Show 1 more...", "Show 2 more...".
   *
   * @param args An array containing the printf substitution arguments, all of string type, and the string id
   *             as the first element.
   * @param stripAmpersand Whether to strip the "&" character from the fetched string.
   * @return The final substituted string.
   */
  String getSubstitutedString(String[] args, boolean stripAmpersand);

  /**
   * Fetches the integer describing the Opera Desktop product type. This may, however doesn't have to,
   * be available via the System|OperaProduct preference.
   *
   * The currently known values are:
   * 0: Opera
   * 1: Opera Next
   * 2: Opera Labs
   *
   * @return One of the known integer values.
   */
  Integer getDesktopProductType();

  /**
   * In case the desktop product type is 2 (i.e. Opera Labs), this method returns the string
   * describing the labs release, i.e. "Camera API".
   *
   * @return String describing the Labs release or an empty string in case the connected Opera
   * browser is not a Labs product, or the string has not been set for the given Labs release.
   */
  String getDesktopLabsProductName();

  /**
   * The desktop package type as string, i.e. "exe" for Windows.
   *
   * @return The package type string.
   */
  String getDesktopProductPackageType();

  /**
   * The Opera Desktop version number has the format MM.NN.BBBB, where MM is the major version
   * number, NN is the minor version number and BBBB is the actual build number, i.e. 11.50.4202.
   *
   * @return The major version number as an integer.
   */
  Integer getDesktopVersionMajor();

  /**
   * The Opera Desktop version number has the format MM.NN.BBBB, where MM is the major version
   * number, NN is the minor version number and BBBB is the actual build number, i.e. 11.50.4202.
   *
   * @return The minor version number as an integer.
   */
  Integer getDesktopVersionMinor();

  /**
   * The Opera Desktop version number has the format MM.NN.BBBB, where MM is the major version
   * number, NN is the minor version number and BBBB is the actual build number, i.e. 11.50.4202.
   *
   * @return The build number as an integer.
   */
  Integer getDesktopVersionBuildNo();
}
