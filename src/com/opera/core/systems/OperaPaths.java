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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.UNIX;

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
  private static final Platform currentPlatform = Platform.getCurrent();

  /**
   * This method will try and find Opera on any platform.  It performs the following steps:
   *
   * 1. Check the environment variable OPERA_PATH.  If it exists, and the file it points to exists,
   *    then return
   *
   * 2. Check if Opera exists at the default location on the respective OS
   *
   * 3. (Unix) Call `which opera` to find the location 4. Give up and return null
   *
   * @return the path to Opera, or null
   */
  public static String operaPath() {
    String envPath = System.getenv("OPERA_PATH");

    if (isPathValid(envPath)) {
      return envPath;
    } else if (!isPathValid(envPath) && (envPath != null && envPath.length() > 0)) {
      throw new WebDriverException("Path \"" + envPath + "\" in OPERA_PATH does not exist");
    }

    List<String> paths = new ArrayList<String>();

    switch (currentPlatform) {
      case LINUX:
      case UNIX:
        paths.add(which("opera"));
        paths.add(which("opera-next"));
        paths.add("/usr/bin/opera");
        paths.add("/usr/bin/opera-next");
        break;

      case MAC:
        paths.add("/Applications/Opera.app/Contents/MacOS/Opera");
        paths.add("/Applications/Opera Next.app/Contents/MacOS/Opera");
        break;

      case WINDOWS:
      case XP:
      case VISTA:
        String programFiles = getWindowsProgramFilesDirectory();
        paths.add(programFiles + "\\Opera\\opera.exe");
        paths.add(programFiles + "\\Opera Next\\opera.exe");
        break;

      default:  // Android?
        throw new WebDriverException("Unable to resolve the path to Opera on this platform");
    }

    for (String path : paths) {
      if (isPathValid(path)) {
        return path;
      }
    }

    return null;
  }

  /**
   * Check that the given path is not null, not empty and that it exists.
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

  /**
   * Locates a specified program using the `which` program on UNIX or LINUX platforms.  If no
   * program is found, it will return null.
   *
   * @param program the program binary to find
   * @return the absolute path to the binary, or null if program is not found
   */
  private static String which(String program) {
    if (!currentPlatform.is(UNIX) && !currentPlatform.is(LINUX)) {
      throw new WebDriverException("Executing program `which` not possible on this platform");
    }

    CommandLine which = new CommandLine("which", program);
    which.execute();
    return which.getStdOut().trim();
  }

  /**
   * Returns the Program Files directory on the Windows platform.  It looks for the 32-bit directory
   * first, then the 64-bit directory.
   *
   * @return the Program Files directory on Windows
   */
  private static String getWindowsProgramFilesDirectory() {
    String x86 = System.getenv("ProgramFiles(x86)");
    String programFiles = (x86 == null) ? System.getenv("PROGRAMFILES") : x86;

    if (programFiles == null) {
      programFiles = "\\Program Files";
    }

    return programFiles;
  }

}