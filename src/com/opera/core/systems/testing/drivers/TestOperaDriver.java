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

package com.opera.core.systems.testing.drivers;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IOperaExec;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Provides access to the {@link com.opera.core.systems.runner.OperaRunner}, so we can detect
 * crashes.
 */
public class TestOperaDriver extends OperaDriver {
  
  public static enum ClosingStrategy { SWITCH_TO, ACTION }

  private boolean isRunning = false;
  private String controlWindow;

  /**
   * Creates a new TestOperaDriver with the given capabilities.
   *
   * @param capabilities the set of capabilities to use
   */
  public TestOperaDriver(Capabilities capabilities) {
    super(capabilities);
    isRunning = true;
    controlWindow = getWindowHandle();
  }

  /**
   * Provides access to {@link OperaRunner} which is used for monitoring the Opera process.
   *
   * @return instance of runner control interface for this driver instance
   */
  public OperaRunner getRunner() {
    return runner;
  }

  /**
   * Return the currently used set of capabilities.
   *
   * @return currently used capabilities
   */
  public Capabilities getCapabilities() {
    return capabilities;
  }

  public void close() {
    int windowCountBeforeClosing = getWindowCount();
    super.close();
    if ((windowCountBeforeClosing - 1) == 0) {
      isRunning = false;
    }
  }
  
  public void quit() {
    super.quit();
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public boolean isOperaIdleAvailable() {
    return super.isOperaIdleAvailable();
  }

  public IOperaExec getExecService() {
    return super.getExecService();
  }

  /**
   * Get the window handle of the initial control window, that is, the first window that was opened.
   *
   * @return window handle of control window
   */
  public String getControlWindow() {
    return controlWindow;
  }

  /**
   * Allows you to specifying the strategy for closing all windows (apart from the control window).
   * Windows can either be closed using a {@link OperaDriver#switchTo()} and {@link
   * OperaDriver#close()} call, or by sending the action "Close all pages".  The latter method
   * <strong>does not</strong> guarantee that all windows are in fact closed.
   *
   * @param strategy the strategy to use for closing windows; either {@link
   *                 ClosingStrategy#SWITCH_TO} or {@link ClosingStrategy#ACTION}
   */
  public void closeAll(ClosingStrategy strategy) {
    int windowCountBeforeClosing = getWindowHandles().size();

    switch (strategy) {
      case SWITCH_TO:
        closeAllUsingSwitchTo();
        break;
      case ACTION:
        closeAllUsingAction();
        break;
    }

    // Were all windows closed?
    if (getWindowHandles().size() != 1) {
      logger.warning("Should have had " + (windowCountBeforeClosing - 1) + " fewer window(s)");
    }

    // Make sure we're in the control window before continuing
    if (!controlWindow.equals(getWindowHandle())) {
      logger.warning("After closing windows, we were not in the default control window");
      switchTo().window(controlWindow);
    }
  }

  public TestOperaOptions manage() {
    return new TestOperaOptions();
  }

  public class TestOperaOptions extends OperaOptions {

    public TestOperaTimeouts timeouts() {
      return new TestOperaTimeouts();
    }

  }

  public class TestOperaTimeouts extends OperaTimeouts {

    /**
     * Sets the response timeout, that how long the driver should wait for Opera to send a reply
     * confirmation to a Scope command.
     *
     * @param time the amount of time to wait
     * @param unit the unit of measure for {@code time}
     * @return a self reference
     */
    public TestOperaTimeouts responseTimeout(long time, TimeUnit unit) {
      OperaIntervals.RESPONSE_TIMEOUT.setValue(TimeUnit.MILLISECONDS.convert(time, unit));
      return this;
    }

  }

  /**
   * Get the default set of capabilities used in OperaDriver and modifies them.
   *
   * @return capability defaults for {@link TestOperaDriver}
   */
  public static Capabilities getDefaultCapabilities() {
    DesiredCapabilities c = (DesiredCapabilities) OperaDriver.getDefaultCapabilities();
    c.setCapability(OperaDriver.LOGGING_LEVEL, Level.FINE);
    return c;
  }

  /**
   * Iterates through windows and closes all of them apart from the controlling window.  If the
   * default control window (first window) is closed, the browser will be quit.
   */
  private void closeAllUsingSwitchTo() {
    if (getWindowHandles().size() > 1) {
      // Close all windows apart from our first control window
      for (String windowHandle : getWindowHandles()) {
        if (!windowHandle.equals(controlWindow)) {
          switchTo().window(windowHandle);
          close();
        }
      }
    }
  }

  /**
   * Uses an Opera action to close all open windows.  There is no guarantee that this call will
   * succeed.
   */
  private void closeAllUsingAction() {
    if (utils().getProduct().is(OperaProduct.DESKTOP)) {
      throw new UnsupportedOperationException("Closing pages by Opera action not supported on " +
                                              "product DESKTOP");
    }

    services.getExec().action("Close all pages");
  }

}