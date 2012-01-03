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

import com.google.common.io.Files;

import com.opera.core.systems.preferences.OperaFilePreferences;
import com.opera.core.systems.preferences.OperaGenericPreferences;
import com.opera.core.systems.preferences.OperaPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

public class OperaFilePreferencesTest extends OperaDriverTestCase {

  public static TestOperaDriver newDriver;
  public OperaPreferences preferences;
  public int prefCountBefore = 0;
  public File iniFile;
  public OperaProfile profile;
  public File profileDirectory;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /*
  // Replace OperaDriverTestCase setup and teardown so that we don't launch Opera
  @BeforeClass
  public static void setUpBeforeClass() {
    initFixtures();
  }
  */

  @Before
  public void setUp() throws IOException {
    if (driver.utils().getProduct().contains("desktop")) {
      iniFile = temporaryFolder.newFile("operaprefs.ini");
      profileDirectory = temporaryFolder.getRoot();
    } else {
      profileDirectory = new File(driver.utils().getBinaryPath()).getParentFile();
      iniFile = new File(profileDirectory + File.separator + "opera.ini");
    }

    // Make a new copy in a temporary filesystem so we don't overwrite our fixture
    // TODO(andreastt): This should be done more elegantly in OperaDriverTestCase
    try {
      Files.copy(fixtureFile("profile" + File.separator + "opera.ini"), iniFile);
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
      driver.quit();
    }

    if (newDriver != null && newDriver.isRunning()) {
      newDriver.quit();
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
    assertEquals("Developer Tools",
                 verifier.get("Developer Tools", "Proxy Auto Connect").getSection());
    assertEquals("Proxy Auto Connect",
                 verifier.get("Developer Tools", "Proxy Auto Connect").getKey());
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
    preferences.set(
        new OperaGenericPreferences.GenericPreference("Developer Tools", "Proxy Host", "1.2.3.4"));

    OperaPreferences verifier = new OperaFilePreferences(iniFile);
    assertEquals(prefCountBefore, verifier.size());
    assertEquals("Developer Tools", verifier.get("Developer Tools", "Proxy Host").getSection());
    assertEquals("Proxy Host", verifier.get("Developer Tools", "Proxy Host").getKey());
    assertEquals("1.2.3.4", verifier.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testPreferencesAreSet() {
    profile.preferences().set("User Prefs", "Ignore Unrequested Popups", false);
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaDriver.PROFILE, profile);
    newDriver = new TestOperaDriver(capabilities);
    assertFalse(
        (Boolean) newDriver.preferences().get("User Prefs", "Ignore Unrequested Popups")
            .getValue());
  }

  @Test
  public void testPreferencesAreSetWithExistingObject() {
    preferences.set("User Prefs", "Ignore Unrequested Popups", false);
    profile.setPreferences(preferences);
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaDriver.PROFILE, profile);
    newDriver = new TestOperaDriver(capabilities);
    assertFalse(
        (Boolean) newDriver.preferences().get("User Prefs", "Ignore Unrequested Popups")
            .getValue());
  }

  // If we have an existing OperaGenericPreferences object this will not write the preferences to
  // disk, so in this test we will check whether OperaProfile.setPreferences() does that for us.
  @Test
  public void testPreferencesAreSetWithGenericPreferenceObject() {
    OperaPreferences preferences = new OperaGenericPreferences();
    preferences.set("User Prefs", "Ignore Unrequested Popups", false);
    profile.setPreferences(preferences);
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaDriver.PROFILE, profile);
    newDriver = new TestOperaDriver(capabilities);
    assertFalse(
        (Boolean) newDriver.preferences().get("User Prefs", "Ignore Unrequested Popups")
            .getValue());
  }

}