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
import com.opera.core.systems.model.ScreenCaptureReply;
import com.opera.core.systems.runner.AbstractOperaRunner;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.internal.ImplicitWait;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.support.ui.Duration;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OperaInProcessRunner provides a native Java in-process way of launching and managing Opera
 * instances.  It relies on Selenium's {@link CommandLine}.
 */
public class OperaInProcessRunner extends AbstractOperaRunner implements OperaRunner {

  private final ReentrantLock lock = new ReentrantLock();
  private CommandLine process = null;
  private boolean shutdown = false;

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
      logger.config(String.format("runner arguments: %s", process));

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
    assertNotShutdown();
    throw new UnsupportedOperationException();
  }

  public String getOperaCrashlog() {
    assertNotShutdown();
    throw new UnsupportedOperationException();
  }

  public void shutdown() {
    shutdown = true;
  }

  public ScreenCaptureReply captureScreen() {
    return captureScreen(OperaIntervals.RUNNER_SCREEN_CAPTURE_TIMEOUT.getMs());
  }

  public ScreenCaptureReply captureScreen(long timeout) {
    return captureScreen(timeout, (String) null);
  }

  public ScreenCaptureReply captureScreen(long timeout, String... knownMD5s) {
    assertNotShutdown();

    ScreenCapture capture = new ImplicitWait(new Duration(timeout, TimeUnit.MILLISECONDS))
        .until(new Callable<ScreenCapture>() {
          public ScreenCapture call() throws Exception {
            return ScreenCapture.of();
          }
        });

    try {
      return new ScreenCaptureReply(capture.getMd5(), capture.getData());
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to do screen capture: " + e.getMessage());
    }
  }

  private void assertNotShutdown() {
    if (shutdown) {
      throw new OperaRunnerException("Opera was shutdown");
    }
  }

}