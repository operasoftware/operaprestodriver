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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
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
   * This method will try to locate the launcher on any system.  If the OPERA_LAUNCHER environment
   * variable is set but invalid, it will throw an exception.  If that is not the case, it will
   * attempt to extract the launcher from OperaDriver's resources or source directory into a
   * directory on your system if it doesn't already exist or if the launcher is outdated.
   *
   * @return the path to the launcher
   * @throws WebDriverException if launcher is not found
   */
  public String launcherPath() {
    String path = System.getenv("OPERA_LAUNCHER");

    if (!isPathValid(path) && (path != null && path.length() > 0)) {
      throw new WebDriverException("Path \"" + path + "\" in OPERA_LAUNCHER does not exist");
    } else if (!isPathValid(path)) {
      path = extractLauncher(FileUtils.getUserDirectoryPath() + File.separator + ".launcher");
    }

    return path;
  }

  /**
   * Extracts the launcher from either the JAR or the source directory into directory specified.  If
   * the launcher currently in that location is outdated, it will be updated.
   *
   * @param launcherPath directory where you wish to put the launcher
   * @return path to launcher executable
   */
  private String extractLauncher(String launcherPath) {
    String launcherName = getLauncherNameForOS();
    File sourceLauncher = null;
    File targetLauncher = new File(launcherPath + File.separator + launcherName);

    // Whether we need to copy a new launcher across, either because it doesn't currently exist, or
    // because its hash differs from our launcher.
    boolean copy;

    // Get the launcher resource from JAR
    URL res = OperaDriver.class.getClassLoader().getResource("launcher/" + launcherName);

    // Does launcher exist among our resources?
    if (res != null) {
      logger.finer("launcher found among resources");

      String url = res.toExternalForm();

      if ((url.startsWith("jar:")) || (url.startsWith("wsjar:"))) {
        sourceLauncher = new File(url);
      }
    } else {
      // If launcher is not inside jar, the user is probably running OperaDriver from the source
      // directory or from an IDE.  This means we can copy the launcher directly from the source
      // folder.
      logger.finer("Looking for launcher in source directory");

      File
          outputPath =
          new File(OperaDriver.class.getProtectionDomain().getCodeSource().getLocation().getPath());

      try {
        sourceLauncher = findLauncherInRepository(outputPath);
      } catch (FileNotFoundException e) {
        throw new WebDriverException("Unable to find launcher above the \"" + outputPath + "\" " +
                                     "output path, please extract the launcher manually or run " +
                                     "OperaDriver from the JAR the first time");
      }
    }

    // Copy launcher if it doesn't exist or if the current launcher on the system doesn't match the
    // one bundled with OperaDriver (launcher needs to be upgraded).
    if (!targetLauncher.exists()) {
      copy = true;
      logger.fine("No launcher found, copying");
    } else {
      try {
        if (res != null) {
          copy = !Arrays.equals(md5(targetLauncher), md5(res.openStream()));
        } else {
          copy = !Arrays.equals(md5(targetLauncher), md5(sourceLauncher));
        }
      } catch (Exception e) {
        copy = true;
        logger.fine("Old launcher detected, upgrading");
      }
    }

    if (copy) {
      try {
        if (!targetLauncher.exists()) {
          new File(launcherPath).mkdirs();
          FileUtils.touch(targetLauncher);
        }

        InputStream is = (res != null) ? res.openStream() : new FileInputStream(sourceLauncher);
        OutputStream os = new FileOutputStream(targetLauncher);

        IOUtils.copy(is, os);

        is.close();
        os.close();

        targetLauncher.setLastModified(targetLauncher.lastModified());
      } catch (IOException e) {
        throw new WebDriverException("Cannot write file to disk: " + e.getMessage());
      }

      logger.fine("New launcher copied to " + targetLauncher.getAbsolutePath());
    }

    if (copy && targetLauncher != null) {
      makeLauncherExecutable(targetLauncher);
    }

    return targetLauncher.getAbsolutePath();
  }

  /**
   * Makes the launcher executable by chmod'ing the file at given path (GNU/Linux and Mac only).
   *
   * @param launcher the file to make executable
   */
  private void makeLauncherExecutable(File launcher) {
    Platform current = Platform.getCurrent();
    if (current.is(Platform.LINUX) || current.is(Platform.MAC)) {
      CommandLine line = new CommandLine("chmod", "755", launcher.getAbsolutePath());
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
   * @throws java.security.NoSuchAlgorithmException if MD5 is not available
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
   * Get the MD5 hash of the given file.
   *
   * @param file file to compute a hash on
   * @return a byte array of the MD5 hash
   * @throws FileNotFoundException if file cannot be found
   * @since v0.8
   */
  private static byte[] md5(File file) throws Exception {
    return md5(new FileInputStream(file));
  }

  /**
   * Finds the launcher executable in the source code repository of OperaDriver.  This is typically
   * used when running OperaDriver from an IDE for the first time, where you do not have a launcher
   * extracted to ~/.launcher yet.
   *
   * Please note that this _will not_ work if your IDE's output path is set outside of the
   * OperaDriver root.
   *
   * @param outputPath the build/IDE output path
   * @return the launcher for this OS
   * @since v0.8
   * @throws FileNotFoundException if launcher cannot be found
   */
  private File findLauncherInRepository(File outputPath) throws FileNotFoundException {
    File launcher = new File(findLauncherDirectoryInRepository(outputPath) +
                             File.separator + getLauncherNameForOS());

    if (launcher.exists()) {
      return launcher;
    } else {
      throw new FileNotFoundException();
    }
  }

  /**
   * Finds the directory with launchers in the source code repository of OperaDriver.  This is
   * typically used when running OperaDriver from an IDE for the first time, where you do not have a
   * launcher extracted to ~/.launcher yet.
   *
   * Please note that this _will not_ work if your IDE's output path is set outside of the
   * OperaDriver root.  This method will check if there is a path called ./launcher in the current
   * directory, and in the three directories above that in the file system hierarchy.
   *
   * @param outputPath the build/IDE output path
   * @return the directory that contains the launchers
   * @since v0.8
   */
  private File findLauncherDirectoryInRepository(File outputPath) {
    FilenameFilter launcherDirectoryFilter = new FilenameFilter() {
      public boolean accept(File file, String name) {
        return name.equals("launcher");
      }
    };

    File parent = new File(outputPath.getParent());
    File current = null;

    for (int i = 3; i >= 0; i--) {
      current = new File(parent.getParent());
      String[] currentChildren = current.list(launcherDirectoryFilter);

      if (currentChildren.length == 1) {
        current = new File(current.getAbsolutePath() + File.separator + "launcher");
        break;
      }

      parent = current;
    }

    if (current != null) {
      return current;
    }

    return null;
  }

}