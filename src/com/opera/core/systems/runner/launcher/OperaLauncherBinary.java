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
package com.opera.core.systems.runner.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.ProcessUtils;

public class OperaLauncherBinary extends Thread {

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

  public void kill() {
    watcher.kill();
  }

  public void shutdown() {
    kill();
  }

  public void init() {
    ProcessBuilder builder = new ProcessBuilder(commands);
    try {

      process = builder.start();
      builder.redirectErrorStream(true);

      watcher = new OutputWatcher(process);

      outputWatcherThread = new Thread(getThreadGroup(), watcher, "output-watcher");
      running.set(true);
      outputWatcherThread.start();

    } catch (IOException e) {
      throw new WebDriverException("Could not start the process : "
          + e.getMessage());
    }
  }

  @Override
  public void run() {
    logger.fine("Waiting for Launcher binary to exit.");
    int exit = 0;
    while (running.get()) {
      try {
        exit = process.waitFor();
        logger.info("Launcher exited with return value " + exit);
        running.set(false);
      } catch (InterruptedException e) {
        logger.fine("Got interrupted. Will terminate Launcher.");
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
      logger.config("Running launcher: " + running.get());
      InputStream stream = process.getInputStream();
      String buffer = "";
      while (running.get()) {
        try {
          int r = stream.read();
          if(r == -1) return;
          else if(r == '\n') {
            logger.fine("LB: " + buffer);
            buffer = "";
          }
          else buffer += (char)r;
        } catch (IOException e) {
          /* ignored */
        }
      }
    }

    public void kill() {
      running.set(false);
      try {
        ProcessUtils.killProcess(process);
      } catch (Exception e) { // ProcessStillAliveException, RuntimeException
        // we cant do much here
        logger.warning("Could not kill the process : " + e.getMessage());
      }
    }
  }
}
