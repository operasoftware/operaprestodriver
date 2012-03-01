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

import com.google.common.io.Files;

import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherRunnerSettings;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestOperaDriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.CORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DesiredCapabilitiesTest extends OperaDriverTestCase {

  public static final NetworkUtils NETWORK_UTILS = new NetworkUtils();

  public DesiredCapabilities capabilities;

  @Before
  public void beforeEach() {
    capabilities = (DesiredCapabilities) TestOperaDriver.getDefaultCapabilities();
  }

  @After
  public void afterEach() {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }
  }

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void testSettingLoggingLevel() {
    capabilities.setCapability(OperaDriver.LOGGING_LEVEL, "FINER");
    driver = new TestOperaDriver(capabilities);

    assertEquals("FINER", capabilities.getCapability(OperaDriver.LOGGING_LEVEL));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLoggingLevelWithSmallLetters() {
    capabilities.setCapability(OperaDriver.LOGGING_LEVEL, "info");
    driver = new TestOperaDriver(capabilities);

    assertEquals("info", capabilities.getCapability(OperaDriver.LOGGING_LEVEL));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLoggingLevelWithObject() {
    capabilities.setCapability(OperaDriver.LOGGING_LEVEL, Level.FINER);
    driver = new TestOperaDriver(capabilities);

    assertEquals(Level.FINER, capabilities.getCapability(OperaDriver.LOGGING_LEVEL));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLogFile() throws IOException {
    File log = tmpFolder.newFile("operadriver.log");
    capabilities.setCapability(OperaDriver.LOGGING_FILE, log.getCanonicalPath());
    capabilities
        .setCapability(OperaDriver.LOGGING_LEVEL, "FINER");  // up the level to get some ompf
    driver = new TestOperaDriver(capabilities);

    assertTrue(log.length() > 0);
  }

  @Test(expected = WebDriverException.class)
  public void testSettingInvalidLogFile() throws Exception {
    capabilities.setCapability(OperaDriver.LOGGING_FILE, "/an/invalid/path");
    driver = new TestOperaDriver(capabilities);
  }

  @Test
  public void testSettingBinary() {
    capabilities.setCapability(OperaDriver.BINARY, OperaPaths.operaPath());
    driver = new TestOperaDriver(capabilities);
    driver.navigate().to("about:blank");
  }

  @Test(expected = OperaRunnerException.class)
  public void testSettingInvalidBinary() {
    capabilities.setCapability(OperaDriver.BINARY, "/invalid/path");
    driver = new TestOperaDriver(capabilities);
  }

  @Test
  public void testSettingHost() {
    String host = NETWORK_UTILS.getPrivateLocalAddress();  // 127.0.0.1
    capabilities.setCapability(OperaDriver.HOST, host);
    driver = new TestOperaDriver(capabilities);
    assertNotNull(driver);
    assertEquals(host, driver.getCapabilities().getCapability(OperaDriver.HOST));
    assertEquals(host, driver.getSettings().getHost());
    driver.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not reset port number if -debugproxy is omitted")
  public void testSettingPort() {
    capabilities.setCapability(OperaDriver.PORT, -1);
    driver = new TestOperaDriver(capabilities);
    assertNotNull(driver);
    driver.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not support -pd")
  public void testSettingProfile() throws IOException {
    File profile = tmpFolder.newFolder();
    capabilities.setCapability(OperaDriver.PROFILE, profile.getPath());
    driver = new TestOperaDriver(capabilities);

    assertNotNull(driver);
    assertEquals(profile, driver.preferences().get("User Prefs", "Opera Directory").getValue());
    assertEquals(profile, driver.settings.getProfile().getDirectory());

    driver.quit();
  }

  @Test
  public void testSettingIdle() {
    capabilities.setCapability(OperaDriver.OPERAIDLE, true);
    driver = new TestOperaDriver(capabilities);
    driver.navigate().to("about:blank");
    driver.quit();
  }

  @Test
  public void testSettingAutostartToTrue() {
    capabilities.setCapability(OperaDriver.AUTOSTART, true);
    driver = new TestOperaDriver(capabilities);
    assertTrue(driver.runner.isOperaRunning());
    driver.quit();
  }

  @Test
  public void testFakeLauncher() {
    OperaLauncherRunnerSettings settings = new OperaLauncherRunnerSettings();

    try {
      TemporaryFolder tmp = new TemporaryFolder();
      tmp.create();
      File newLauncher = tmp.newFile("newLauncher");
      Files.copy(settings.getLauncher(), newLauncher);
      OperaLauncherRunnerSettingsTest.TestOperaLauncherRunnerSettings
          .makeLauncherExecutable2(newLauncher);
      capabilities.setCapability(OperaDriver.LAUNCHER, newLauncher.getPath());
      driver = new TestOperaDriver(capabilities);
      assertEquals(newLauncher.getPath(),
                   driver.getCapabilities().getCapability(OperaDriver.LAUNCHER));
    } catch (IOException e) {
      fail("Problem copying launcher for testing: " + e.getMessage());
    } finally {
      tmpFolder.delete();
    }
  }

  @Test(expected = OperaRunnerException.class)
  public void testInvalidLauncher() {
    capabilities.setCapability(OperaDriver.LAUNCHER, "/path/to/invalid/launcher");
    driver = new TestOperaDriver(capabilities);
  }

}