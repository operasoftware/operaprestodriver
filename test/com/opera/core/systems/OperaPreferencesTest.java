/*
Copyright 2008-2011 Opera Software ASA

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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.io.TemporaryFilesystem;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

public class OperaPreferencesTest extends OperaDriverTestCase {

  public OperaPreferences preferences;
  public File temporaryProfile;
  public File iniFile;

  // Replace OperaDriverTestCase setup and teardown so that we don't launch Opera
  @BeforeClass
  public static void setUpBeforeClass() {
    initFixtures();
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void setUp() {
    temporaryProfile =
        TemporaryFilesystem.getDefaultTmpFS().createTempDir("operadriver", "profile");
    iniFile = new File(temporaryProfile + File.separator + "opera.ini");

    try {
      Files.copy(fixtureFile("opera.ini"), iniFile);
    } catch (IOException e) {
      fail("Unable to copy preference fixture: " + e.getMessage());
    }
  }

  @Test
  public void testConstruction() {
    OperaPreferences p = new OperaPreferences();
    assertNotNull(p);
    assertEquals(0, p.getAll().size());
  }

  @Test
  public void testConstructionWithFile() {
    OperaPreferences p = new OperaPreferences(iniFile);
    assertNotNull(p);
    assertEquals(46, p.getAll().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructionWithInvalidFile() {
    OperaPreferences p = new OperaPreferences(new File("foo"));
  }

  @Test
  public void testMerge() {
    OperaPreferences p = new OperaPreferences(iniFile);
    int prefCountBefore = p.getAll().size();

    OperaPreferences newPrefs = new OperaPreferences();
    newPrefs.set("Developer Tools", "Proxy Host", "1.2.3.4");
    newPrefs.set("Developer Tools", "Proxy Port", 1234);
    newPrefs.set("Developer Tools", "Proxy Auto Connect", true);

    p.merge(newPrefs);

    assertEquals("1.2.3.4", p.get("Developer Tools", "Proxy Host").getValue());
    assertEquals(1234, p.get("Developer Tools", "Proxy Port").getValue());
    assertEquals(true, p.get("Developer Tools", "Proxy Auto Connect").getValue());
    assertEquals(prefCountBefore, p.getAll().size());
  }

  @Test
  public void testSetWithString() {
    OperaPreferences p = new OperaPreferences();
    p.set("Developer Tools", "Proxy Host", "1.2.3.4");
    assertEquals("Developer Tools", p.get("Developer Tools", "Proxy Host").getSection());
    assertEquals("Proxy Host", p.get("Developer Tools", "Proxy Host").getKey());
    assertEquals("1.2.3.4", p.get("Developer Tools", "Proxy Host").getValue());
  }

  @Test
  public void testSetWithBoolean() {
    OperaPreferences p = new OperaPreferences();
    p.set("Developer Tools", "Proxy Auto Connect", true);
    assertEquals("Developer Tools", p.get("Developer Tools", "Proxy Auto Connect").getSection());
    assertEquals("Proxy Auto Connect", p.get("Developer Tools", "Proxy Auto Connect").getKey());
    assertEquals(true, p.get("Developer Tools", "Proxy Auto Connect").getValue());
  }

  @Test
  public void testSetWithInteger() {
    OperaPreferences p = new OperaPreferences();
    p.set("Developer Tools", "Proxy Port", 1234);
    assertEquals("Developer Tools", p.get("Developer Tools", "Proxy Port").getSection());
    assertEquals("Proxy Port", p.get("Developer Tools", "Proxy Port").getKey());
    assertEquals(1234, p.get("Developer Tools", "Proxy Port").getValue());
  }

  @Test
  public void testGetCaseInsensitive() {
    OperaPreferences p = new OperaPreferences();
    p.set("FOO", "BAR", "BAZ");
    assertEquals("FOO", p.get("foo", "bar").getSection());
    assertEquals("BAR", p.get("foo", "bar").getKey());
    assertEquals("BAZ", p.get("foo", "bar").getValue());
  }

  @Test
  public void testSetWithUnknownPreference() {
    OperaPreferences p = new OperaPreferences();
    int numberOfPrefsBefore = p.getAll().size();

    p.set(new OperaPreferences.OperaPreference("foo", "bar", "baz"));

    assertNotSame(numberOfPrefsBefore, p.getAll().size());
    assertEquals("foo", p.get("foo", "bar").getSection());
    assertEquals("bar", p.get("foo", "bar").getKey());
    assertEquals("baz", p.get("foo", "bar").getValue());
  }

  @Test
  public void testSetWithKnownPreference() {
    OperaPreferences p = new OperaPreferences(iniFile);
    int numberOfPrefsBefore = p.getAll().size();

    p.set(new OperaPreferences.OperaPreference("Developer Tools", "Proxy Host", "1.2.3.4"));

    assertEquals(numberOfPrefsBefore, p.getAll().size());
    assertEquals("Developer Tools", p.get("Developer Tools", "Proxy Host").getSection());
    assertEquals("Proxy Host", p.get("Developer Tools", "Proxy Host").getKey());
    assertEquals("1.2.3.4", p.get("Developer Tools", "Proxy Host").getValue());
  }

}