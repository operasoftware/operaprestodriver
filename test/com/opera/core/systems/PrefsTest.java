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

import com.opera.core.systems.scope.protos.PrefsProtos.Pref;
import com.opera.core.systems.scope.protos.PrefsProtos.Pref.Type;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import java.util.Map;

import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class PrefsTest extends OperaDriverTestCase {

  /*
  * Resets all used preferences to their defaults.  Remember
  * to add preferences used in tests manually to their tear
  * down method.
  */
  @After
  public void tearDown() throws Exception {
    driver.setPref("File Types Section Info", "Version",
                   driver.getDefaultPref("File Types Section Info", "Version"));
    driver.setPref("User Display Mode", "Author CSS",
                   driver.getDefaultPref("User Display Mode", "Author CSS"));
    driver.setPref("Colors", "Background", driver.getDefaultPref("Colors", "Background"));
  }

  @Test
  public void testGetValidPreference() {
    assertEquals("#ffffff", driver.getPref("Colors", "Background"));
  }

  @Test
  public void testGetValidPreferenceWithSpacesInSection() {
    assertNotNull(driver.getPref("File Types Section Info", "Version"));
  }

  @Test
  public void testGetValidPreferenceWithSpacesInKey() {
    assertNotNull(driver.getPref("Colors", "Highlighted Background Unfocused"));
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testGetInvalidPreference() {
    Exception invalidPreferenceException = null;

    try {
      driver.getPref("hooba", "flooba");
    } catch (WebDriverException e) {
      invalidPreferenceException = e;
    }

    assertNotNull(invalidPreferenceException);
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testGetPreferenceWithInvalidSection() {
    Exception invalidSectionException = null;

    try {
      driver.getPref("hoobaflooba", "Background");
    } catch (WebDriverException e) {
      invalidSectionException = e;
    }

    assertNotNull(invalidSectionException);
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testGetPreferenceWithInvalidKey() {
    Exception invalidKeyException = null;

    try {
      driver.getPref("Colors", "hoobaflooba");
    } catch (WebDriverException e) {
      invalidKeyException = e;
    }

    assertNotNull(invalidKeyException);
  }

  @Test
  public void testGetValidDefaultPreference() {
    assertEquals("#ffffff", driver.getDefaultPref("Colors", "Background"));
  }

  @Test
  public void testGetValidDefaultPreferenceWithSpacesInSection() {
    assertNotNull(driver.getDefaultPref("File Types Section Info", "Version"));
  }

  @Test
  public void testGetValidDefaultPreferenceWithSpacesInKey() {
    assertNotNull(driver.getDefaultPref("Colors", "Highlighted Background Unfocused"));
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testGetInvalidDefaultPreference() {
    Exception invalidPreference = null;

    try {
      driver.getDefaultPref("hooba", "flooba");
    } catch (WebDriverException e) {
      invalidPreference = e;
    }

    assertNotNull(invalidPreference);
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testGetDefaultPreferenceWithInvalidSection() {
    Exception invalidPreference = null;

    try {
      driver.getDefaultPref("hoobaflooba", "Background");
    } catch (WebDriverException e) {
      invalidPreference = e;
    }

    assertNotNull(invalidPreference);
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testGetDefaultPreferenceWithInvalidKey() {
    Exception invalidPreference = null;

    try {
      driver.getDefaultPref("Colors", "hoobaflooba");
    } catch (WebDriverException e) {
      invalidPreference = e;
    }

    assertNotNull(invalidPreference);
  }

  @Test
  @Ignore
  public void testListPreferencesOfSectionWithSorting() {
  }

  @Test
  @Ignore
  public void testListPreferencesOfSectionWithoutSorting() {
  }

  @Test
  @Ignore
  public void testListPreferencesWithInvalidSection() {
  }

  @Test
  public void testListAllPreferences() {
    Map<String, Map<String, Pref>> prefs = driver.listAllPrefs();

    assertTrue("Prefs contains User Prefs", prefs.containsKey("User Prefs"));
    Map<String, Pref> userPrefs = prefs.get("User Prefs");
    assertTrue("User Prefs contains Opera Directory", userPrefs.containsKey("Opera Directory"));
    assertTrue("Opera Directory", userPrefs.get("Opera Directory").getType() == Type.DIRECTORY);
  }

  @Test
  public void testSetValidPreference() {
    driver.setPref("Colors", "Background", "#eeeeee");
    assertEquals("#eeeeee", driver.getPref("Colors", "Background"));
  }

  @Test
  public void testSetValidPreferenceWithSpacesInSection() {
    driver.setPref("File Types Section Info", "Version", "2");
    assertEquals("2", driver.getPref("File Types Section Info", "Version"));
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testSetValidPreferencesWithInvalidType() {
    // User Display Mode → Author CSS expects boolean
    driver.setPref("User Display Mode", "Author CSS", "hoobaflooba");
    assertNotSame("hoobaflooba", driver.getPref("User Display Mode", "Author CSS"));
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testSetPreferenceWithInvalidSection() {
    Exception invalidSectionException = null;

    try {
      driver.setPref("hoobaflooba", "Background", "#cccccc");
    } catch (WebDriverException e) {
      invalidSectionException = e;
    }

    assertNotNull(invalidSectionException);
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testSetPreferenceWithInvalidKey() {
    Exception invalidKeyException = null;

    try {
      driver.setPref("Colors", "hoobaflooba", "#cccccc");
    } catch (WebDriverException e) {
      invalidKeyException = e;
    }

    assertNotNull(invalidKeyException);
  }

  @Test
  @Ignore("needs investigation")
  public void testSetPreferenceWithEmptyValue() {
    Exception noException = null;

    try {
      driver.setPref("Colors", "Background", "");
    } catch (WebDriverException e) {
      noException = e;
    }

    assertNull(noException);
  }

  @Test
  @Ignore("Opera closes connection if requesting invalid preference")
  // TODO(andreastt): File CORE bug
  public void testSetPreferenceWithInvalidSectionAndKey() {
    Exception invalidSectionAndKeyException = null;

    try {
      driver.setPref("hooba", "flooba", "#cccccc");
    } catch (WebDriverException e) {
      invalidSectionAndKeyException = e;
    }

    assertNotNull(invalidSectionAndKeyException);
  }

}