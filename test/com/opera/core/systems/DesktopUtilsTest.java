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

import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDesktopDriverTestCase;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@NoDriver
@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class DesktopUtilsTest extends OperaDesktopDriverTestCase {

  public static TestOperaDesktopDriver driver;
  public static DesiredCapabilities capabilities = DesiredCapabilities.opera();
  public static File profileDirectory;

  @BeforeClass
  public static void setupDriver() throws IOException {
    profileDirectory = new TemporaryFolder().newFolder();
    capabilities.setCapability(PROFILE.getCapability(), profileDirectory.getPath());
    driver = new TestOperaDesktopDriver(capabilities);
  }

  @AfterClass
  public static void teardownDriver() {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }
  }

  /**
   * The tests using the <code>text</code> property of anything, and also the string tests, depend
   * on the language setting of Opera.  Since there doesn't seem to be any API in the
   * OperaDesktopDriver to control the language or even check it, we use the prefs API to validate
   * that the language file in use is "en.lng".  Not the best option, this should be approached with
   * some new language control API perhaps.
   */
  @Before
  public void verifyLanguageFileIsEnLng() {
    assertThat(((File) driver.preferences().get("User ScopePrefs", "Language File").getValue())
                   .getAbsolutePath(),
               containsString("en.lng"));
  }

  @Test
  public void getStringWorksWithAmpersand() {
    assertEquals("Price &comparison", driver.getString("SI_IDM_SEARCH_ENGINE6", false));
  }

  @Test
  public void getStringWorksWithStrippedAmpersand() {
    assertEquals("Price comparison", driver.getString("SI_IDM_SEARCH_ENGINE6", true));
  }

  @Test
  public void getSubstitutedStringWithNoArguments() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS"};
    assertEquals("Downloaded %1 of %2 at %3", driver.getSubstitutedString(inputParam, true));
  }

  @Test
  public void getSubstitutedStringWithAllArguments() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "one", "two", "three"};
    assertEquals("Downloaded one of two at three", driver.getSubstitutedString(inputParam, true));
  }

  @Test
  public void getSubstitutedStringWithZeroLengthStringArguments() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "", "", ""};
    assertEquals("Downloaded _ANY_ of _ANY_ at _ANY_",
                 driver.getSubstitutedString(inputParam, true));
  }

  @Test
  public void getSubstitutedStringWithMixOfFilledAndEmptyArguments() {
    String[] inputParam = {"S_PLUGIN_AUTO_INSTALL_DOWNLOAD_PROGRESS", "one", "", "three"};
    assertEquals("Downloaded one of _ANY_ at three", driver.getSubstitutedString(inputParam, true));
  }

  @Test
  public void largePreferencesDirectory() throws IOException {
    assertEquals(profileDirectory.getCanonicalPath(),
                 new File(driver.getLargePreferencesPath()).getCanonicalPath());
  }

  @Test
  public void smallPreferencesDirectory() throws IOException {
    assertEquals(profileDirectory.getCanonicalPath(),
                 new File(driver.getSmallPreferencesPath()).getCanonicalPath());
  }

  @Test
  public void cachePreferencesDirectory() throws IOException {
    assertEquals(profileDirectory.getCanonicalPath(),
                 new File(driver.getCachePreferencesPath()).getCanonicalPath());
  }

}