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

import com.google.common.io.Files;

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class OperaProfileTest extends OperaDriverTestCase {

  public OperaProfile profile;
  public File temporaryProfile;
  public File existingProfile;

  @Rule
  public TemporaryFolder existingProfileDirectory = new TemporaryFolder();

  @Rule
  public TemporaryFolder temporaryProfileDirectory = new TemporaryFolder();

  @BeforeClass
  public static void setUpBeforeClass() {
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void setUp() {
    existingProfile = existingProfileDirectory.getRoot();
    temporaryProfile = temporaryProfileDirectory.getRoot();

    // Prepare an existing profile.  Because we are afraid to overwrite any test fixtures, we'll
    // copy the existing profile from the fixtures directory into a new temporary file system.
    //
    // TODO(andreastt): Abstract this out into OperaDriverTestCase
    try {
      Files.copy(fixtureFile("profile" + File.separator + "opera.ini"),
                 new File(existingProfile.getPath() + File.separator + "operaprefs.ini"));
    } catch (IOException e) {
      fail("Unable to prepare new profile");
    }
  }

  @Test
  public void testConstructionWithNoArguments() {  // new temporary profile
    profile = new OperaProfile();
    assertNotNull(profile);
    assertEquals(0, profile.preferences().size());
  }

  @Test
  public void testConstructionWithNewProfileString() {  // use specified (empty) profile
    profile = new OperaProfile(temporaryProfile.getPath());
    assertNotNull(profile);
  }

  @Test
  public void testConstructionWithExistingProfileString() {  // use specified profile
    profile = new OperaProfile(existingProfile.getPath());
    assertNotNull(profile);
    assertEquals(46, profile.preferences().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructionWithEmptyString() {
    profile = new OperaProfile("");
  }

  @Test
  public void testConstructionWithNewProfileDirectory() {  // use specified (empty) profile
    profile = new OperaProfile(temporaryProfile);
    assertNotNull(profile);
  }

  @Test
  public void testConstructionWithExistingProfileDirectory() {  // use specified profile
    profile = new OperaProfile(existingProfile);
    assertNotNull(profile);
    assertEquals(46, profile.preferences().size());
  }

  @Test
  public void testPreferences() {
    OperaProfile profile = new OperaProfile();
    assertNotNull(profile.preferences());
  }

  @Test
  public void testToJson() {

  }

  @Test
  public void testFromJson() {

  }

}