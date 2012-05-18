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
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperaDesktopDriverManualProfileTest extends OperaDesktopDriverTestCase {
  /**
   * Check that the driver reports the browser as running after starting it with a manual profile
   * path override.
   * This is what happens when you pass the '-p' parameter to desktopwatir.
   */
  @Test
  public void testManualProfileBrowserStartsOK()
  {
    DesiredCapabilities caps = getDefaultCaps();
    profileTmpFolder = tmpFolder.newFolder("OperaDesktopDriver-junit-test");
    String profileTmpFolderPath = profileTmpFolder.getAbsolutePath();
    caps.setCapability("opera.profile", profileTmpFolderPath);
    setOperaPath();
    driver = new TestOperaDesktopDriver(caps);
    browserPid = driver.getPid();

    assertTrue(driver.isRunning());
  }

  /**
   * Check that the large prefs dir reported by the driver matches the one we requested
   * on the command line/by the capability value.
   * @throws java.io.IOException
   */
  @Test
  public void testManualProfileLargePrefsDirMatches() throws IOException {
    File profileTmpFolderPath = new File(profileTmpFolder.getAbsolutePath()).getCanonicalFile();
    File largePrefsPath = new File(driver.getLargePreferencesPath()).getCanonicalFile();

    assertEquals(profileTmpFolderPath, largePrefsPath);
  }

  /**
   * Check that the small prefs dir reported by the driver matches the one we requested
   * on the command line/by the capability value.
   * @throws IOException
   */
  @Test
  public void testManualProfileSmallPrefsDirMatches() throws IOException {
    File profileTmpFolderPath = new File(profileTmpFolder.getAbsolutePath()).getCanonicalFile();
    File smallPrefsPath = new File(driver.getSmallPreferencesPath()).getCanonicalFile();

    assertEquals(profileTmpFolderPath,  smallPrefsPath);
  }

  /**
   * Check that the cache prefs dir reported by the driver matches the one we requested
   * on the command line/by the capability value.
   * @throws IOException
   */
  @Test
  public void testManualProfileCachePrefsDirMatches() throws IOException {
    File profileTmpDirPath = new File(profileTmpFolder.getAbsolutePath()).getCanonicalFile();
    File cachePrefsPath = new File(driver.getCachePreferencesPath()).getCanonicalFile();

    assertEquals(profileTmpDirPath, cachePrefsPath);
  }

  /**
   * Check that the 'opera.profile' capability value reported by the driver matches the one
   * we set before the driver was started.
   * This test is supposed to check if no bad magic happens in the OperaDriver/OperaDesktopDriver
   * constructors.
   * @throws IOException
   */
  @Test
  public void testManualProfileProfileCapValueMatches() {
    String profileTmpDirPath = profileTmpFolder.getAbsolutePath();

    Map<String, String> expectedValues = new HashMap<String, String>();
    expectedValues.put("opera.profile", profileTmpDirPath);
    checkCaps(driver.getCapabilities(), expectedValues);
  }
}
