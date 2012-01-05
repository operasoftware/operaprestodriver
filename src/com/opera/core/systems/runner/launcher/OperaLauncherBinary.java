/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems.runner.launcher;

import com.opera.core.systems.runner.OperaRunnerException;

import org.openqa.selenium.Platform;
import org.openqa.selenium.os.ProcessUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperaLauncherBinary extends Thread {

  private static final String LAUNCHER_LOGGING_OUTPUT_EXPRESSION = "^\\[(\\w+)\\]";

  private Process process;
  private OutputWatcher watcher;
  private Thread outputWatcherThread;
  private List<String> commands = new ArrayList<String>();
  private static Logger logger = Logger.getLogger(OperaLauncherBinary.class.getName());
  private AtomicBoolean running = new AtomicBoolean(false);

  public OperaLauncherBinary(String location, String... args) {
    super(new ThreadGroup("run-process"), "launcher");

    commands.add(location);

    if (args != null && args.length > 0) {
      commands.addAll(Arrays.asList(args));
    }

    init();
  }

  public String getCommand() {
    return commands.toString();
  }

  public void kill() {
    watcher.kill();
  }

  public void shutdown() {
    kill();
  }

  public void init() {
    ProcessBuilder builder = new ProcessBuilder(commands);

    try {
      builder.redirectErrorStream(true);
      process = builder.start();
      watcher = new OutputWatcher(process);
      outputWatcherThread = new Thread(getThreadGroup(), watcher, "output-watcher");
      running.set(true);
      outputWatcherThread.start();
    } catch (IOException e) {
      if (Platform.getCurrent().is(Platform.WINDOWS)) {
        throw new OperaRunnerException(
            "Could not start the launcher process, make sure you have the Microsoft Visual C++ " +
            "2008 Redistributable Package installed on your system: " + e.getMessage());
      } else {
        throw new OperaRunnerException("Could not start the launcher process: " + e.getMessage());
      }
    }
  }

  @Override
  public void run() {
    logger.fine("Waiting for launcher binary to exit");

    int exit;

    while (running.get()) {
      try {
        exit = process.waitFor();
        logger.fine("Launcher exited with return value " + exit);
        running.set(false);
      } catch (InterruptedException e) {
        logger.warning("Got interrupted, will terminate launcher");
        process.destroy();
      }
    }
  }

  private class OutputWatcher implements Runnable {

    private Process process;

    public OutputWatcher(Process process) {
      this.process = process;
    }

    public void run() {
      logger.finer("Running launcher: " + running.get());

      InputStream stream = process.getInputStream();
      Level level;
      Pattern pattern = Pattern.compile(LAUNCHER_LOGGING_OUTPUT_EXPRESSION);
      String buffer = "";

      while (running.get()) {
        try {
          int r = stream.read();
          if (r == -1) {
            return;
          } else if (r == '\n') {
            // Log any messages from launcher's stdout
            level = Level.FINE;
            Matcher matcher = pattern.matcher(buffer);

            if (matcher.find()) {
              level = OperaLauncherRunner.toLauncherLoggingLevel(Level.parse(matcher.group(1)));
              buffer =
                  "launcher: " + buffer.replaceFirst(LAUNCHER_LOGGING_OUTPUT_EXPRESSION, "").trim();
            } else {
              buffer = "opera: " + buffer;
            }

            logger.log(level, buffer);

            // Empty buffer
            buffer = "";
          } else {
            buffer += (char) r;
          }
        } catch (IOException e) {
          // ignored
        }
      }
    }

    public void kill() {
      running.set(false);
      try {
        ProcessUtils.killProcess(process);
      } catch (Exception e) {  // ProcessStillAliveException, RuntimeException
        // We can't do much here...
        logger.warning("Could not kill the process: " + e.getMessage());
      }
    }

  }

}