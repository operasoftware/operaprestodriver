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

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.environment.GlobalTestEnvironment;
import com.opera.core.systems.environment.InProcessTestEnvironment;
import com.opera.core.systems.environment.TestEnvironment;
import com.opera.core.systems.environment.webserver.AppServer;
import com.opera.core.systems.testing.drivers.OperaDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * OperaDriverTestCase provides a test framework with various convenience methods for OperaDriver.
 *
 * In particular it spawns an out-of-process web server holding fixture files, a storage of
 * pages/fixtures that can be used in tests, various ad-hoc resources (such as fake files,
 * executables, &c.), a storage for preserving OperaDriver instances cross test classes, and a set
 * of annotations that provide ignore rules and special actions.
 *
 * To use these features, make sure that the JUnit test class extends this class as such:
 *
 * <pre><code>
 *   public class MyTest extends OperaDriverTestCase {
 *     …
 *   }
 * </code></pre>
 *
 * This test setup mimics the characteristics of the Selenium test framework making it easier to
 * exchange tests.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@RunWith(OperaDriverTestRunner.class)
public abstract class OperaDriverTestCase {

  protected static TestEnvironment environment;
  protected static AppServer server;
  protected static Pages pages;
  protected static Resources resources;
  protected static TestOperaDriver driver;

  private static OperaSettings settings = new OperaSettings();
  private static boolean spawnDriver = true;
  private static ThreadLocal<TestOperaDriver> storedDriver = new ThreadLocal<TestOperaDriver>();

  @Before
  public void prepareEnvironment() throws Exception {
    environment = GlobalTestEnvironment.get();
    server = environment.getWebServer();
    pages = new Pages(server);
    resources = new Resources();

    String hostName = environment.getWebServer().getHostName();
    String alternateHostName = environment.getWebServer().getAlternateHostName();

    assertThat(hostName, is(not(equalTo(alternateHostName))));
  }

  @Before
  public void createDriver() {
    driver = createDriverIfNecessary();
    storedDriver.set(driver);
  }

  @AfterClass
  public static void resetSettings() {
    settings = new OperaSettings();
  }

  public static TestOperaDriver createFreshDriver() {
    return (TestOperaDriver) new OperaDriverBuilder(
        new TestOperaDriverSupplier()).using(getSettings()).get();
  }

  public static TestOperaDriver getWrappedDriver() {
    createDriverIfNecessary();
    return storedDriver.get();
  }

  public static TestOperaDriver createDriverIfNecessary() {
    TestOperaDriver driver = storedDriver.get();

    if (driver == null && spawnDriver || (driver != null && !driver.isRunning())) {
      driver = createFreshDriver();
      storedDriver.set(driver);
    }

    return storedDriver.get();
  }

  @Before
  public void createEnvironment() {
    environment = GlobalTestEnvironment.get(InProcessTestEnvironment.class);
  }

  public static OperaSettings getSettings() {
    return settings;
  }

  public static void setSettings(OperaSettings newSettings) {
    settings = newSettings;
  }

  /**
   * Set this to true to disable spawning of new drivers.
   *
   * @param enabled true will create new drivers when needed, false will disable creation of new
   *                drivers
   */
  public static void setCreateDriver(boolean enabled) {
    spawnDriver = enabled;
  }

  public static void removeDriver() {
    TestOperaDriver current = storedDriver.get();

    if (current == null) {
      return;
    }

    try {
      current.quit();
    } catch (RuntimeException ignored) {
      // fall through
    } finally {
      storedDriver.remove();
    }
  }

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

  /*
  public static OperaProduct currentProduct() {
    createDriverIfNecessary();
    return getWrappedDriver().utils().getProduct();
  }

  public static Platform currentPlatform() {
    TestOperaDriver current = getWrappedDriver();

    if (current != null) {
      currentPlatform = current.utils().getPlatform();
    }

    return currentPlatform;
  }
  */

}