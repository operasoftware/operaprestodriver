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

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.arguments.interfaces.OperaArguments;
import com.opera.core.systems.runner.OperaLaunchers;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.OperaRunnerSettings;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.remote.DesiredCapabilities;

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

  public static final String LAUNCHER_ENV_VAR = "OPERA_LAUNCHER";

  protected File launcher;
  protected String backend = "software";

  private static final Logger logger = Logger.getLogger(OperaLauncherRunnerSettings.class.getName());

  public OperaLauncherRunnerSettings() {
    super();

    loggingLevel = OperaLauncherRunner.toLauncherLoggingLevel(getLoggingLevel());
    setLauncher(System.getenv(LAUNCHER_ENV_VAR));
  }

  public void setLoggingLevel(Level level) {
    loggingLevel = OperaLauncherRunner.toLauncherLoggingLevel(level);
  }

  public Capabilities toCapabilities() {
    DesiredCapabilities capabilities = (DesiredCapabilities) super.toCapabilities();

    capabilities.setCapability(OperaDriver.LAUNCHER, getLauncher().getPath());
    capabilities.setCapability(OperaDriver.BACKEND, getBackend());

    return capabilities;
  }

  public File getLauncher() {
    if (launcher == null) {
      setLauncher(provideLauncher());
    }

    return launcher;
  }

  /**
   * Specifies launcher to be used for controlling the Opera binary.  If <code>launcherPath</code>
   * is null, no action will be taken.
   *
   * @param launcherPath the absolute path to the launcher binary
   */
  public void setLauncher(String launcherPath) {
    if (launcherPath == null) {
      return;
    }

    setLauncher(new File(launcherPath));
  }

  /**
   * Specifies launcher to be used for controlling the Opera binary.
   *
   * @param newLauncher launcher to be used
   */
  public void setLauncher(File newLauncher) {
    try {
      assertLauncherGood(newLauncher);
    } catch (IOException e) {
      throw new OperaRunnerException("Invalid launcher: " + e.getMessage());
    }

    launcher = newLauncher;
  }

  /**
   * Gets the rendering backend currently used.
   *
   * @return "software" or "hardware" depending on backend used
   *
  @Deprecated
  @SuppressWarnings("unused")
  public String getBackend() {
    return backend;
  }

  /**
   * Sets which rendering backend to use.  Available backends are "software" and "hardware".
   *
   * @param backend the rendering backend to use
   */
  @Deprecated
  @SuppressWarnings("unused")
  public void setBackend(String backend) {
    this.backend = backend;
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

  public void makeLauncherExecutable() {
    makeLauncherExecutable(getLauncher());
  }


}