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

package com.opera.core.systems;

import com.opera.core.systems.runner.OperaRunner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * You can extend OperaDriverTestCase in your test case to gain access to convenience methods
 * related to finding the current product used, auto-starting Opera before the test, quitting Opera
 * after the test, and gaining access to the fixtures directory.
 *
 * It also holds an extension of {@link OperaDriver}, called {@link TestOperaDriver}, that exposes
 * the {@link OperaRunner} and a method for determining whether the constructor and {@link
 * OperaDriver#quit()} methods has been called, {@link TestOperaDriver#isRunning()}.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>7
 */
@RunWith(OperaDriverTestRunner.class)
public abstract class OperaDriverTestCase {

  protected static TestOperaDriver driver;
  protected static OperaProduct currentProduct = OperaProduct.CORE;
  protected static Platform currentPlatform = Platform.getCurrent();

  private static String fixtureDirectory;
  private static final Logger logger = Logger.getLogger(OperaDriverTestCase.class.getName());

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    if (driver != null && driver.isRunning()) {
      return;
    }

    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability(OperaDriver.LOGGING_LEVEL, "FINE");

    driver = new TestOperaDriver(caps);
    assertNotNull(driver);

    initProduct();
    initFixtures();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }
  }

  /**
   * Fetch the product from the connected driver or override it using the OPERA_PRODUCT
   * environmental variable.
   */
  protected static void initProduct() {
    if (!isDriverRunning()) {
      return;
    }

    String requestedProduct = System.getenv("OPERA_PRODUCT");
    if (requestedProduct == null || requestedProduct.isEmpty()) {
      requestedProduct = driver.utils().getProduct();
    }

    if (requestedProduct != null && !requestedProduct.isEmpty()) {
      try {
        currentProduct = OperaProduct.get(requestedProduct);
      } catch (IllegalArgumentException e) {
        logger.warning("Product not found, defaulting to " + currentProduct.toString());
      }
    }
  }

  /**
   * Setup the fixture directory.
   */
  protected static void initFixtures() {
    String separator = System.getProperty("file.separator");
    fixtureDirectory = System.getProperty("user.dir");
    fixtureDirectory = separator + fixtureDirectory + separator + separator + "test" +
                       separator + "fixtures" + separator;

    assertTrue(new File(fixtureDirectory).isDirectory());
  }

  /**
   * Get the URL of the given fixture file.
   *
   * @param file the filename to get
   * @return the URL to the fixture file
   */
  protected String fixture(String file) {
    return "file://localhost" + fixtureDirectory + file;
  }

  protected File fixtureFile(String file) {
    return new File(fixtureDirectory + file);
  }

  /**
   * Navigate to the given fixture file.
   *
   * @param file the filename from the fixture directory to navigate to
   */
  protected void getFixture(String file) {
    driver.get(fixture(file));
  }

  private static boolean isDriverRunning() {
    return (driver != null) && driver.isRunning();
  }

}

/**
 * Provides access to the {@link OperaRunner}, so we can detect crashes.
 */
class TestOperaDriver extends OperaDriver {

  private boolean isRunning = false;

  public TestOperaDriver() {
    this(OperaDriver.getDefaultCapabilities());
  }

  public TestOperaDriver(Capabilities capabilities) {
    super(capabilities);
    isRunning = true;
  }

  public OperaRunner getRunner() {
    return runner;
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public static Capabilities getDefaultCapabilities() {
    return OperaDriver.getDefaultCapabilities();
  }

  public void quit() {
    super.quit();
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

}