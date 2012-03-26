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
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.scope.services.IOperaExec;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;

import java.util.logging.Logger;

/**
 * Provides access to the {@link com.opera.core.systems.runner.OperaRunner}, so we can detect
 * crashes.
 */
public class TestOperaDriver extends OperaDriver {

  public static enum ClosingStrategy {SWITCH_TO, ACTION}

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final String controlWindow;
  private final Platform currentPlatform = Platform.getCurrent();

  private OperaProduct currentProduct;

  /**
   * Creates a new TestOperaDriver with the given capabilities.
   *
   * @param capabilities the set of capabilities to use
   */
  public TestOperaDriver(Capabilities capabilities) {
    super(capabilities);
    controlWindow = getWindowHandle();
  }

  /**
   * Creates a new TestOperaDriver with the given settings.
   *
   * @param settings the settings to use
   */
  public TestOperaDriver(OperaSettings settings) {
    super(settings);
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

  public OperaSettings getSettings() {
    return settings;
  }

  /**
   * Return the currently used set of capabilities.
   *
   * @return currently used capabilities
   */
  public Capabilities getCapabilities() {
    return settings.toCapabilities();
  }

  public boolean isRunning() {
    return runner != null && runner.isOperaRunning();
  }

  public ScopeServices getServices() {
    return services;
  }

  public IOperaExec getExecService() {
    return super.getExecService();
  }

  /**
   * Get the window handle of the initial control window, that is, the first window that was
   * opened.
   *
   * @return window handle of control window
   */
  public String getControlWindow() {
    return controlWindow;
  }

  /**
   * Closes all windows except the control window using the {@link ClosingStrategy#SWITCH_TO}
   * strategy.
   */
  public void closeAll() {
    closeAll(ClosingStrategy.SWITCH_TO);
  }

  /**
   * Allows you to specifying the strategy for closing all windows (apart from the control window).
   * Windows can either be closed using a {@link OperaDriver#switchTo()} and {@link
   * OperaDriver#close()} call, or by sending the action "Close all pages".  The latter method
   * <strong>does not</strong> guarantee that all windows are in fact closed.
   *
   * This method is failsafe, meaning that if there are no windows open it will not do anything.
   *
   * @param strategy the strategy to use for closing windows; either {@link
   *                 ClosingStrategy#SWITCH_TO} or {@link ClosingStrategy#ACTION}
   */
  public void closeAll(ClosingStrategy strategy) {
    int windowCountBeforeClosing = getWindowCount();

    // Either control window (1) or no windows (0)
    if (windowCountBeforeClosing <= 1) {
      return;
    }

    switch (strategy) {
      case SWITCH_TO:
        closeAllUsingSwitchTo();
        break;
      case ACTION:
        closeAllUsingAction();
        break;
    }

    // Were all windows closed?
    if (getWindowCount() != 1) {
      logger.warning("Should have had " + (windowCountBeforeClosing - 1) + " fewer window(s)");
    }

    // Make sure we're in the control window before continuing
    if (!getControlWindow().equals(getWindowHandle())) {
      logger.warning("After closing windows, we were not in the default control window");
      switchTo().window(getControlWindow());
    }
  }

  public TestOperaUtils utils() {
    return new TestOperaUtils();
  }

  public class TestOperaUtils extends OperaUtils {

    /**
     * Overrides the default {@link OperaDriver.OperaUtils#getProduct()} to also take the
     * environmental variable <code>OPERA_PRODUCT</code> into account, which can be used for
     *
     * @return browser's product type
     */
    public OperaProduct getProduct() {
      if (currentProduct == null) {
        currentProduct = OperaProduct.CORE;  // default
        String requestedProduct = System.getenv("OPERA_PRODUCT");

        if (isRunning() && (requestedProduct == null || requestedProduct.isEmpty())) {
          requestedProduct = super.getProduct().toString();
        } else {
          logger.warning("Driver is not running, defaulting to " + currentProduct);
        }

        try {
          currentProduct = OperaProduct.get(requestedProduct);
        } catch (IllegalArgumentException e) {
          logger.warning("Product '" + requestedProduct + "' not found, defaulting to " +
                         currentProduct);
        }
      }

      return currentProduct;
    }

    public Platform getPlatform() {
      return currentPlatform;
    }

  }

  public static Capabilities getDefaultCapabilities() {
    return new OperaSettings().toCapabilities();
  }

  /**
   * Iterates through windows and closes all of them apart from the controlling window.  If the
   * default control window (first window) is closed, the browser will be quit.
   */
  private void closeAllUsingSwitchTo() {
    // Close all windows apart from our first control window
    for (String windowHandle : getWindowHandles()) {
      if (!windowHandle.equals(getControlWindow())) {
        switchTo().window(windowHandle);
        close();
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