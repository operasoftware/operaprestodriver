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

import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDesktopDriverTestCase;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NoDriver
@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class OperaDesktopDriverTest extends OperaDesktopDriverTestCase {

  public final long defaultHandshakeTimeout = OperaIntervals.HANDSHAKE_TIMEOUT.getValue();

  public TestOperaDesktopDriver driver;
  public DesiredCapabilities capabilities = DesiredCapabilities.opera();
  public File profileDirectory;

  @Before
  public void setup() throws IOException {
    OperaIntervals.HANDSHAKE_TIMEOUT.setValue(2000);
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
    OperaIntervals.HANDSHAKE_TIMEOUT.setValue(defaultHandshakeTimeout);
  }

  @Test
  public void specificProfile() throws IOException {
    driver = new TestOperaDesktopDriver(capabilities);

    assertTrue(driver.isRunning());
    assertEquals(profileDirectory.getCanonicalPath(),
                 driver.getSettings().profile().getDirectory().getCanonicalPath());
  }

  @Test
  public void startAndQuitOperaFourTimes() throws IOException {
    for (int i = 0; i < 5; i++) {
      driver = new TestOperaDesktopDriver();
      assertTrue("Opera should be running", driver.isRunning());

      // TODO: What does this do?  Why is it here?
      driver.resetOperaPrefs("");
      assertTrue("Opera should be running", driver.isRunning());

      driver.quit();
    }
  }

  @Test(expected = WebDriverException.class)
  public void autostartDisabled() {
    OperaSettings settings = new OperaSettings();
    settings.autostart(false);
    new TestOperaDesktopDriver(settings);
  }

}