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

package com.opera.core.systems;

import com.opera.core.systems.common.io.ProcessManager;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NeedsLocalEnvironment;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDesktopDriverTestCase;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Duration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.opera.core.systems.OperaBinary.OPERA_PATH_ENV_VAR;
import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.OperaSettings.Capability.NO_QUIT;
import static com.opera.core.systems.OperaSettings.Capability.NO_RESTART;
import static com.opera.core.systems.OperaSettings.Capability.PRODUCT;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.openqa.selenium.Platform.LINUX;

@NoDriver
@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class OperaDesktopDriverTest extends OperaDesktopDriverTestCase {

  public static final Duration
      DEFAULT_HANDSHAKE_TIMEOUT = OperaIntervals.HANDSHAKE_TIMEOUT.getValue();
  public static final String OLD_OPERA_PATH = System.getenv(OPERA_PATH_ENV_VAR);

  public TestOperaDesktopDriver driver;
  public DesiredCapabilities capabilities = DesiredCapabilities.opera();
  public File profileDirectory;

  @Before
  public void setup() throws IOException {
    OperaIntervals.HANDSHAKE_TIMEOUT.setValue(new Duration(2, TimeUnit.SECONDS));
    profileDirectory = new TemporaryFolder().newFolder();
    capabilities.setCapability(PROFILE.getCapability(), profileDirectory.getPath());
  }

  @After
  public void tearDownDriver() {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }
  }

  @After
  public void reset() {
    OperaIntervals.HANDSHAKE_TIMEOUT.setValue(DEFAULT_HANDSHAKE_TIMEOUT);
    environment.set(OPERA_PATH_ENV_VAR, OLD_OPERA_PATH);
  }

  @Test
  public void specificProfile() throws IOException {
    driver = new TestOperaDesktopDriver(capabilities);

    assertTrue(driver.isRunning());
    assertEquals(profileDirectory.getCanonicalPath(),
                 driver.getSettings().profile().getDirectory().getCanonicalPath());
  }

  @Test
  public void startAndQuitOperaFourTimes() {
    for (int i = 0; i < 5; i++) {
      driver = new TestOperaDesktopDriver();
      assertTrue("Opera should be running", driver.isRunning());

      // TODO: What does this do?  Why is it here?
      driver.resetOperaPrefs("");
      assertTrue("Opera should be running", driver.isRunning());

      driver.quit();
    }
  }

  @Test
  public void autostartDisabled() {
    OperaSettings settings = new OperaSettings();
    settings.autostart(false);

    try {
      new TestOperaDesktopDriver(settings);
      fail("Expected exception");
    } catch (WebDriverException e) {
      assertThat(e.getCause(), instanceOf(ResponseNotReceivedException.class));
      assertThat(e.getMessage(), containsString("No response in a timely fashion"));
    }
  }

  @Test
  public void environmentalBinaryPathIsRespected() {
    environment.set(OperaBinary.OPERA_PATH_ENV_VAR, resources.executableBinary().getPath());

    try {
      new TestOperaDesktopDriver();
      fail("Expected exception");
    } catch (WebDriverException e) {
      assertThat(e.getCause(), instanceOf(OperaRunnerException.class));
      assertThat(e.getMessage(), containsString("Could not start Opera"));
    }
  }

  /**
   * This test is known to fail if OPERA_PATH points to a shell script or a symlink, as is the case
   * on Debian.
   */
  @Test
  @Ignore(platforms = LINUX)
  public void environmentalBinaryPathWorks() throws IOException {
    File binary = new OperaBinary(OperaProduct.DESKTOP).getFile();
    environment.set(OperaBinary.OPERA_PATH_ENV_VAR, binary.getPath());

    driver = new TestOperaDesktopDriver();
    assertEquals(binary.getCanonicalPath(), driver.getSettings().getBinary().getCanonicalPath());
    assertEquals(binary.getCanonicalPath(), new File(driver.getOperaPath()).getCanonicalPath());
    assertEquals(binary.getCanonicalPath(), driver.utils().getBinaryPath());
  }

  @Test
  public void capabilitiesForDesktopAreCorrect() {
    capabilities.setCapability(NO_QUIT.getCapability(), true);

    driver = new TestOperaDesktopDriver(capabilities);

    Capabilities actual = driver.getSettings().toCapabilities();
    assertEquals(actual.getCapability(AUTOSTART.getCapability()), true);
    assertEquals(actual.getCapability(PRODUCT.getCapability()), OperaProduct.DESKTOP);
    assertEquals(actual.getCapability(NO_QUIT.getCapability()), true);
    assertEquals(actual.getCapability(NO_RESTART.getCapability()), false);
  }

  @Test
  @NeedsLocalEnvironment
  public void browserDoesNotQuit() {
    OperaSettings settings = new OperaSettings();
    settings.setDetach(true);

    driver = new TestOperaDesktopDriver(settings);
    int processID = driver.utils().getPID();
    driver.quit();

    // Driver should be shut down, and there should be no connection to browser
    assertFalse(driver.getServices().isConnected());

    // But browser should be running
    assertTrue(ProcessManager.isPidRunning(processID));

    ProcessManager.killPID(processID);
  }

}