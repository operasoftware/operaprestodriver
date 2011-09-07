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

package com.opera.core.systems.settings;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.opera.core.systems.OperaDriver;

/**
 * Stores settings for OperaDriver.
 *
 * @author Deniz Turkoglu
 * @deprecated Use DesiredCapabilites instead.
 */
@Deprecated
public class OperaDriverSettings {

  private DesiredCapabilities capabilities;

  public OperaDriverSettings() {
    capabilities = (DesiredCapabilities) OperaDriver.getDefaultCapabilities();
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public boolean doRunOperaLauncherFromOperaDriver() {
    return true;
  }

  /**
   * Set whether OperaDriver should start Opera launcher. Defaults to
   * <code>true</code>. Set to <code>false</code> if you will start Opera
   * launcher manually.
   *
   * @param runOperaLauncherFromOperaDriver
   *         <code>true</code> to start Opera
   *         launcher, <code>false</code> to not.
   * @deprecated
   */
  public void setRunOperaLauncherFromOperaDriver(boolean runOperaLauncherFromOperaDriver) {
    throw new UnsupportedOperationException("Due to random port probing for launcher, " +
      "manually specifying whether to run launcher is not supported anymore.");
  }

  /**
   * @deprecated
   */
  public int getOperaLauncherListeningPort() {
    return 9999;
  }

  /**
   * Set the port for Opera launcher to listen on. Defaults to port 9999.
   *
   * @param operaLauncherPort port number to listen on
   * @deprecated
   */
  public void setOperaLauncherListeningPort(int operaLauncherPort) {
    throw new UnsupportedOperationException("Due to random port probing for launcher, " +
      "manually setting the launcher port is not supported anymore.");
  }

  public String getOperaLauncherBinary() {
    return (String) capabilities.getCapability(OperaDriver.LAUNCHER);
  }

  /**
   * Set the location of the Opera launcher binary.
   *
   * @param operaLauncherBinary the absolute path to the Opera launcher.
   */
  public void setOperaLauncherBinary(String operaLauncherBinary) {
    capabilities.setCapability(OperaDriver.LAUNCHER, operaLauncherBinary);
  }

  public String getOperaBinaryLocation() {
    return (String) capabilities.getCapability(OperaDriver.BINARY);
  }

  /**
   * Set the path to the of Opera.
   *
   * @param operaBinaryLocation the absolute path to Opera.
   */
  public void setOperaBinaryLocation(String operaBinaryLocation) {
    capabilities.setCapability(OperaDriver.BINARY, operaBinaryLocation);
  }

  public String getOperaBinaryArguments() {
    return (String) capabilities.getCapability(OperaDriver.ARGUMENTS);
  }

  /**
   * Set the arguments that will be passed to Opera.
   * <p/>
   * For example: <code>opera -nosession opera:debug</code> to start without
   * restoring a session (on *nix systems), and load the
   * <code>opera:debug</code> page in a tab.
   *
   * @param operaBinaryArguments the arguments to pass, separated by spaces.
   */
  public void setOperaBinaryArguments(String operaBinaryArguments) {
    if (operaBinaryArguments == null) {
      operaBinaryArguments = "";
    }

    capabilities.setCapability(OperaDriver.ARGUMENTS, operaBinaryArguments);
  }

  public Integer getOperaLauncherXvfbDisplay() {
    return (Integer) capabilities.getCapability(OperaDriver.DISPLAY);
  }

  /**
   * Set the *nix display to start Opera on. This will be concatenated with ":"
   * e.g. passing <code>1</code> will result in the command line option
   * <code>-display :1</code> being passed.
   *
   * @param operaLauncherXvfbDisplay the X screen number to start Opera on.
   */
  public void setOperaLauncherXvfbDisplay(Integer operaLauncherXvfbDisplay) {
    capabilities.setCapability(OperaDriver.DISPLAY, operaLauncherXvfbDisplay);
  }

  /**
   * If set then launcher will not close Opera when OperaDriver finishes, and it
   * quits.
   *
   * @param noQuit
   */
  public void setNoQuit(boolean noQuit) {
    capabilities.setCapability(OperaDriver.NO_QUIT, noQuit);
  }

  /**
   * @return
   */
  public boolean getNoQuit() {
    return (Boolean) capabilities.getCapability(OperaDriver.NO_QUIT);
  }

  /**
   * @param noRestart
   * @deprecated
   */
  @Deprecated
  public void setNoRestart(boolean noRestart) {
    capabilities.setCapability(OperaDriver.NO_RESTART, noRestart);
  }

  /**
   * @return
   * @deprecated
   */
  @Deprecated
  public boolean getNoRestart() {
    return (Boolean) capabilities.getCapability(OperaDriver.NO_RESTART);
  }

  public void setGuessOperaPath(boolean guess) {
    capabilities.setCapability(OperaDriver.GUESS_BINARY_PATH, guess);
  }

  public boolean guessOperaPath() {
    return (Boolean) capabilities.getCapability(OperaDriver.GUESS_BINARY_PATH);
  }

  public void setUseOperaIdle(boolean useOperaIdle) {
    capabilities.setCapability("opera.idle", useOperaIdle);
  }

  public boolean getUseOperaIdle() {
    return (Boolean) capabilities.getCapability("opera.idle");
  }

  /**
   * Whether OperaDriver should try and automatically launch Opera.
   *
   * @param autostart true to autostart, false to wait for connection
   */
  public void setAutostart(boolean autostart) {
    capabilities.setCapability(OperaDriver.AUTOSTART, autostart);
  }

  public boolean getAutostart() {
    return (Boolean) capabilities.getCapability(OperaDriver.AUTOSTART);
  }

  /**
   * Set which profile (desktop, smartphone, etc.) to use.
   *
   * @param profile The profile name.
   */
  public void setProfile(String profile) {
    capabilities.setCapability(OperaDriver.BINARY_PROFILE, profile);
  }

  /**
   * @return The profile name to use.
   */
  public String getProfile() {
    return (String) capabilities.getCapability(OperaDriver.BINARY_PROFILE);
  }

}