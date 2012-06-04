package com.opera.core.systems.common.io;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

import java.util.logging.Logger;

import static org.openqa.selenium.Platform.MAC;
import static org.openqa.selenium.Platform.UNIX;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * Should eventually be merged with {@link org.openqa.selenium.os.ProcessUtils} to make it platform-
 * independent.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public final class ProcessManager {

  private static final Platform currentPlatform = Platform.getCurrent();
  private static final Logger logger = Logger.getLogger(ProcessManager.class.getName());

  public static boolean isPidRunning(int processID) {
    String output;

    if (currentPlatform.is(WINDOWS)) {
      output = executeCommand("tasklist", "/FI", String.format("\"PID eq %d\"", processID));
    } else if (currentPlatform.is(UNIX) || currentPlatform.is(MAC)) {
      output = executeCommand("ps", "-p", String.valueOf(processID));
    } else {
      throw new WebDriverException("Unknown platform: " + currentPlatform);
    }

    return output != null && output.contains(String.format("%d ", processID));
  }

  public static void killPID(int processID) {
    if (currentPlatform.is(WINDOWS)) {
      executeCommand("taskkill", "/pid", String.valueOf(processID));
    } else if (currentPlatform.is(UNIX) || currentPlatform.is(MAC)) {
      executeCommand("kill", "-9", String.valueOf(processID));
    } else {
      throw new WebDriverException("Unknown platform: " + currentPlatform);
    }
  }

  private static String executeCommand(String commandName, String... args) {
    CommandLine cmd = new CommandLine(commandName, args);
    logger.fine(cmd.toString());

    cmd.execute();
    String output = cmd.getStdOut();

    if (!cmd.isSuccessful()) {
      throw new WebDriverException(
          String.format("exec return code %d: %s", cmd.getExitCode(), output));
    }

    return output;
  }

}