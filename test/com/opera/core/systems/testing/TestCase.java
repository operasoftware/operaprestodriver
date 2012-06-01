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

package com.opera.core.systems.testing;

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.testing.drivers.TestDriver;

import org.openqa.selenium.Platform;

/**
 * Provides a test framework with various convenience methods for OperaDriver.
 *
 * In particular it spawns an out-of-process web server holding fixture files, a storage of
 * pages/fixtures that can be used in tests, various ad-hoc resources (such as fake files,
 * executables, &c.), a storage for preserving OperaDriver instances cross test classes, and a set
 * of annotations that provide ignore rules and special actions.
 *
 * To use these features, make sure that the JUnit test class extends this class as such:
 *
 * <pre><code>
 *   public class MyTest extends TestCaseImpl {
 *     …
 *   }
 * </code></pre>
 *
 * This test setup mimics the characteristics of the Selenium test framework making it easier to
 * exchange tests.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public interface TestCase {

  /**
   * Creates a new driver instance and stores it for future use.  If a driver instance already
   * exists nothing will happen.
   */
  void createDriverIfNecessary();

  /**
   * Creates a new instance of the driver using the provided {@link com.opera.core.systems.testing.drivers.TestDriverSupplier}
   * and {@link com.opera.core.systems.OperaSettings}.
   *
   * @return new driver instance
   */
  TestDriver createFreshDriver();

  /**
   * Quit and remove the current driver from storage.  If no driver exists, this method will do
   * nothing.
   */
  void removeDriver();

  /**
   * Temporarily disable the creation of new drivers.
   *
   * @param enabled true if drivers should be created, false otherwise
   */
  void setCreateDriver(boolean enabled);

  /**
   * Specify which settings for the next driver to use.  The settings specified here will
   * <em>not</em> be applicable to the currently running driver.
   */
  void setSettings(OperaSettings newSettings);

  /**
   * Gets the settings currently in use by the current driver.
   *
   * @return current settings
   */
  OperaSettings getSettings();

  /**
   * Retrieves a the the stored driver if present.  If no driver is stored, this will return null.
   *
   * @return driver instance, or null if no driver is stored
   */
  TestDriver getWrappedDriver();

  /**
   * Gets the Opera product currently tested.
   *
   * @return currently tested product
   */
  OperaProduct currentProduct();

  /**
   * Gets the currently tested platform.
   *
   * @return currently tested platform
   */
  Platform currentPlatform();

}