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

package com.opera.core.systems.testing;

import com.opera.core.systems.testing.drivers.TestOperaDesktopDriver;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriverSupplier;

/**
 * Test framework for {@link com.opera.core.systems.OperaDesktopDriver}.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see OperaDriverTestCase
 */
public abstract class OperaDesktopDriverTestCase extends AbstractTestCase {

  protected TestOperaDesktopDriver driver;

  public OperaDesktopDriverTestCase() {
    super(new TestOperaDesktopDriverSupplier());
  }

  @Override
  public void createDriverIfNecessary() {
    super.createDriverIfNecessary();
    driver = (TestOperaDesktopDriver) super.driver;
  }

  /*
  public static OperaProduct currentProduct() {
    return getWrappedDriver().utils().getProduct();
  }

  public static Platform currentPlatform() {
    return getWrappedDriver().utils().getPlatform();
  }

  public static boolean currentHasIdle() {
    return currentServices().isOperaIdleAvailable();
  }

  public static ScopeServices currentServices() {
    return getWrappedDriver().getServices();
  }
  */

}