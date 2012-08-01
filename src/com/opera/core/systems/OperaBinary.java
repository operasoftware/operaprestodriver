/*
Copyright 2012 Opera Software ASA
Portions copyright 2007-2012 Software Freedom Conservancy

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

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.os.WindowsUtils;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.of;
import static org.openqa.selenium.Platform.WINDOWS;

public class OperaBinary {

  public static final String OPERA_PATH_ENV_VAR = "OPERA_PATH";

  private static final Platform platform = Platform.getCurrent();

  private final File binary;

  public OperaBinary(OperaProduct product) {
    this(find(product));

    if (binary == null) {
      throw new WebDriverException("Cannot find Opera binary in your PATH or in OPERA_PATH");
    }
  }

  public OperaBinary(String userSpecifiedPath) {
    this(new File(userSpecifiedPath));
  }

  public OperaBinary(File userSpecifiedBinary) {
    binary = checkNotNull(userSpecifiedBinary);
    checkExecutable(binary);
  }

  public File getFile() {
    return binary;
  }

  public static File find() {
    return find(OperaProduct.DESKTOP);
  }

  /**
   * Locate the binary of the given product on the local system.  If no binary is found, null is
   * returned.
   *
   * Some products, such as {@link OperaProduct#DESKTOP} offers multiple configurations of the same
   * product (Opera and Opera Next), in which case the first is preferred.
   *
   * @param product the product to find the binary of
   * @return the binary of the product, or null
   */
  public static File find(OperaProduct product) {
    File binary = findBinaryBasedOnEnvironmentVariable();
    if (binary != null) {
      return binary;
    }
    return findBinaryBasedOnPlatform(product);
  }

  private static File findBinaryBasedOnEnvironmentVariable() {
    String binaryPath = System.getenv(OPERA_PATH_ENV_VAR);
    if (binaryPath == null) {
      return null;
    }

    File binary = new File(binaryPath);
    if (binary.exists() && binary.isFile()) {
      return binary;
    }

    ImmutableList.Builder<String> paths = ImmutableList.builder();

    switch (platform) {
      case WINDOWS:
      case VISTA:
      case XP:
        if (!binaryPath.endsWith(".exe")) {
          paths.add(binaryPath + ".exe");
        } else {
          paths.add(binaryPath);
        }
        break;

      case MAC:
        if (!binaryPath.endsWith(".app")) {
          binaryPath += ".app";
        }

        for (String internalAppDirectory : of("/Contents/MacOS",
                                              "/Contents/Resources/Opera Mobile.app/Contents/MacOS")) {
          for (String b : buildMobileBinaries()) {
            paths.add(binaryPath + internalAppDirectory + b);
          }
        }

        break;
    }

    System.out.println(paths.build());

    for (String path : paths.build()) {
      File possibleBinary = new File(path);
      if (possibleBinary.exists()) {
        return possibleBinary;
      }
    }

    return null;
  }

  private static File findBinaryBasedOnPlatform(OperaProduct product) {
    final List<String> paths;
    final List<String> binaries;

    switch (product) {
      case ALL:
      case DESKTOP:
        paths = buildDesktopPaths();
        binaries = buildDesktopBinaries();
        break;

      case MOBILE:
        paths = buildMobilePaths();
        binaries = buildMobileBinaries();
        break;

      default:
        return null;
    }

    for (String path : paths) {
      File file = new File(path);
      if (file.exists()) {
        return file;
      }
    }

    for (String binary : binaries) {
      String systemOpera = CommandLine.find(binary);
      if (systemOpera != null) {
        return new File(systemOpera);
      }
    }

    return null;
  }

  private static List<String> buildDesktopPaths() {
    ImmutableList.Builder<String> paths = ImmutableList.builder();

    switch (platform) {
      case LINUX:
      case UNIX:
        paths.add("/usr/bin/opera");
        paths.add("/usr/bin/opera-next");
        break;

      case MAC:
        paths.add("/Applications/Opera.app/Contents/MacOS/Opera");
        paths.add("/Applications/Opera Next.app/Contents/MacOS/Opera");
        break;

      case WINDOWS:
      case VISTA:
      case XP:
        paths.addAll(WindowsUtils.getPathsInProgramFiles("Opera\\opera.exe"));
        paths.addAll(WindowsUtils.getPathsInProgramFiles("Opera Next\\opera.exe"));
        break;
    }

    return paths.build();
  }

  private static List<String> buildDesktopBinaries() {
    return platform.is(WINDOWS) ? of("opera.exe") : of("opera", "opera-next");
  }

  private static List<String> buildMobilePaths() {
    ImmutableList.Builder<String> paths = ImmutableList.builder();

    if (platform.is(Platform.WINDOWS)) {
      paths
          .addAll(WindowsUtils.getPathsInProgramFiles("Opera Mobile Emulator\\OperaMobileEmu.exe"));
      return paths.build();
    }

    String path = "";
    switch (platform) {
      case LINUX:
      case UNIX:
        path = "/usr/bin";
        break;

      case MAC:
        path =
            "/Applications/Opera Mobile Emulator.app/Contents/Resources/Opera Mobile.app/Contents/MacOS";
        break;
    }

    for (String binary : buildMobileBinaries()) {
      paths.add(path + binary);
    }

    return paths.build();
  }

  private static List<String> buildMobileBinaries() {
    switch (platform) {
      case WINDOWS:
        return of("OperaMobileEmu.exe");

      case MAC:
        return of("Opera Mobile", "operamobile");

      case LINUX:
      case UNIX:
        return of("operamobile");

      default:
        return of();
    }
  }


  private static void checkExecutable(File executable) {
    checkState(executable.exists(),
               "The executable does not exist: %s", executable.getPath());
    checkState(!executable.isDirectory(),
               "The executable is a directory: %s", executable.getPath());
    checkState(executable.canExecute(),
               "The file is not executable: %s", executable.getPath());
  }

}