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

import com.opera.core.systems.model.OperaColor;
import com.opera.core.systems.preferences.OperaGenericPreferences;
import com.opera.core.systems.preferences.OperaGenericPreferences.GenericPreference;
import com.opera.core.systems.preferences.OperaPreferences;
import com.opera.core.systems.preferences.OperaScopePreferences;
import com.opera.core.systems.preferences.OperaScopePreferences.ScopePreference;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperaScopePreferencesTest extends OperaDriverTestCase {

  public static OperaScopePreferences preferences;
  public static int prefCountBefore = 0;

  @BeforeClass
  public static void beforeAll() {
    preferences = driver.preferences();
  }

  @Before
  public void beforeEach() {
    prefCountBefore = preferences.size();
  }

  @After
  public void afterEach() {
    preferences.get("Developer Tools", "Proxy Host").reset();
    preferences.get("Developer Tools", "Proxy Auto Connect").reset();
    preferences.get("Developer Tools", "Proxy Port").reset();
  }

  @Test
  public void testPrefetchedPreferences() {
    assertTrue(preferences.size() > 0);
  }

  @Test
  public void testGet() {
    assertEquals("127.0.0.1", preferences.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithPreference() {
    preferences.set(new GenericPreference("Developer Tools", "Proxy Host", "1.2.3.4"));
    assertEquals("1.2.3.4", preferences.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithSectionAndKey() {
    preferences.set("Developer Tools", "Proxy Host", "1.2.3.4");
    assertEquals("1.2.3.4", preferences.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithString() {
    preferences.set("Developer Tools", "Proxy Host", "1.2.3.4");
    assertEquals("1.2.3.4", preferences.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithBoolean() {
    preferences.set("Developer Tools", "Proxy Auto Connect", true);
    assertEquals(true, preferences.get("Developer Tools", "Proxy Auto Connect").getValue());
  }

  @Test
  public void testSetWithInteger() {
    preferences.set("Developer Tools", "Proxy Port", 1234);
    assertEquals(1234, preferences.get("Developer Tools", "Proxy Port").getValue());
  }
  
  @Test
  public void testSetWithColor() {
    OperaColor color = new OperaColor(120,120,120);
    preferences.set("Colors", "Color.Normal", color);
    assertEquals(color, preferences.get("Colors", "Color.Normal").getValue());
  }
  
  @Test
  public void testSetWithFile() {
    File cssFile = new File("/no/file");
    preferences.set("User Prefs", "Browser CSS File", cssFile);
    assertEquals(cssFile, preferences.get("User Prefs", "Browser CSS File").getValue());
  }

  @Test
  public void testSetCaseInsensitive() {
    preferences.set("DEVELOPER TOOLS", "PROXY HOST", "BAZ");
    assertEquals("BAZ", preferences.get("Developer Tools", "Proxy Host").getValue());
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

    assertEquals(prefCountBefore, preferences.size());
  }

  @Test
  public void testGetTypeString() {
    assertEquals(String.class, preferences.get("Developer Tools", "Proxy Host").getType());
  }

  @Test
  public void testGetTypeInteger() {
    assertEquals(Integer.class, preferences.get("Developer Tools", "Proxy Port").getType());
  }

  @Test
  public void testGetTypeBoolean() {
    assertEquals(Boolean.class, preferences.get("Developer Tools", "Proxy Auto Connect").getType());
  }

  @Test
  public void testGetTypeFile() {
    assertEquals(File.class, preferences.get("User Prefs", "Browser CSS File").getType());
  }

  @Test
  public void testGetTypeColor() {
    assertEquals(OperaColor.class, preferences.get("Colors", "Color.H1").getType());
  }

  @Test
  public void testIsEnabled() {
    assertTrue(preferences.get("Developer Tools", "Proxy Auto Connect").isEnabled());
  }

  @Test
  public void testReset() {
    ScopePreference preference = preferences.get("Developer Tools", "Proxy Host");
    preference.setValue("4.3.2.1");
    preference.reset();
    assertEquals("127.0.0.1", preference.getValue());
  }

  @Test
  public void testResetAll() {
    preferences.set("Developer Tools", "Proxy Host", "4.3.2.1");
    preferences.resetAll();
    assertEquals("127.0.0.1", preferences.get("Developer Tools", "Proxy Host").getDefaultValue());
  }

}