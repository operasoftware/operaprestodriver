package com.opera.core.systems.runner.launcher;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import com.opera.core.systems.OperaPaths;
import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.arguments.interfaces.OperaArguments;
import com.opera.core.systems.runner.OperaLaunchers;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.OperaRunnerSettings;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OperaLauncherRunnerSettings extends OperaRunnerSettings {

  public static final Logger logger = Logger.getLogger(OperaLauncherRunnerSettings.class.getName());
  protected File launcher;

  public OperaLauncherRunnerSettings() {
    super();
  }

  /**
   * The launcher allows for the following logging levels: "FINEST", "FINE", "INFO", "WARNING",
   * "SEVERE".  Since we want the launcher to use the same logging level as OperaDriver and the Java
   * logger used there have more logging levels, this function uses a heuristic to figure out a
   * sensible logging level based on the Java logger for the launcher.
   *
   * @param level the Java logging level
   */
  public void setLoggingLevel(Level level) {
    if (level == Level.FINEST ||
        level == Level.ALL) {
      loggingLevel = Level.FINEST;
    } else if (level == Level.FINER ||
               level == Level.FINE) {
      loggingLevel = Level.FINE;
    } else if (level == Level.WARNING) {
      loggingLevel = Level.WARNING;
    } else if (level == Level.SEVERE) {
      loggingLevel = Level.SEVERE;
    } else if (level == Level.INFO ||
               level == Level.CONFIG) {
      loggingLevel = Level.INFO;
    } else {
      loggingLevel = Level.OFF;
    }
  }

  public File getLauncher() {
    if (launcher == null) {
      launcher = new File(launcherPath());
    }

    return launcher;
  }

  public void setLauncher(File launcher) {
    if (launcher.exists() && launcher.isFile() && launcher.canExecute()) {
      this.launcher = launcher;
    } else {
      throw new OperaRunnerException("Invalid launcher: " + launcher);
    }
  }

  public static OperaLauncherRunnerSettings getDefaultSettings() {
    OperaLauncherRunnerSettings settings = new OperaLauncherRunnerSettings();

    OperaArguments arguments;
    if (settings.getProduct().is(OperaProduct.DESKTOP)) {
      arguments = new OperaDesktopArguments();
    } else {
      arguments = new OperaCoreArguments();
    }

    settings.setArguments(arguments);
    return settings;
  }

  /**
   * This method will try to locate the launcher on any system.  If the OPERA_LAUNCHER environment
   * variable is set but invalid, it will throw an exception.  If that is not the case, it will
   * attempt to extract the launcher from the resources of the launcher JAR that is bundled with
   * OperaDriver.
   *
   * @return the path to the launcher
   * @throws org.openqa.selenium.WebDriverException
   *          if launcher is not found
   */
  private static String launcherPath() {
    String path = System.getenv("OPERA_LAUNCHER");

    if (!OperaPaths.isPathValid(path)) {
      if (path != null && !path.isEmpty()) {
        throw new OperaRunnerException("Path from OPERA_LAUNCHER does not exist: " + path);
      }

      try {
        String userHome = System.getProperty("user.home");
        path = extractLauncher(new File(userHome + File.separator + ".launcher"));
      } catch (OperaRunnerException e) {
        throw new WebDriverException("Unable to extract bundled launcher: " + e.getMessage());
      }
    }

    return path;
  }

  /**
   * Extracts the launcher from the launcher JAR bundled with OperaDriver into the directory
   * specified.  If the launcher in that location is outdated, it will be updated/replaced.
   *
   * @param launcherPath directory where you wish to put the launcher
   * @return path to the launcher executable
   */
  private static String extractLauncher(File launcherPath) {
    String launcherName = getLauncherNameForOS();
    File
        targetLauncher =
        new File(launcherPath.getAbsolutePath() + File.separatorChar + launcherName);

    // Whether we need to copy a new launcher across, either because it doesn't currently exist, or
    // because its hash differs from our launcher.
    boolean copy;

    // Get the launcher resource from JAR.
    URL
        sourceLauncher =
        OperaLaunchers.class.getClassLoader().getResource("launchers/" + launcherName);

    // Does launcher exist among our resources?
    if (sourceLauncher == null) {
      throw new OperaRunnerException("Unknown file: " + sourceLauncher);
    }

    // Copy the launcher if it doesn't exist or if the current launcher on the system doesn't match
    // the one bundled with OperaDriver (launcher needs to be upgraded).
    if (targetLauncher.exists()) {
      try {
        copy = !Arrays.equals(md5(targetLauncher), md5(sourceLauncher.openStream()));
        if (copy) {
          logger.fine("Old launcher detected, upgrading");
        }
      } catch (NoSuchAlgorithmException e) {
        throw new OperaRunnerException("Algorithm is not available in your environment: " + e);
      } catch (IOException e) {
        throw new OperaRunnerException("Unable to open stream or file: " + e);
      }
    } else {
      logger.fine("No launcher found, copying");
      copy = true;
    }

    if (copy) {
      InputStream is = null;
      OutputStream os = null;

      try {
        if (!targetLauncher.exists()) {
          launcherPath.mkdirs();
          Files.touch(targetLauncher);
        }

        is = sourceLauncher.openStream();
        os = new FileOutputStream(targetLauncher);

        ByteStreams.copy(is, os);

        if (!targetLauncher.setLastModified(targetLauncher.lastModified())) {
          throw new OperaRunnerException(
              "Unable to set modification time for file: " + targetLauncher);
        }
      } catch (IOException e) {
        throw new WebDriverException("Cannot write file to disk: " + e.getMessage());
      } finally {
        if (is != null) {
          Closeables.closeQuietly(is);
        }
        if (os != null) {
          Closeables.closeQuietly(os);
        }
      }

      logger.fine("New launcher copied to " + targetLauncher.getAbsolutePath());
    }

    if (copy) {
      makeLauncherExecutable(targetLauncher);
    }

    return targetLauncher.getAbsolutePath();
  }

  public void makeLauncherExecutable() {
    makeLauncherExecutable(getLauncher());
  }

  /**
   * Makes the launcher executable by chmod'ing the file at given path (GNU/Linux and Mac only).
   *
   * @param launcher the file to make executable
   */
  private static void makeLauncherExecutable(File launcher) {
    Platform current = Platform.getCurrent();

    if (current.is(Platform.UNIX) || current.is(Platform.MAC)) {
      CommandLine line = new CommandLine("chmod", "u+x", launcher.getAbsolutePath());
      line.execute();
    }
  }

  /**
   * Get the launcher's binary file name based on what flavour of operating system and what kind of
   * architecture the user is using.
   *
   * @return the launcher's binary file name
   */
  protected static String getLauncherNameForOS() {
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
   * @throws NoSuchAlgorithmException if MD5 is not available
   * @throws IOException              if an I/O error occurs
   */
  private static byte[] md5(InputStream fis) throws NoSuchAlgorithmException, IOException {
    return ByteStreams.getDigest(ByteStreams.newInputStreamSupplier(ByteStreams.toByteArray(fis)),
                                 MessageDigest.getInstance("MD5"));
  }

  /**
   * Get the MD5 hash of the given file.
   *
   * @param file file to compute a hash on
   * @return a byte array of the MD5 hash
   * @throws IOException              if file cannot be found
   * @throws NoSuchAlgorithmException if MD5 is not available
   */
  private static byte[] md5(File file) throws NoSuchAlgorithmException, IOException {
    return Files.getDigest(file, MessageDigest.getInstance("MD5"));
  }

}