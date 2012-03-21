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

package com.opera.core.systems.runner.interfaces;

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaProfile;
import com.opera.core.systems.arguments.interfaces.OperaArguments;

import org.openqa.selenium.Capabilities;

import java.io.File;
import java.util.logging.Level;

/**
 * Defines a settings object which impacts OperaRunner, the interface for controlling the Opera
 * binary.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public interface OperaRunnerSettings {

  /**
   * Returns the full path to the browser binary.
   *
   * @return the absolute path to Opera's binary
   */
  public File getBinary();

  /**
   * Sets Opera's location.
   *
   * @param binary the Opera binary
   */
  public void setBinary(File binary);

  /**
   * Gets the X display.  Only works on *nix operating systems.
   *
   * @return the X display
   */
  public Integer getDisplay();

  /**
   * Sets the X display to use.  Only works on *nix operating systems.
   *
   * @param display the X display to use
   * @throws UnsupportedOperationException if on a non-nix operating system
   */
  public void setDisplay(Integer display) throws UnsupportedOperationException;

  /**
   * Returns the product currently used, for example desktop or core.
   *
   * @return the profile configuration set
   */
  public OperaProduct getProduct();

  /**
   * Sets the product currently used, for example desktop or core.
   *
   * @param product the profile configuration to use
   */
  public void setProduct(OperaProduct product);

  /**
   * Returns the whether Opera should quit when OperaRunner is shut down.  If enabled, it will keep
   * the browser running after the runner is shut down.
   *
   * @return true if browser should keep running, false otherwise
   */
  public boolean getNoQuit();

  /**
   * Specifies whether Opera should keep running after OperaRunner is shut down.  If enabled, it
   * will keep the browser running after the runner is shut down.
   *
   * @param noQuit true if browser should keep running, false otherwise
   */
  public void setNoQuit(boolean noQuit);

  /**
   * The host Opera should connect to.
   *
   * 0 = Random -1 = Opera default (for use with Opera < 12)
   *
   * @return the host Opera should connect to
   */
  public String getHost();

  /**
   * The host Opera should connect to.
   *
   * 0 = Random -1 = Opera default (for use with Opera < 12)
   *
   * @param host the host Opera should connect to
   */
  public void setHost(String host);

  /**
   * Returns the port Opera should connect to.  0 = Random, -1 = Opera default (for use with Opera <
   * 12).
   *
   * @return the port Opera should connect to
   */
  public Integer getPort();

  /**
   * Specifies the port Opera should connect to.  0 = Random, -1 = Opera default (for use with Opera
   * < 12).
   *
   * @param port the port Opera should connect to
   */
  public void setPort(Integer port);

  /**
   * Whether or not the currently specified Opera configuration supports the
   * <code>-debugproxy</code> command-line argument.  If the specified port <em>is not equal</em> to
   * the default proxy server port specified in {@link com.opera.core.systems.scope.internal.OperaIntervals#SERVER_PORT},
   * this will be true.
   *
   * @return true if this configuration supports the <code>-debugproxy</code> command-line argument,
   *         false otherwise
   */
  public boolean supportsDebugProxy();

  /**
   * Whether or not the currently specified Opera configuration supports the <code>-pd</code>
   * command-line argument.
   *
   * @return true if this configuration supports the <code>-pd</code> command-line argument.
   */
  public boolean supportsPd();

  /**
   * Gets the arguments passed on to Opera.
   *
   * @return the arguments passed on to Opera
   */
  public OperaArguments getArguments();

  /**
   * Specifies what arguments to pass on to Opera.
   *
   * @param arguments which arguments to pass on to Opera
   */
  public void setArguments(OperaArguments arguments);

  /**
   * Returns the logging level of the started Opera process and the process handling mechanism.
   *
   * @return the logging level
   */
  public Level getLoggingLevel();

  /**
   * Specifies the logging level of the started Opera process and the process handling mechanism.
   *
   * @param level the logging level to use
   */
  public void setLoggingLevel(Level level);

  /**
   * Converts the OperaRunner settings to capabilities for Opera.  This does not copy the settings.
   *
   * @return capabilities for Opera with these settings
   */
  public Capabilities toCapabilities();

}