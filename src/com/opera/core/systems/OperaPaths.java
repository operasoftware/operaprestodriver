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

package com.opera.core.systems;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.util.logging.Logger;

/**
 * This class tries to find the paths to Opera and the launcher on any system.  If it cannot find a
 * launcher it will extract the appropriate one from the JAR it is in or attempt to copy it from the
 * source directory.
 *
 * @author Stuart Knightley <stuartk@opera.com>, Andreas Tolf Tolfsen <andreastt@opera.com>, Deniz
 *         Turkoglu <dturkoglu@opera.com>
 */
public class OperaPaths {

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * This method will try and find Opera on any system.  It takes the following steps:
   *
   *   1. Check the environment variable "OPERA_PATH".  If it exists, and the file it points to
   *      exists, then return
   *   2. Check if Opera exists at the default location on the respective OS
   *   3. (Unix) Call `which opera` to find the location
   *   4. Give up and return null
   *
   * @return the path to Opera, or null
   */
  public static String operaPath() {
    String path = System.getenv("OPERA_PATH");

    if (isPathValid(path)) {
      return path;
    } else if (!isPathValid(path) && (path != null && path.length() > 0)) {
      throw new WebDriverException("Path \"" + path + "\" in OPERA_PATH does not exist");
    }

    Platform platform = Platform.getCurrent();

    switch (platform) {
      case LINUX:
      case UNIX:
        path = "/usr/bin/opera";
        if (!isPathValid(path)) {
          CommandLine line = new CommandLine("which", "opera");
          line.execute();
          path = line.getStdOut().trim();
        }
        break;
      case MAC:
        path = "/Applications/Opera.app/Contents/MacOS/Opera";
        break;
      case WINDOWS:
      case XP:
      case VISTA:
        String x86 = System.getenv("ProgramFiles(x86)");
        String progfiles = (x86 == null) ? System.getenv("PROGRAMFILES") : x86;
        if (progfiles == null) {
          progfiles = "\\Program Files";
        }
        path = progfiles + "\\Opera\\opera.exe";
        break;
      default:
        throw new WebDriverException("Auto find is not support on this platform"); // android?
    }

    return (isPathValid(path)) ? path : null;
  }

  /**
   * Check that the given path is not null, and exists.
   *
   * @param path the path to check
   * @return true if the path is valid, false otherwise
   */
  public static boolean isPathValid(String path) {
    if (path == null) {
      return false;
    }

    if (path.length() == 0) {
      return false;
    }

    File file = new File(path);
    return (file.exists());
  }

}