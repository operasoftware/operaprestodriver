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
import com.opera.core.systems.preferences.OperaPreferences;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NoDriver
public class OperaGenericPreferencesTest extends OperaDriverTestCase {

  public static final OperaGenericPreferences preferences = new OperaGenericPreferences();

  @After
  public void afterEach() {
    preferences.clear();
  }

  @Test
  public void testMerge() {
    int prefCountBefore = preferences.size();

    OperaPreferences newPrefs = new OperaGenericPreferences();
    newPrefs.set("Developer Tools", "Proxy Host", "1.2.3.4");
    newPrefs.set("Developer Tools", "Proxy Port", 1234);
    newPrefs.set("Developer Tools", "Proxy Auto Connect", true);

    preferences.merge(newPrefs);

    assertEquals("1.2.3.4", preferences.get("Developer Tools", "Proxy Host").getValue());
    assertEquals(1234, preferences.get("Developer Tools", "Proxy Port").getValue());
    assertEquals(true, preferences.get("Developer Tools", "Proxy Auto Connect").getValue());
    assertTrue(prefCountBefore < preferences.size());
  }

  @Test
  public void testGetCaseInsensitive() {
    preferences.set("FOO", "BAR", "BAZ");
    assertEquals("FOO", preferences.get("foo", "bar").getSection());
    assertEquals("BAR", preferences.get("foo", "bar").getKey());
    assertEquals("BAZ", preferences.get("foo", "bar").getValue());
  }

  // It should be possible to set an unknown preference when interacting with the generic
  // implementation
  @Test
  public void testSetWithUnknownPreference() {
    preferences.set(new OperaGenericPreferences.GenericPreference("foo", "bar", "baz"));
    assertEquals(1, preferences.size());
  }

}