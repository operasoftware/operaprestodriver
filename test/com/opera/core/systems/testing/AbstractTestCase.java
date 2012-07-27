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
import com.opera.core.systems.environment.GlobalTestEnvironment;
import com.opera.core.systems.environment.InProcessTestEnvironment;
import com.opera.core.systems.environment.TestEnvironment;
import com.opera.core.systems.environment.webserver.AppServer;
import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;
import com.opera.core.systems.testing.drivers.TestDriverSupplier;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Shared functionality of different {@link TestCase} implementations.
 *
 * @see OperaDriverTestCase
 * @see OperaDesktopDriverTestCase
 */
@RunWith(TestRunner.class)
public abstract class AbstractTestCase implements TestCase {

  protected TestEnvironment environment;
  protected AppServer server;
  protected Pages pages;
  protected Resources resources;
  protected TestDriver driver;

  private static final ThreadLocal<TestDriver> storedDriver = new ThreadLocal<TestDriver>();
  private static OperaSettings settings = new OperaSettings();
  private static boolean spawnDriver = true;

  private final TestDriverSupplier supplier;
  private OperaProduct currentProduct;
  private Platform currentPlatform;

  public AbstractTestCase() {
    supplier = TestDriverBuilder.detect();
  }

  public AbstractTestCase(TestDriverSupplier supplier) {
    this.supplier = supplier;
  }

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
  public void createEnvironment() {
    environment = GlobalTestEnvironment.get(InProcessTestEnvironment.class);
  }

  @Before
  public void createDriver() {
    createDriverIfNecessary();
  }

  @AfterClass
  public static void resetSettings() {
    settings = new OperaSettings();
    spawnDriver = true;
  }

  public TestDriver createFreshDriver() {
    return new TestDriverBuilder(supplier).using(settings).get();
  }

  public void createDriverIfNecessary() {
    driver = getWrappedDriver();

    if (driver != null && !supplier.supplies(driver.getClass())) {
      removeDriver();
    }

    if (driver == null && spawnDriver || (driver != null && !driver.isRunning())) {
      driver = createFreshDriver();
      storedDriver.set(driver);

      currentProduct();
      currentPlatform();
    }
  }

  public void removeDriverIfNecessary() {
    TestDriver driver = getWrappedDriver();
    if (!spawnDriver && (driver != null && driver.isRunning())) {
      removeDriver();
    }
  }

  public void removeDriver() {
    TestDriver current = storedDriver.get();

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

  public void setCreateDriver(boolean enabled) {
    spawnDriver = enabled;
  }

  public void setSettings(OperaSettings newSettings) {
    settings = newSettings;
  }

  public OperaSettings getSettings() {
    return settings;
  }

  public TestDriver getWrappedDriver() {
    return storedDriver.get();
  }

  public OperaProduct currentProduct() {
    if (currentProduct == null) {
      currentProduct = getWrappedDriver().utils().getProduct();
    }

    return currentProduct;
  }

  public Platform currentPlatform() {
    if (currentPlatform == null) {
      currentPlatform = getWrappedDriver().utils().getPlatform();
    }

    return currentPlatform;
  }

}