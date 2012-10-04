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

import com.opera.core.systems.OperaBinary;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.Platform;

import java.io.File;
import java.util.logging.Logger;

import static com.opera.core.systems.OperaProduct.DESKTOP;

/**
 * AbstractOperaRunner defines the command-line arguments to give to Opera shared between the
 * different {@link OperaRunner} implementations.
 */
public abstract class AbstractOperaRunner implements OperaRunner {

  protected final Logger logger = Logger.getLogger(getClass().getName());
  protected final OperaSettings settings;

  public AbstractOperaRunner() {
    this(new OperaSettings());
  }

  public AbstractOperaRunner(OperaSettings s) {
    settings = s;

    // Find a suitable Opera executable based on requested product if no binary has already been
    // specified
    if (settings.getBinary() == null) {
      // Do check for null here since OperaBinary's sanitization throws a cryptic null pointer
      File binary = OperaBinary.find(settings.getProduct());
      if (binary == null) {
        throw new OperaRunnerException(String.format(
            "Unable to find executable for product %s", settings.getProduct()));
      }

      // Calls new OperaBinary(b) which will check that the binary is executable and that it's not a
      // directory
      settings.setBinary(binary);
    }

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

    // Hack - if we are testing desktop mac the fullscreen argument must be after -pd
    // otherwise the pd setting does not have any effect. So putting it last.
    if (settings.getProduct().is(DESKTOP) && Platform.getCurrent().is(Platform.MAC)) {
      OperaArgument fullscreen = new OperaArgument("fullscreen");
      settings.arguments().remove(fullscreen);
      settings.arguments().add(fullscreen);
    }

    logger.config("Opera arguments: " + settings.arguments().getArgumentsAsStringList());
  }

  public OperaSettings getSettings() {
    return settings;
  }

  protected void sleep(OperaIntervals interval) {
    try {
      Thread.sleep(interval.getMs());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}