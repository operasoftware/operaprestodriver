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

package com.opera.core.systems.internal;

import com.google.common.io.Files;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * This class contains any utility methods that doesn't really seem to fit anywhere else.
 *
 * TODO(andreastt): This needs to go
 */
public class WatirUtils {

  public static final String ANY_MATCHER = "_ANY_";

  private static final Logger logger = Logger.getLogger(WatirUtils.class.getName());

  /**
   * Returns the platform double click timeout, that is the time that separates two clicks treated
   * as a double click from two clicks treated as two single clicks.
   */
  public static Integer getSystemDoubleClickTimeMs() {
    Integer DEFAULT_INTERVAL_MS = 500;
    Toolkit t = Toolkit.getDefaultToolkit();

    if (t == null) {
      return DEFAULT_INTERVAL_MS;
    }

    Object o = t.getDesktopProperty("awt.multiClickInterval");
    if (o == null) {
      return DEFAULT_INTERVAL_MS;
    }

    return (Integer) o;
  }

  /**
   * Sanitize the given filename/path, so that (1) it is not surrounded with quotation marks "", (2)
   * all occurences of "\\" are changed to a "/", and (3) all occurences of "\" are changed to a
   * "/".
   *
   * @param input the input path
   * @return a StringBuffer containing the sanitized input path
   */
  public static StringBuffer sanitizePath(String input) {
    StringBuffer buf = new StringBuffer();

    // Strip any surrounding quotation marks, that might have came with the file name from
    // any external source like the Windows environment variable.
    if (input.matches("^\".+\"$")) {
      input = input.substring(1, input.length() - 1);
    }

    // Make sure we use "/" as the path separator, seems to be the best solution. Also, strip double
    // "\\" used as separators.
    input = input.replaceAll("\\\\\\\\", "/");
    input = input.replaceAll("\\\\", "/");

    buf.append(input);
    return buf;
  }

  /**
   * Copies the whole disk directory/file structure starting from the source path to the destination
   * path.
   *
   * @param source      the source path, may designate either a file or a directory
   * @param destination the destination path
   */
  public static boolean copyDirAndFiles(File source, File destination) {
    logger.finest(String.format("WatirUtils::copyDirAndFiles(%s, %s)", source.getAbsolutePath(),
                                destination.getAbsolutePath()));

    if (source.isDirectory()) {
      String[] items;
      items = source.list();
      for (String item : items) {
        File itemSource = new File(source.getPath(), item);
        File itemDestination = new File(destination.getPath(), item);
        boolean res = copyDirAndFiles(itemSource, itemDestination);
        if (!res) {
          logger.severe(String.format("Could not copy \"%s\" to \"%s\"",
                                      itemSource.getPath(), itemDestination.getPath()));
          return false;
        }
      }
    } else {
      try {
        Files.createParentDirs(destination);
        Files.copy(source, destination);
      } catch (IOException e) {
        logger.severe(
            String.format("Could not copy files from \"%s\" to \"%s\"",
                          source.getPath(), destination.getPath()));
        return false;
      }
    }

    return true;
  }

  /**
   * Compares haystack and needle taking into the account that the needle may contain any number of
   * ANY_MATCHER occurrences, that will be matched to any substring in haystack, i.e. "Show _ANY_
   * more..." will match anything like "Show 1 more...", "Show 2 more..." and so on.
   *
   * @param haystack the text that will be compared, may not contain any ANY_MATCHER occurrence
   * @param needle   the text that will be used for comparision, may contain any number of
   *                 ANY_MATCHER occurrences
   * @return a boolean indicating whether needle matched haystack.
   */
  public static boolean textMatchesWithANY(String haystack, String needle) {
    haystack = haystack.trim();
    needle = needle.trim();

    // Make sure we escape every character that is considered to be a special character inside a
    // regular expression.  Matcher.quoteReplacement() takes care of '\\' and '$'.
    needle = Matcher.quoteReplacement(needle);
    String chars_to_be_escaped = ".|*?+(){}[]^";

    for (char c : chars_to_be_escaped.toCharArray()) {
      String regex = "\\" + c;
      String replacement = "\\\\" + c;
      needle = needle.replaceAll(regex, replacement);
    }

    String pattern = needle.replaceAll(ANY_MATCHER, "(?:.+)");
    logger.finest("Looking for pattern '" + pattern + "' in '" + haystack + "'");

    return haystack.matches(pattern);
  }

}