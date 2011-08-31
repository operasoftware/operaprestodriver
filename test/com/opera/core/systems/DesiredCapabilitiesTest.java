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

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class DesiredCapabilitiesTest {

  WebDriver driver;
  DesiredCapabilities capabilities;

  @Before
  public void setUp() {
    capabilities = OperaDriver.getDefaultCapabilities();
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
    capabilities.setCapability("opera.logging.level", "FINEST");
    driver = new OperaDriver(capabilities);

    Assert.assertEquals("FINEST", capabilities.getCapability("opera.logging.level"));
    Assert.assertNotNull(driver);
  }

  @Test
  public void testSettingLoggingLevelWithSmallLetters() {
    capabilities.setCapability("opera.logging.level", "info");
    driver = new OperaDriver(capabilities);

    Assert.assertEquals("info", capabilities.getCapability("opera.logging.level"));
    Assert.assertNotNull(driver);
  }

  @Test
  @Ignore
  public void testSettingLogFile() throws IOException {
    File log = tmpFolder.newFile("operadriver.log");
    capabilities.setCapability("opera.logging.file", log.getCanonicalPath());
    driver = new OperaDriver(capabilities);

    Assert.assertTrue(log.length() > 0);
  }

  @Test(expected = WebDriverException.class)
  public void testSettingInvalidLogFile() throws Exception {
    capabilities.setCapability("opera.logging.file", "/an/invalid/path");
    driver = new OperaDriver(capabilities);
  }

}
