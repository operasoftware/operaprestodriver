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
import com.opera.core.systems.scope.services.IWindowManager;

import org.openqa.selenium.Capabilities;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;

/**
 * Provides access to the {@link com.opera.core.systems.runner.OperaRunner}, so we can detect
 * crashes.
 */
public class TestOperaDriver extends OperaDriver implements TestDriver {

  public static enum ClosingStrategy {SWITCH_TO, ACTION}

  private final IWindowManager windowManager;
  private OperaProduct currentProduct = null;

  /**
   * Creates a new TestOperaDriver with the default settings.
   */
  public TestOperaDriver() {
    this(new OperaSettings());
  }

  /**
   * Creates a new TestOperaDriver with the given capabilities.
   *
   * @param capabilities the set of capabilities to use
   */
  public TestOperaDriver(Capabilities capabilities) {
    this(new OperaSettings().merge(capabilities));
  }

  /**
   * Creates a new TestOperaDriver with the given settings.
   *
   * @param settings the settings to use
   */
  public TestOperaDriver(OperaSettings settings) {
    super(settings);
    windowManager = getScopeServices().getWindowManager();
  }

  public OperaRunner getRunner() {
    return runner;
  }

  public OperaSettings getSettings() {
    return settings;
  }

  @Override
  public Capabilities getCapabilities() {
    return settings.toCapabilities();
  }

  public ScopeServices getServices() {
    return getScopeServices();
  }

  @Override
  public IOperaExec getExecService() {
    return super.getExecService();
  }

  /**
   * Determines if Opera is running or not.  If Opera is not run locally using {@link OperaRunner}
   * it will check for a Scope connection instead.
   *
   * @return true if Opera is running, false otherwise
   */
  public boolean isRunning() {
    return runner != null ? runner.isOperaRunning() : getScopeServices().isConnected();
  }

  /**
   * Creates a new window.
   */
  public void createWindow() {
    windowManager.createWindow();
  }

  /**
   * Closes all windows consistently across all products and platforms.
   */
  @SuppressWarnings("unused")
  public void closeAll() {
    if (!isRunning()) {
      return;
    }

    windowManager.closeAllWindows();
    if (utils().getProduct().is(DESKTOP)) {
      windowManager.createWindow();
    }
    switchTo().defaultContent();
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
        currentProduct = CORE;  // default
        String requestedProduct = System.getenv("OPERA_PRODUCT");

        if (requestedProduct == null || requestedProduct.isEmpty()) {
          requestedProduct = super.getProduct().toString();
        } else {
          logger.warning("Driver is not running, defaulting to " + currentProduct);
        }

        try {
          currentProduct = OperaProduct.get(requestedProduct);
        } catch (IllegalArgumentException e) {
          logger.warning(String.format("Product `%s' not found, defaulting to %s",
                                       requestedProduct, currentProduct));
        }
      }

      return currentProduct;
    }

  }

}