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

import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.io.Zip;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@NoDriver
public class OperaProfileTest extends OperaDriverTestCase {

  public OperaProfile profile;
  public File temporaryProfile;
  public File existingProfile;

  @Rule
  public TemporaryFolder existingProfileDirectory = new TemporaryFolder();

  @Rule
  public TemporaryFolder temporaryProfileDirectory = new TemporaryFolder();

  @Before
  public void beforeEach() throws IOException {
    existingProfile = existingProfileDirectory.getRoot();
    temporaryProfile = temporaryProfileDirectory.getRoot();

    // Prepare an existing profile.  Because we are afraid to overwrite any test fixtures, we'll
    // copy the existing profile from the fixtures directory into a new temporary file system.
    //
    // TODO(andreastt): Abstract this out into OperaDriverTestCase
    try {
      Files.copy(resources.locate("profile/opera.ini"),
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
  public void testToJson() throws IOException {
    profile = new OperaProfile(existingProfile);

    new Zip().unzip(profile.toJson(), temporaryProfile);
    OperaProfile extractedProfile = new OperaProfile(temporaryProfile.getPath());

    assertThat(profile, matchesProfile(extractedProfile));
  }

  @Test
  public void testFromJson() throws IOException {
    OperaProfile data = new OperaProfile(existingProfile);
    profile = TestOperaProfile.fromJson(temporaryProfile, data.toJson());

    assertThat(profile, matchesProfile(data));
  }

  private Matcher matchesProfile(final OperaProfile expected) {
    return new BaseMatcher() {
      public boolean matches(Object o) {
        OperaProfile actual = (OperaProfile) o;

        try {
          if ((expected.toJson().equals(actual.toJson())) &&
              (expected.preferences().size() == actual.preferences().size())) {
            return true;
          }
        } catch (IOException e) {
          // fall through, caught by JUnit
        }

        return false;
      }

      public void describeTo(Description description) {
        description.appendText(expected.toString());
      }
    };
  }

  private static class TestOperaProfile extends OperaProfile {

    public static OperaProfile fromJson(File directory, String json) throws IOException {
      new Zip().unzip(json, directory);
      return new OperaProfile(directory);
    }

  }

}