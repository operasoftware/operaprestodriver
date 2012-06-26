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

import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.Platform;
import org.openqa.selenium.os.ProcessUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openqa.selenium.Platform.WINDOWS;

public class OperaLauncherBinary extends Thread {

  private static final String launcherLoggingOutputExpression = "^\\[(\\w+)\\]";

  private final Process process;
  private final OutputWatcher watcher;
  private final Thread outputWatcherThread;
  private final List<String> commands = new LinkedList<String>();
  private final Logger logger = Logger.getLogger(getClass().getName());
  private final AtomicBoolean running = new AtomicBoolean(false);

  public OperaLauncherBinary(String location, String... args) throws IOException {
    super(new ThreadGroup("run-process"), "launcher");

    commands.add(location);
    if (args != null && args.length > 0) {
      commands.addAll(Arrays.asList(args));
    }

    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.redirectErrorStream(true);

    try {
      process = builder.start();
      watcher = new OutputWatcher(process);
      outputWatcherThread = new Thread(getThreadGroup(), watcher, "output-watcher");
      outputWatcherThread.setDaemon(true);
      running.set(true);
      outputWatcherThread.start();
    } catch (IOException e) {
      if (Platform.getCurrent().is(WINDOWS)) {
        throw new IOException(
            "Could not start the launcher process, make sure you have the Microsoft Visual C++ " +
            "2008 Redistributable Package installed on your system: " + e.getMessage());
      } else {
        throw new IOException(e);
      }
    }

    try {
      waitFor(OperaIntervals.PROCESS_START_SLEEP.getMs());
      if (process.exitValue() > 0) {
        throw new IOException("exited immediately; possibly incorrect command-line arguments?  " +
                              "Commands: " + commands);
      }
    } catch (IllegalThreadStateException e) {
      // process didn't exited, but soldier on!
    }
  }

  public String getCommands() {
    return commands.toString();
  }

  public void kill() {
    watcher.kill();
  }

  public void shutdown() {
    kill();
  }

  public boolean isRunning() {
    return running.get();
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
      Pattern pattern = Pattern.compile(launcherLoggingOutputExpression);
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
                  "launcher: " + buffer.replaceFirst(launcherLoggingOutputExpression, "").trim();
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

  private static void waitFor(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      // fall through
    }
  }

}