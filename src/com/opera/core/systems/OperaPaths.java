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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This class tries to find the paths to Opera and Opera Launcher on any system. If it cannot find a
 * launcher it will extract the appropriate one from the jar it is in.
 *
 * @author Stuart Knightley <stuartk@opera.com>, Deniz Turkoglu <dturkoglu@opera.com>
 */
public class OperaPaths {

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * This method will try and find Opera on any system. It takes the following steps: 1. Check the
   * environment variable "OPERA_PATH". If it exists, and the file it points to exists, then return
   * 2. Check if Opera exists at the default location on the respective OS 3. (Unix) Call `which
   * opera` to find the location 4. Give up and return null
   *
   * @return the path to Opera, or null
   */
  public String operaPath() {
    String path = System.getenv("OPERA_PATH");

    if (isPathValid(path)) {
      return path;
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
   * This method will try and find Opera Launcher on any system.
   *
   * @return the path to Opera Launcher, or null
   */
  public String launcherPath() {
    String path = System.getenv("OPERA_LAUNCHER");
    if (!isPathValid(path)) {
      path = getOperaLauncherPath();
    }

    return path;
  }

  /**
   * Tries to load the launcher executable from the jar file.  A copy of the launcher is put into
   * user directory and on each call is compared to the content in jar file.
   *
   * @return path to launcher executable
   */
  public String getOperaLauncherPath() {
    String launcherName = getLauncherNameForOS();

    String executablePath = null;

    // Get the launcher path
    URL res = OperaDriver.class.getClassLoader().getResource(
        "launcher/" + launcherName);
    if (res != null) {
      String url = res.toExternalForm();
      // If the launcher is inside the jar we will need to copy it out
      if ((url.startsWith("jar:")) || (url.startsWith("wsjar:"))) {
        executablePath = FileUtils.getUserDirectoryPath() + File.separatorChar
                         + ".launcher" + File.separatorChar + launcherName;
        File cur_launcher = new File(executablePath);

        // Whether we need to copy a new launcher across, either because
        // it doesn't currently exist, or because its hash differs from
        // our launcher
        boolean copy = false;
        if (!cur_launcher.exists()) {
          copy = true;
        } else {
          try {
            // Copy if launchers don't match
            copy = !Arrays.equals(md5(executablePath), md5(res.openStream()));
          } catch (Exception e) {
            copy = true;
          }
        }

        if (copy == true) {
          try {
            if (!cur_launcher.exists()) {
              FileUtils.touch(cur_launcher);
            }

            InputStream is = res.openStream();
            OutputStream os = new FileOutputStream(cur_launcher);

            IOUtils.copy(is, os);

            is.close();
            os.close();

            cur_launcher.setLastModified(cur_launcher.lastModified());
          } catch (IOException e) {
            throw new WebDriverException("Cant write file to disk : "
                                         + e.getMessage());
          }
          logger.fine("New launcher copied");
        }
        // If launcher is not inside jar we can run it from it's current
        // location
      } else if (url.startsWith("file:")) {
        File f;
        try {
          f = new File(res.toURI());
        } catch (URISyntaxException e) {
          f = new File(res.getPath());
        }
        executablePath = f.getAbsolutePath();
      }
    }

    if (executablePath != null) {
      makeLauncherExecutable(executablePath);
    }

    return executablePath;
  }

  /**
   * Makes the launcher executable by chmod'ing the file at given path (GNU/Linux and Mac only).
   */
  private void makeLauncherExecutable(String executablePath) {
    Platform current = Platform.getCurrent();
    if (current.is(Platform.LINUX) || current.is(Platform.MAC)) {
      CommandLine line = new CommandLine("chmod", "755", executablePath);
      line.execute();
    }
  }

  /**
   * Get the launcher's binary file name based on what flavour of operating system and what kind of
   * architecture the user is using.
   *
   * @return the launcher's binary file name
   */
  private String getLauncherNameForOS() {
    boolean is64 = "64".equals(System.getProperty("sun.arch.data.model"));
    Platform currentPlatform = Platform.getCurrent();

    switch (currentPlatform) {
      case LINUX:
      case UNIX:
        return (is64 ? "launcher-linux-x86_64" : "launcher-linux-i686");
      case MAC:
        return "launcher-mac";
      case WINDOWS:
      case VISTA:
      case XP:
        return "launcher-win32-i86pc.exe";
      default:
        throw new WebDriverException(
            "Could not find a platform that supports bundled launchers, please set it manually");
    }
  }

  /**
   * Check that the given path is not null, and exists.
   *
   * @param path the path to check
   * @return true if the path is valid, false otherwise
   */
  private boolean isPathValid(String path) {
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
   * Get the MD5 hash of the given stream.
   *
   * @param fis the input stream to use
   * @return a byte array of the MD5 hash
   */
  private static byte[] md5(InputStream fis) throws Exception {
    byte[] buffer = new byte[1024];
    MessageDigest complete = MessageDigest.getInstance("MD5");

    int numRead;
    do {
      numRead = fis.read(buffer);
      if (numRead > 0) {
        complete.update(buffer, 0, numRead);
      }
    } while (numRead != -1);
    fis.close();

    return complete.digest();
  }

  /**
   * Get the MD5 hash of the given file path.
   *
   * @param filename the path to the file
   * @return a byte array of the MD5 hash
   */
  private static byte[] md5(String filename) throws Exception {
    return md5(new FileInputStream(filename));
  }
}