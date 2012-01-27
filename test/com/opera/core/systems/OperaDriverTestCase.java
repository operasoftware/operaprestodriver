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
import com.opera.core.systems.testing.drivers.OperaDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

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

  private static final String SEPARATOR = System.getProperty("file.separator");
  private static final File USER_HOME = new File(System.getProperty("user.dir"));

  private static File fixtureDirectory;

  @BeforeClass
  public static void setup() {
    setup(new OperaDriverBuilder(new TestOperaDriverSupplier()));
  }

  public static void setup(OperaDriverBuilder builder) {
    if (driver != null && driver.isRunning()) {
      return;
    }

    driver = (TestOperaDriver) builder.get();
    assertNotNull(driver);
  }

  @AfterClass
  public static void teardown() {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }

    driver = null;
  }

  // TODO(andreastt): All of the fixture-related methods below should be replaced by a page factory or something

  /**
   * Get the URL of the given fixture file.
   *
   * @param file the filename to get
   * @return the URL to the fixture file
   */
  protected static String fixture(String file) {
    return "file://localhost" + getFixtureDirectory().getPath() + SEPARATOR + file;
  }

  protected static File fixtureFile(String file) {
    return new File(getFixtureDirectory().getPath() + SEPARATOR +  file);
  }

  protected static File getFixtureDirectory() {
    if (fixtureDirectory == null) {
      fixtureDirectory = new File(USER_HOME.getPath() + SEPARATOR + "test" + SEPARATOR +
                                  "fixtures" + SEPARATOR);
    }

    return fixtureDirectory;
  }

  /**
   * Navigate to the given fixture file.
   *
   * @param file the filename from the fixture directory to navigate to
   */
  protected static void getFixture(String file) {
    driver.get(fixture(file));
  }

}