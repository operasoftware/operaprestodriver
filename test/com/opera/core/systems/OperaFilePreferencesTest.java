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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.opera.core.systems.preferences.OperaFilePreferences;
import com.opera.core.systems.preferences.OperaGenericPreferences;
import com.opera.core.systems.preferences.OperaPreferences;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NoDriver
public class OperaFilePreferencesTest extends OperaDriverTestCase {

  public TestDriver driver;
  public OperaPreferences preferences;
  public int prefCountBefore = 0;
  public File iniFile;
  public OperaProfile profile;
  public File profileDirectory;

  private final Logger logger = Logger.getLogger(getClass().getName());

  @Rule
  public TemporaryFolder temporaryFolder;

  @Before
  public void beforeEach() throws IOException {
    temporaryFolder = new TemporaryFolder();
    temporaryFolder.create();

    OperaProduct assumedProduct = OperaProduct.ALL;
    if (driver != null && driver.isRunning()) {
      assumedProduct = driver.utils().getProduct();
    }

    switch (assumedProduct) {
      case DESKTOP:
        iniFile = temporaryFolder.newFile("operaprefs.ini");
        profileDirectory = temporaryFolder.getRoot();
        break;
      case CORE:
        profileDirectory = new File(driver.utils().getBinaryPath()).getParentFile();
        iniFile = new File(profileDirectory + File.separator + "opera.ini");
        break;
      case ALL:
      default:
        iniFile = temporaryFolder.newFile("opera.ini");
        profileDirectory = temporaryFolder.getRoot();
        break;
    }

    // Make a new copy in a temporary filesystem so we don't overwrite our fixture
    // TODO(andreastt): This should be done more elegantly in OperaDriverTestCase
    try {
      Files.copy(resources.locate("profile/opera.ini"), iniFile);
    } catch (IOException e) {
      fail("Unable to copy preference fixture: " + e.getMessage());
    }

    preferences = new OperaFilePreferences(iniFile);
    profile = new OperaProfile(profileDirectory);
    prefCountBefore = preferences.size();
  }

  @After
  public void afterEach() {
    if (driver != null && driver.isRunning()) {
       try {
        driver.quit();
      } catch (RuntimeException e) {
        logger.warning(Throwables.getStackTraceAsString(e));
      } finally {
        driver = null;
      }
    }
  }

  @Test
  public void testConstruction() {
    assertNotNull(preferences);
    assertEquals(46, preferences.size());
  }

  @Test(expected = WebDriverException.class)
  public void testConstructionWithInvalidFile() {
    new OperaFilePreferences(new File("/path/does/not/exist"));
  }

  @Test
  public void testMerge() {
    OperaPreferences newPrefs = new OperaGenericPreferences();
    newPrefs.set("Developer Tools", "Proxy Host", "1.2.3.4");
    newPrefs.set("Developer Tools", "Proxy Port", 1234);
    newPrefs.set("Developer Tools", "Proxy Auto Connect", true);

    preferences.merge(newPrefs);

    assertEquals("1.2.3.4", preferences.get("Developer Tools", "Proxy Host").getValue());
    assertEquals(1234, preferences.get("Developer Tools", "Proxy Port").getValue());
    assertEquals(true, preferences.get("Developer Tools", "Proxy Auto Connect").getValue());
    assertEquals(prefCountBefore, preferences.size());  // make sure it didn't add more prefs
  }

  @Test
  public void testSetWithString() {
    preferences.set("Developer Tools", "Proxy Host", "1.2.3.4");

    OperaPreferences verifier = new OperaFilePreferences(iniFile);
    assertEquals("Developer Tools", verifier.get("Developer Tools", "Proxy Host").getSection());
    assertEquals("Proxy Host", verifier.get("Developer Tools", "Proxy Host").getKey());
    assertEquals("1.2.3.4", verifier.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithBoolean() {
    preferences.set("Developer Tools", "Proxy Auto Connect", true);

    OperaPreferences verifier = new OperaFilePreferences(iniFile);
    assertEquals("Developer Tools", verifier.get("Developer Tools", "Proxy Auto Connect")
        .getSection());
    assertEquals("Proxy Auto Connect", verifier.get("Developer Tools", "Proxy Auto Connect")
        .getKey());
    assertEquals(true, verifier.get("Developer Tools", "Proxy Auto Connect").getValue());
  }

  @Test
  public void testSetWithInteger() {
    preferences.set("Developer Tools", "Proxy Port", 1234);

    OperaPreferences verifier = new OperaFilePreferences(iniFile);
    assertEquals("Developer Tools", verifier.get("Developer Tools", "Proxy Port").getSection());
    assertEquals("Proxy Port", verifier.get("Developer Tools", "Proxy Port").getKey());
    assertEquals(1234, verifier.get("Developer Tools", "Proxy Port").getValue());
  }

  @Test
  public void testSetWithUnknownPreference() {
    preferences.set(new OperaGenericPreferences.GenericPreference("foo", "bar", "baz"));

    OperaPreferences verifier = new OperaFilePreferences(iniFile);
    assertNotSame(prefCountBefore, preferences.size());
    assertNotSame(prefCountBefore, verifier.size());

    assertEquals("foo", verifier.get("foo", "bar").getSection());
    assertEquals("bar", verifier.get("foo", "bar").getKey());
    assertEquals("baz", verifier.get("foo", "bar").getValue());
  }

  @Test
  public void testSetWithKnownPreference() {
    preferences.set(new OperaGenericPreferences.GenericPreference("Developer Tools", "Proxy Host",
                                                                  "1.2.3.4"));

    OperaPreferences verifier = new OperaFilePreferences(iniFile);
    assertEquals(prefCountBefore, verifier.size());
    assertEquals("Developer Tools", verifier.get("Developer Tools", "Proxy Host").getSection());
    assertEquals("Proxy Host", verifier.get("Developer Tools", "Proxy Host").getKey());
    assertEquals("1.2.3.4", verifier.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testPreferencesAreSet() {
    profile.preferences().set("Personal Info", "Country", "Norway");
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaSettings.Capability.PROFILE.getCapability(), profile);
    driver = new TestDriverBuilder().using(capabilities).get();
    assertEquals("Norway", driver.preferences().get("Personal Info", "Country").getValue());
  }

  @Test
  public void testPreferencesAreSetWithExistingObject() {
    profile.preferences().set("Personal Info", "Country", "Norway");
    profile.setPreferences(preferences);
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaSettings.Capability.PROFILE.getCapability(), profile);
    driver = new TestDriverBuilder().using(capabilities).get();
    assertEquals("Norway", driver.preferences().get("Personal Info", "Country").getValue());
  }

  // If we have an existing OperaGenericPreferences object this will not write the preferences to
  // disk, so in this test we will check whether OperaProfile.setPreferences() does that for us.
  @Test
  public void testPreferencesAreSetWithGenericPreferenceObject() {
    OperaPreferences preferences = new OperaGenericPreferences();
    profile.preferences().set("Personal Info", "Country", "Norway");
    profile.setPreferences(preferences);
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaSettings.Capability.PROFILE.getCapability(), profile);
    driver = new TestDriverBuilder().using(capabilities).get();
    assertEquals("Norway", driver.preferences().get("Personal Info", "Country").getValue());
  }

  // Make sure we can handle/parse a pref that has no value (i.e. it lacks a = in the line)
  @Test
  public void testPreferenceWithNoValue() throws IOException {
    File prefsFile = temporaryFolder.newFile();
    Files.write("[Test]\nPrefWithNoValue\nPrefWithValue=1\n", prefsFile, Charsets.UTF_8);
    OperaFilePreferences prefs = new OperaFilePreferences(prefsFile);
    assertEquals("", prefs.get("Test", "PrefWithNoValue").getValue());
    assertEquals(true, prefs.get("Test", "PrefWithValue").getValue());
  }
}