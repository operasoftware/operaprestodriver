/*
Copyright 2012 Opera Software ASA

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
      executeCommand("kill", "-SIGKILL", String.valueOf(processID));
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