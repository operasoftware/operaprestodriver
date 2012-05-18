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

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperaDesktopDriverAutoProfileTest extends OperaDesktopDriverTestCase {
  /**
   * Start Opera with an empty 'opera.profile' capability (the default).
   */
  @Test
  public void testAutoProfileStart()
  {
    setOperaPath();
    driver = new TestOperaDesktopDriver(getDefaultCaps());
    browserPid = driver.getPid();

    assertTrue(driver.isRunning());
  }

  /**
   * Check that the large prefs path reported by the driver matches the one found in the 'opera.profile'
   * capability value.
   * @throws java.io.IOException
   */
  @Test
  public void testAutoProfileLarge() throws IOException {
    File largePrefsFile = new File(driver.getLargePreferencesPath()).getCanonicalFile();
    File capsProfileFile = new File((String)driver.getCapabilities().getCapability("opera.profile")).getCanonicalFile();

    assertEquals(largePrefsFile, capsProfileFile);
  }

  /**
   * Check that the small prefs path reported by the driver matches the one found in the 'opera.profile'
   * capability value.
   * @throws IOException
   */
  @Test
  public void testAutoProfileSmall() throws IOException {
    File smallPrefsFile = new File(driver.getSmallPreferencesPath()).getCanonicalFile();
    File capsProfileFile = new File((String)driver.getCapabilities().getCapability("opera.profile")).getCanonicalFile();

    assertEquals(smallPrefsFile, capsProfileFile);
  }

  /**
   * Check that the cache prefs path reported by the driver matches the one found in the 'opera.profile'
   * capability value.
   * @throws IOException
   */
  @Test
  public void testAutoProfileCache() throws IOException {
    File cachePrefsFile = new File(driver.getCachePreferencesPath()).getCanonicalFile();
    File capsProfileFile = new File((String)driver.getCapabilities().getCapability("opera.profile")).getCanonicalFile();

    assertEquals(cachePrefsFile, capsProfileFile);
  }

  /**
   * Check that the 'opera.profile' capability value contains the "opera-profile" string.
   * When the driver created a temporary test profile, it picks up a random name with this
   * string.
   */
  @Test
  public void testAutoProfile() throws IOException, InterruptedException {
    String largePrefsPath = driver.getLargePreferencesPath();

    assertTrue(String.format("The prefs folder reported by the browser should contain the 'opera-profile' string [%s].", largePrefsPath), largePrefsPath.matches(".*opera-profile.*"));
  }
}
