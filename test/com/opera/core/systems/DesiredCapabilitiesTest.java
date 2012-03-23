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

import com.opera.core.systems.runner.OperaRunnerException;
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
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.OperaSettings.Capability.BINARY;
import static com.opera.core.systems.OperaSettings.Capability.HOST;
import static com.opera.core.systems.OperaSettings.Capability.LAUNCHER;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_FILE;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_LEVEL;
import static com.opera.core.systems.OperaSettings.Capability.OPERAIDLE;
import static com.opera.core.systems.OperaSettings.Capability.PORT;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    capabilities.setCapability(LOGGING_LEVEL.getCapability(), "FINER");
    driver = new TestOperaDriver(capabilities);

    assertEquals("FINER", capabilities.getCapability(LOGGING_LEVEL.getCapability()));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLoggingLevelWithSmallLetters() {
    capabilities.setCapability(LOGGING_LEVEL.getCapability(), "info");
    driver = new TestOperaDriver(capabilities);

    assertEquals("info", capabilities.getCapability(LOGGING_LEVEL.getCapability()));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLoggingLevelWithObject() {
    capabilities.setCapability(LOGGING_LEVEL.getCapability(), Level.FINER);
    driver = new TestOperaDriver(capabilities);

    assertEquals(Level.FINER, capabilities.getCapability(LOGGING_LEVEL.getCapability()));
    assertNotNull(driver);
  }

  @Test
  public void testSettingLogFile() throws IOException {
    File log = tmpFolder.newFile("operadriver.log");
    capabilities.setCapability(LOGGING_FILE.getCapability(), log.getCanonicalPath());
    capabilities
        .setCapability(LOGGING_LEVEL.getCapability(), "FINER");  // up the level to get some ompf
    driver = new TestOperaDriver(capabilities);

    assertTrue(log.length() > 0);
  }

  @Test(expected = WebDriverException.class)
  public void testSettingInvalidLogFile() throws Exception {
    capabilities.setCapability(LOGGING_FILE.getCapability(), "/an/invalid/path");
    driver = new TestOperaDriver(capabilities);
  }

  @Test
  public void testSettingBinary() {
    capabilities.setCapability(BINARY.getCapability(), OperaPaths.operaPath());
    driver = new TestOperaDriver(capabilities);
    driver.navigate().to("about:blank");
  }

  @Test(expected = WebDriverException.class)
  public void testSettingInvalidBinary() {
    capabilities.setCapability(BINARY.getCapability(), "/invalid/path");
    driver = new TestOperaDriver(capabilities);
  }

  @Test
  public void testSettingHost() {
    String host = NETWORK_UTILS.getPrivateLocalAddress();  // 127.0.0.1
    capabilities.setCapability(HOST.getCapability(), host);
    driver = new TestOperaDriver(capabilities);
    assertNotNull(driver);
    assertEquals(host, driver.getCapabilities().getCapability(HOST.getCapability()));
    assertEquals(host, driver.getSettings().getHost());
    driver.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not reset port number if -debugproxy is omitted")
  public void testSettingPort() {
    capabilities.setCapability(PORT.getCapability(), -1);
    driver = new TestOperaDriver(capabilities);
    assertNotNull(driver);
    driver.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not support -pd")
  public void testSettingProfile() throws IOException {
    File profile = tmpFolder.newFolder();
    capabilities.setCapability(PROFILE.getCapability(), profile.getPath());
    driver = new TestOperaDriver(capabilities);

    assertNotNull(driver);
    assertEquals(profile, driver.preferences().get("User Prefs", "Opera Directory").getValue());
    assertEquals(profile, driver.settings.profile().getDirectory());

    driver.quit();
  }

  @Test
  public void testSettingIdle() {
    capabilities.setCapability(OPERAIDLE.getCapability(), true);
    driver = new TestOperaDriver(capabilities);
    driver.navigate().to("about:blank");
    driver.quit();
  }

  @Test
  public void testSettingIdleUsingInteger() {
    capabilities.setCapability(OPERAIDLE.getCapability(), 1);
    driver = new TestOperaDriver(capabilities);
    driver.navigate().to("about:blank");
    driver.quit();
  }

  @Test
  public void testSettingIdleUsingString() {
    capabilities.setCapability(OPERAIDLE.getCapability(), "true");
    driver = new TestOperaDriver(capabilities);
    driver.navigate().to("about:blank");
    driver.quit();
  }

  @Test
  public void testSettingAutostartToTrue() {
    capabilities.setCapability(AUTOSTART.getCapability(), true);
    driver = new TestOperaDriver(capabilities);
    assertTrue(driver.runner.isOperaRunning());
    driver.quit();
  }

  /*
  @Test
  public void testFakeLauncher() {
    OperaSettings settings = new OperaSettings();

    try {
      TemporaryFolder tmp = new TemporaryFolder();
      tmp.create();
      File newLauncher = tmp.newFile("newLauncher");
      Files.copy(settings.getLauncher(), newLauncher);
      OperaLauncherRunnerSettingsTest.TestOperaLauncherRunnerSettings
          .makeLauncherExecutable2(newLauncher);
      capabilities.setCapability(LAUNCHER.getCapability(), newLauncher.getPath());
      driver = new TestOperaDriver(capabilities);
      assertEquals(newLauncher.getPath(),
                   driver.getCapabilities().getCapability(LAUNCHER.getCapability()));
    } catch (IOException e) {
      fail("Problem copying launcher for testing: " + e.getMessage());
    } finally {
      tmpFolder.delete();
    }
  }
  */

  @Test(expected = OperaRunnerException.class)
  public void testInvalidLauncher() {
    capabilities.setCapability(LAUNCHER.getCapability(), "/path/to/invalid/launcher");
    driver = new TestOperaDriver(capabilities);
  }

}