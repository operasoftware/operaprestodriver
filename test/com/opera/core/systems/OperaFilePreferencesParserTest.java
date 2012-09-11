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

import com.google.common.io.Files;

import com.opera.core.systems.preferences.OperaFilePreferences;
import com.opera.core.systems.preferences.OperaPreferences;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@NoDriver
public class OperaFilePreferencesParserTest {

  private static Resources resources;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void prepare() throws IOException {
    OperaFilePreferencesParserTest.resources = new Resources();
  }

  /**
   * Makes sure we work on a copy of the resource files to avoid modifying them.
   *
   * @param source the location of the resource file
   * @return a file object of the copied file
   */
  private File resourceCopy(File source) {
    File destination = null;

    try {
      destination = temporaryFolder.newFile("resource_copy");
      Files.copy(source, destination);
      return destination;
    } catch (IOException e) {
      fail("Unable to copy resource: " + e.getMessage());
    }

    return destination;
  }

  @Test
  public void preferenceWithNoValueIsNull() throws IOException {
    OperaPreferences prefs = new OperaFilePreferences(
        resourceCopy(resources.locate("profile/opera_no_value_pref.ini")));

    assertNull(prefs.get("Test", "PrefWithNoValue").getValue());

  }

  @Test
  public void preferenceWithValueIsRecognized() throws IOException {
    OperaPreferences prefs = new OperaFilePreferences(
        resourceCopy(resources.locate("profile/opera_no_value_pref.ini")));

    assertEquals(true, prefs.get("Test", "PrefWithValue").getValue());
  }

  @Test
  public void preferenceWithBackslashIsRecognized() throws IOException {
    OperaPreferences prefs = new OperaFilePreferences(
        resourceCopy(resources.locate("profile/opera_backslashes.ini")));

    assertEquals("C:\\tmp\\user.css", prefs.get("Test", "Local CSS File").getValue());
  }

}