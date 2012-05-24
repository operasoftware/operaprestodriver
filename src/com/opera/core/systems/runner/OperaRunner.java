/*
Copyright 2011-2012 Opera Software ASA

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

package com.opera.core.systems.runner;

import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.model.ScreenShotReply;

import org.openqa.selenium.os.CommandLine;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * OperaRunner implements a pure-Java process manager for controlling the Opera binary.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaRunner implements com.opera.core.systems.runner.interfaces.OperaRunner {

  protected final Logger logger = Logger.getLogger(getClass().getName());
  protected final OperaSettings settings;

  /**
   * Controls access to {@link #process}.
   */
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * A reference to the current child process. Will be {@code null} whenever this service is not
   * running.  Protected by {@link #lock}.
   */
  private CommandLine process = null;

  public OperaRunner() {
    this(new OperaSettings());
  }

  public OperaRunner(OperaSettings s) {
    settings = s;

    // This can't be last, otherwise it might get interpreted as the page to open, and the file
    // listing page doesn't have a JS context to inject into.

    // Only append -pd if compatibility mode is enabled (for Opera < 11.60)
    if (settings.supportsPd()) {
      settings.arguments().add("-pd", settings.profile().getDirectory().getAbsolutePath());
    }

    // If port is set to -1 it means we're in compatibility mode as Opera < 11.60 does not support
    // the -debugproxy command-line argument.  It will instead use the default port 7001.
    if (settings.supportsDebugProxy()) {
      settings.arguments().add("debugproxy", settings.getHost() + ":" + settings.getPort());
    }

    settings.arguments().add("autotestmode");

    logger.config("Opera arguments: " + settings.arguments().getArgumentsAsStringList());
  }

  public void startOpera() throws OperaRunnerException {
    lock.lock();

    List<String> arguments = settings.arguments().getArgumentsAsStringList();

    try {
      if (process != null) {
        return;
      }

      process = new CommandLine(
          settings.getBinary().getPath(),
          arguments.toArray(new String[arguments.size()])
      );

      // TODO(andreastt): Do we need to forward the environment to CommandLine?
      //process.setEnvironmentVariables(environment);
      process.copyOutputTo(System.err);
      process.executeAsync();
    } finally {
      lock.unlock();
    }
  }

  public void stopOpera() throws OperaRunnerException {
    lock.lock();

    try {
      if (process == null) {
        return;
      }

      process.destroy();
    } finally {
      lock.unlock();
      process = null;
    }
  }

  public boolean isOperaRunning() {
    lock.lock();

    try {
      if (process == null) {
        return false;
      }

      process.destroy();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    } finally {
      lock.unlock();
    }
  }

  public boolean hasOperaCrashed() {
    throw new UnsupportedOperationException("not implemented");
  }

  public String getOperaCrashlog() {
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * Handles safe shutdown of the OperaRunner class.
   */
  public void shutdown() {
  }

  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    throw new UnsupportedOperationException("not implemented");
  }

}