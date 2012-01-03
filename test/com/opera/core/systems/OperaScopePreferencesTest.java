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

import com.opera.core.systems.preferences.OperaGenericPreferences;
import com.opera.core.systems.preferences.OperaGenericPreferences.GenericPreference;
import com.opera.core.systems.preferences.OperaPreferences;
import com.opera.core.systems.preferences.OperaScopePreferences;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    assertEquals("Developer Tools", preferences.get("Developer Tools", "Proxy Host").getSection());
    assertEquals("Proxy Host", preferences.get("Developer Tools", "Proxy Host").getKey());
    assertEquals("1.2.3.4", preferences.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithBoolean() {
    preferences.set("Developer Tools", "Proxy Auto Connect", true);
    assertEquals("Developer Tools",
                 preferences.get("Developer Tools", "Proxy Auto Connect").getSection());
    assertEquals("Proxy Auto Connect",
                 preferences.get("Developer Tools", "Proxy Auto Connect").getKey());
    assertEquals(true, preferences.get("Developer Tools", "Proxy Auto Connect").getValue());
  }

  @Test
  public void testSetWithInteger() {
    preferences.set("Developer Tools", "Proxy Port", 1234);
    assertEquals("Developer Tools", preferences.get("Developer Tools", "Proxy Port").getSection());
    assertEquals("Proxy Port", preferences.get("Developer Tools", "Proxy Port").getKey());
    assertEquals(1234, preferences.get("Developer Tools", "Proxy Port").getValue());
  }

  @Test
  public void testGetCaseInsensitive() {
    preferences.set("DEVELOPER TOOLS", "PROXY HOST", "BAZ");
    assertEquals("Developer Tools", preferences.get("DEVELOPER TOOLS", "PROXY HOST").getSection());
    assertEquals("Proxy Host", preferences.get("DEVELOPER TOOLS", "PROXY HOST").getKey());
    assertEquals("BAZ", preferences.get("DEVELOPER TOOLS", "PROXY HOST").getValue());
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
  public void testResetAll() {
    preferences.set("Developer Tools", "Proxy Host", "4.3.2.1");
    preferences.resetAll();
    assertEquals("127.0.0.1", preferences.get("Developer Tools", "Proxy Host").getDefaultValue());
  }

  @Test
  public void testScopePreferenceGetType() {
    assertEquals(String.class, ((OperaScopePreferences.ScopePreference) preferences
        .get("Developer Tools", "Proxy Host")).getType());
    assertEquals(Integer.class, ((OperaScopePreferences.ScopePreference) preferences
        .get("Developer Tools", "Proxy Port")).getType());
    assertEquals(Boolean.class, ((OperaScopePreferences.ScopePreference) preferences
        .get("Developer Tools", "Proxy Auto Connect")).getType());
  }

  @Test
  public void testScopePreferenceIsEnabled() {
    assertTrue(((OperaScopePreferences.ScopePreference) preferences
        .get("Developer Tools", "Proxy Auto Connect")).isEnabled());
  }

}