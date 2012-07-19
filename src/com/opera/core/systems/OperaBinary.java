package com.opera.core.systems;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.os.WindowsUtils;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
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

    if (!binary.exists() || !binary.isFile()) {
      throw new WebDriverException("Opera binary does not exist or is not a real file: " +
                                   binary.getPath());
    }

    if (!binary.canExecute()) {
      throw new WebDriverException("Opera binary is not an executable: " + binary.getPath());
    }
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
    String binaryName = System.getenv(OPERA_PATH_ENV_VAR);
    if (binaryName == null) {
      return null;
    }

    File binary = new File(binaryName);
    if (binary.exists()) {
      return binary;
    }

    switch (platform) {
      case WINDOWS:
      case VISTA:
      case XP:
        if (!binaryName.endsWith(".exe")) {
          binaryName += ".exe";
        }
        break;

      case MAC:
        if (!binaryName.endsWith(".app")) {
          binaryName += ".app";
        }
        binaryName += "/Contents/MacOS/opera";
        break;
    }

    binary = new File(binaryName);
    if (binary.exists()) {
      return binary;
    }

    return null;
  }

  private static File findBinaryBasedOnPlatform(OperaProduct product) {
    final List<String> paths;
    final List<String> binaries;

    switch (product) {
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

    switch (platform) {
      case LINUX:
      case UNIX:
        paths.add("/usr/bin/operamobile");
        break;

      case MAC:
        paths.add(
            "/Applications/Opera Mobile Emulator.app/Contents/Resources/Opera Mobile.app/Contents/MacOS/operamobile");
        break;

      case WINDOWS:
      case VISTA:
      case XP:
        paths.addAll(
            WindowsUtils.getPathsInProgramFiles("Opera Mobile Emulator\\OperaMobileEmu.exe"));
        break;
    }

    return paths.build();
  }

  private static List<String> buildMobileBinaries() {
    return platform.is(WINDOWS) ? of("OperaMobileEmu.exe") : of("operamobile");
  }

}