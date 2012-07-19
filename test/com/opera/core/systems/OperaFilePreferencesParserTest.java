/*
 * Copyright 2011-2012 Opera Software ASA
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.opera.core.systems;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.io.Files;
import com.opera.core.systems.preferences.OperaFilePreferences;
import com.opera.core.systems.testing.Resources;

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
   * @param resource The location of the resource file
   * @return A File object of the copied file
   * @throws IOException If we could not find or copy the file
   */
  private File resourceCopy(String resource) throws IOException {
    File fileSource = resources.locate(resource);
    File fileDest = temporaryFolder.newFile("resource_copy");
    Files.copy(fileSource, fileDest);
    return fileDest;
  }

  // Make sure we can handle/parse a pref that has no value (i.e. it lacks a = in the line)
  @Test
  public void testPreferenceWithNoValue() throws IOException {
    OperaFilePreferences prefs =
        new OperaFilePreferences(resourceCopy("profile/opera_no_value_pref.ini"));
    assertEquals("", prefs.get("Test", "PrefWithNoValue").getValue());
    assertEquals(true, prefs.get("Test", "PrefWithValue").getValue());
  }

}
