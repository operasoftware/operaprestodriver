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

package com.opera.core.systems.runner.inprocess;

import com.google.common.collect.Iterables;

import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.runner.AbstractOperaRunner;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.interfaces.OperaRunner;
import com.opera.core.systems.scope.internal.ImplicitWait;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.os.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OperaInProcessRunner provides a native Java in-process way of launching and managing Opera
 * instances.  It relies on Selenium's {@link CommandLine}.
 */
public class OperaInProcessRunner extends AbstractOperaRunner implements OperaRunner {

  private final ReentrantLock lock = new ReentrantLock();
  private CommandLine process = null;

  public OperaInProcessRunner() {
    this(new OperaSettings());
  }

  public OperaInProcessRunner(OperaSettings settings) {
    super(settings);
  }

  public void startOpera() throws OperaRunnerException {
    lock.lock();

    try {
      if (isOperaRunning()) {
        return;
      }

      process = new CommandLine(
          settings.getBinary().getCanonicalPath(),
          Iterables.toArray(settings.arguments().getArgumentsAsStringList(), String.class)
      );

      // TODO(andreastt): Do we need to forward the environment to CommandLine?
      //process.setEnvironmentVariables(environment);
      process.copyOutputTo(System.out);
      process.executeAsync();

      boolean startedProperly = new ImplicitWait(OperaIntervals.PROCESS_START_TIMEOUT.getValue())
          .until(new Callable<Boolean>() {
            public Boolean call() {
              try {
                process.getExitCode();

                // Process exited within process execute timeout, meaning it exited immediately
                return false;
              } catch (IllegalStateException e) {
                // getExitCode() throws when process has not yet been executed... patience!
              }

              return true;
            }
          });

      if (!startedProperly) {
        throw new IOException("Opera exited immediately; possibly incorrect arguments?  Command: "
                              + process);
      }
    } catch (IOException e) {
      throw new OperaRunnerException("Could not start Opera: " + e.getMessage());
    } finally {
      lock.unlock();
    }
  }

  public void stopOpera() throws OperaRunnerException {
    lock.lock();

    try {
      if (!isOperaRunning()) {
        return;
      }

      process.destroy();
    } finally {
      lock.unlock();
      process = null;
    }
  }

  public boolean isOperaRunning() {
    return process != null;
  }

  public boolean hasOperaCrashed() {
    throw new UnsupportedOperationException();
  }

  public String getOperaCrashlog() {
    throw new UnsupportedOperationException();
  }

  public void shutdown() {
  }

  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    throw new UnsupportedOperationException();
  }

}