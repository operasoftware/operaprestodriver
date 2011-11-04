/*
Copyright 2011 Opera Software ASA

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
import com.opera.core.systems.settings.OperaDriverSettings;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(OperaDriverTestRunner.class)
public abstract class OperaDriverTestCase {

  protected static TestOperaDriver driver;
  protected static OperaProduct currentProduct = OperaProduct.CORE;
  protected static Platform currentPlatform = Platform.getCurrent();

  private static String fixtureDirectory;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability(OperaDriver.LOGGING_LEVEL, "FINE");

    driver = new TestOperaDriver(caps);
    assertNotNull(driver);

    String requestedProduct = System.getenv("OPERA_PRODUCT");
    if (requestedProduct == null || requestedProduct.isEmpty()) {
      requestedProduct = driver.utils().getProduct();
    }

    if (requestedProduct != null && !requestedProduct.isEmpty()) {
      try {
        currentProduct = OperaProduct.valueOf(requestedProduct);
      } catch (IllegalArgumentException e) {
        // product not found
      }
    }

    initFixtures();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (driver != null && driver.getRunner().isOperaRunning()) {
      driver.quit();
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

  /**
   * Navigate to the given fixture file.
   *
   * @param file the filename from the fixture directory to navigate to
   */
  protected void getFixture(String file) {
    driver.get(fixture(file));
  }
}

/**
 * Provides access to the Opera Runner, so we can detect crashes.
 */
class TestOperaDriver extends OperaDriver {

  public TestOperaDriver() {
    super();
  }

  @Deprecated
  public TestOperaDriver(OperaDriverSettings settings) {
    this(settings.getCapabilities());
  }

  public TestOperaDriver(Capabilities capabilities) {
    super(capabilities);
  }

  public OperaRunner getRunner() {
    return runner;
  }

}