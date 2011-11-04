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

import com.opera.core.systems.runner.OperaRunnerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

import static com.opera.core.systems.OperaProduct.CORE;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class DesiredCapabilitiesTest extends DriverTestCase {

  OperaDriver driver;
  DesiredCapabilities capabilities;

  @Before
  public void setUp() {
    capabilities = (DesiredCapabilities) OperaDriver.getDefaultCapabilities();
  }

  @After
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void testSettingLoggingLevel() {
    capabilities.setCapability(OperaDriver.LOGGING_LEVEL, "FINER");
    driver = new OperaDriver(capabilities);

    assertEquals("FINER", capabilities.getCapability("opera.logging.level"));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLoggingLevelWithSmallLetters() {
    capabilities.setCapability(OperaDriver.LOGGING_LEVEL, "info");
    driver = new OperaDriver(capabilities);

    assertEquals("info", capabilities.getCapability("opera.logging.level"));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLogFile() throws IOException {
    //File log = tmpFolder.newFile("operadriver.log");
    File log = new File("/home/andreastt/operadriver.log");
    capabilities.setCapability(OperaDriver.LOGGING_FILE, log.getCanonicalPath());
    driver = new OperaDriver(capabilities);

    System.out.println(log.getAbsolutePath());

    assertTrue(log.length() > 0);
  }

  @Test(expected = WebDriverException.class)
  public void testSettingInvalidLogFile() throws Exception {
    capabilities.setCapability(OperaDriver.LOGGING_FILE, "/an/invalid/path");
    driver = new OperaDriver(capabilities);
  }

  @Test
  public void testSettingBinary() {
    capabilities.setCapability(OperaDriver.BINARY, OperaPaths.operaPath());
    driver = new OperaDriver(capabilities);
    driver.navigate().to("about:blank");
  }

  @Test(expected = OperaRunnerException.class)
  public void testSettingInvalidBinary() {
    capabilities.setCapability(OperaDriver.BINARY, "/invalid/path");
    driver = new OperaDriver(capabilities);
  }

  @Test
  public void testSettingHost() {
    capabilities.setCapability(OperaDriver.HOST, "localhost");
    driver = new OperaDriver(capabilities);
    assertNotNull(driver);
    driver.quit();
  }

  @Test
  @Ignore(products = CORE, reason = "core does not reset port number if -debugproxy is ommitted")
  public void testSettingPort() {
    capabilities.setCapability(OperaDriver.PORT, -1);
    driver = new OperaDriver(capabilities);
    assertNotNull(driver);
    driver.quit();
  }

  @Test
  public void testSettingProfile() throws IOException {
    capabilities.setCapability(OperaDriver.PROFILE, tmpFolder.newFolder().getCanonicalPath());
    driver = new OperaDriver(capabilities);
    assertNotNull(driver);
    driver.quit();
  }

  @Test
  public void testSettingIdle() {
    capabilities.setCapability(OperaDriver.OPERAIDLE, true);
    driver = new OperaDriver(capabilities);
    driver.navigate().to("about:blank");
    driver.quit();
  }

  @Test
  public void testSettingAutostartToTrue() {
    capabilities.setCapability(OperaDriver.AUTOSTART, true);
    driver = new OperaDriver(capabilities);
    assertTrue(driver.runner.isOperaRunning());
    driver.quit();
  }

}