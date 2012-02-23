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

import com.opera.core.systems.environment.GlobalTestEnvironment;
import com.opera.core.systems.environment.InProcessTestEnvironment;
import com.opera.core.systems.environment.TestEnvironment;
import com.opera.core.systems.environment.webserver.WebServer;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.testing.drivers.OperaDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * You can extend OperaDriverTestCase in your test case to gain access to convenience methods
 * related to finding the current product used, auto-starting Opera before the test, quitting Opera
 * after the test, and gaining access to the fixtures directory.
 *
 * It also holds an extension of {@link com.opera.core.systems.OperaDriver}, called {@link
 * TestOperaDriver}, that exposes the {@link OperaRunner} and a method for determining whether the
 * constructor and {@link com.opera.core.systems.OperaDriver#quit()} methods has been called, {@link
 * TestOperaDriver#isRunning()}.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@RunWith(OperaDriverTestRunner.class)
public abstract class OperaDriverTestCase {

  private static final String SEPARATOR = System.getProperty("file.separator");
  private static final File USER_HOME = new File(System.getProperty("user.dir"));

  protected static TestEnvironment environment;
  protected static WebServer server;
  protected static Pages pages;
  protected static Resources resources;
  protected static TestOperaDriver driver;

  private static File fixtureDirectory;

  @ClassRule
  public static ExternalResource environmentResources = new ExternalResource() {

    @Override
    protected void before() throws Throwable {
      environment = GlobalTestEnvironment.get(InProcessTestEnvironment.class);
      server = environment.getWebServer();
      pages = new Pages(server);
      resources = new Resources();
    }

    @Override
    protected void after() {
    }

  };

  @ClassRule
  public static ExternalResource driverResource = new ExternalResource() {

    @Override
    protected void before() throws Throwable {
      if (driver != null) {
        return;
      }

      driver = (TestOperaDriver) new OperaDriverBuilder(new TestOperaDriverSupplier()).get();
    }

    @Override
    protected void after() {
      if (driver == null) {
        return;
      }

      try {
        driver.quit();
      } catch (RuntimeException ignored) {
        // fall through
      }

      driver = null;
    }
  };

  //@Rule
  //public FreshDriverRule freshDriverRule = new FreshDriverRule();

  // TODO(andreastt): All of the fixture-related methods below should be replaced by a page factory or something

  /**
   * Get the URL of the given fixture file.
   *
   * @param file the filename to get
   * @return the URL to the fixture file
   */
  @Deprecated
  protected static String fixture(String file) {
    return "file://localhost" + getFixtureDirectory().getPath() + SEPARATOR + file;
  }

  @Deprecated
  protected static File fixtureFile(String file) {
    return new File(getFixtureDirectory().getPath() + SEPARATOR + file);
  }

  @Deprecated
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
  @Deprecated
  protected static void getFixture(String file) {
    driver.get(fixture(file));
  }

}