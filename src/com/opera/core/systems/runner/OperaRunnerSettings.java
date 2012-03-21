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

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaProfile;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.arguments.interfaces.OperaArguments;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.logging.Level;

/**
 * Defines a settings object which impacts OperaRunner, the interface for controlling the Opera
 * binary.
 *
public class OperaRunnerSettings
    implements com.opera.core.systems.runner.interfaces.OperaRunnerSettings {

  protected File operaBinary = null;
  protected Integer display = null;
  protected OperaProduct product = OperaProduct.DESKTOP;
  protected boolean noQuit = false;
  protected String host = "127.0.0.1";
  protected Integer port = (int) OperaIntervals.SERVER_RANDOM_PORT_IDENTIFIER.getValue();
  protected Level loggingLevel = Level.INFO;
  protected OperaArguments arguments = new OperaCoreArguments();

  private boolean supportsPd = true;

  public File getBinary() {
    return operaBinary;
  }

  /**
   * Sets Opera's binary.
   *
   * @param path the absolute path to the binary to use
   *
  public void setBinary(String path) {
    if (path != null && !path.isEmpty()) {
      setBinary(new File(path));
    } else {
      throw new OperaRunnerException("Invalid file path: " + path);
    }
  }

  public void setBinary(File binary) {
    if (!binary.exists() || !binary.isFile() || !binary.canExecute()) {
      throw new OperaRunnerException("No such file or not executable: " + binary);
    }

    operaBinary = binary;
  }

  public Integer getDisplay() {
    return display;
  }

  public void setDisplay(Integer display) throws UnsupportedOperationException {
    if (!Platform.getCurrent().is(Platform.LINUX)) {
      throw new UnsupportedOperationException(
          "Unsupported operating system: " + Platform.getCurrent());
    }
    this.display = display;
  }

  public OperaProduct getProduct() {
    return product;
  }

  public void setProduct(OperaProduct product) {
    this.product = product;
  }

  public boolean getNoQuit() {
    return noQuit;
  }

  public void setNoQuit(boolean noQuit) {
    this.noQuit = noQuit;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    // The port Opera should connect to.  0 = Random, -1 = Opera default (7001) (for use with Opera
    // < 11.60).
    if (port == OperaIntervals.SERVER_RANDOM_PORT_IDENTIFIER.getValue()) {
      port = PortProber.findFreePort();
    } else if (port == OperaIntervals.SERVER_DEFAULT_PORT_IDENTIFIER.getValue()) {
      port = (int) OperaIntervals.SERVER_PORT.getValue();
    }

    return port;
  }

  public void setPort(Integer port) {
    // The port Opera should connect to.  0 = Random, -1 = Opera default (7001) (for use with Opera
    // < 11.60).
    if (port == 0) {
      this.port = PortProber.findFreePort();
    } else if (port == -1) {
      this.port = (int) OperaIntervals.SERVER_PORT.getValue();
    } else {
      this.port = port;
    }

    this.port = port;
  }

  public boolean supportsDebugProxy() {
    return port != OperaIntervals.SERVER_PORT.getValue();
  }

  public boolean supportsPd() {
    return !getProduct().is(OperaProduct.CORE) && supportsPd;
  }

  public OperaArguments getArguments() {
    return arguments;
  }

  public void setArguments(OperaArguments arguments) {
    this.arguments = arguments;
  }

  public Level getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel(Level level) {
    loggingLevel = level;
  }

  public Capabilities toCapabilities() {
    DesiredCapabilities capabilities = DesiredCapabilities.opera();

    capabilities.setCapability(OperaDriver.LOGGING_LEVEL, getLoggingLevel());

    capabilities.setCapability(OperaDriver.BINARY,
                               (getBinary() == null) ? (String) null : getBinary().getPath());
    capabilities.setCapability(OperaDriver.ARGUMENTS, getArguments().toString());

    capabilities.setCapability(OperaDriver.HOST, getHost());
    capabilities.setCapability(OperaDriver.PORT, getPort());

    capabilities.setCapability(OperaDriver.DISPLAY, getDisplay());
    capabilities.setCapability(OperaDriver.PROFILE, getProfile());
    capabilities.setCapability(OperaDriver.PRODUCT, getProduct().toString());

    return capabilities;
  }

  public static OperaRunnerSettings getDefaultSettings() {
    OperaRunnerSettings settings = new OperaRunnerSettings();

    OperaArguments arguments;
    if (settings.getProduct().is(OperaProduct.DESKTOP)) {
      arguments = new OperaDesktopArguments();
    } else {
      arguments = new OperaCoreArguments();
    }

    settings.setArguments(arguments);
    return settings;
  }

}
*/