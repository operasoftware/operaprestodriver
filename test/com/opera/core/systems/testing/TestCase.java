package com.opera.core.systems.testing;

import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.testing.drivers.TestDriver;

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

  // TODO
  void setCreateDriver(boolean enabled);

  void setSettings(OperaSettings newSettings);

  OperaSettings getSettings();

  /**
   * Retrieves a the the stored driver if present.  If no driver is stored, this will return null.
   *
   * @return driver instance, or null if no driver is stored
   */
  TestDriver getWrappedDriver();

}