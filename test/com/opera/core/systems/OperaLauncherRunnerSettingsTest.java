/*
Copyright 2011 Opera Software ASA

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

import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherRunnerSettings;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.io.File;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperaLauncherRunnerSettingsTest {

  private TestOperaLauncherRunnerSettings settings;
  private static
  File
      launcherOnSystem =
      new File(System.getProperty("user.home") + File.separator + ".launcher" + File.separator
               + TestOperaLauncherRunnerSettings.getLauncherNameForOS2());
  private static File fakeBinary;

  @BeforeClass
  public static void beforeAll() {
    if (Platform.getCurrent().is(Platform.WINDOWS)) {
      fakeBinary = new File("C:\\WINDOWS\\system32\\find.exe");
    } else {
      fakeBinary = new File("/bin/echo");
    }
  }

  @Before
  public void setUp() {
    settings = new TestOperaLauncherRunnerSettings();
  }

  @Test
  public void testDefaultSettings() {
    OperaLauncherRunnerSettings
        defaultSettings =
        TestOperaLauncherRunnerSettings.getDefaultSettings();
    assertNotNull(defaultSettings);
    assertEquals(OperaProduct.CORE, defaultSettings.getProduct());
    assertTrue("default port should be greater than 0", defaultSettings.getPort() > 0);
    assertTrue(defaultSettings.getArguments() instanceof OperaCoreArguments);
  }

  @Test
  public void testSetLoggingLevel() {
    settings.setLoggingLevel(Level.SEVERE);
    assertEquals(Level.SEVERE, settings.getLoggingLevel());
  }

  @Test
  public void testSetLoggingLevelToAll() {
    settings.setLoggingLevel(Level.ALL);
    assertEquals(Level.FINEST, settings.getLoggingLevel());
  }

  @Test
  public void testSetLoggingLevelToConfig() {
    settings.setLoggingLevel(Level.FINE);
    assertEquals(Level.INFO, settings.getLoggingLevel());
  }

  @Test
  public void testSetLoggingLevelToFiner() {
    settings.setLoggingLevel(Level.FINER);
    assertEquals(Level.FINE, settings.getLoggingLevel());
  }

  @Test
  public void testSetLoggingLevelToOff() {
    settings.setLoggingLevel(Level.OFF);
    assertEquals(Level.OFF, settings.getLoggingLevel());
  }

  @Test
  public void testGetLoggingLevel() {
    assertEquals(Level.INFO, settings.getLoggingLevel());
  }

  @Test
  public void testGetLauncherWithNoLauncherOnSystem() {
    // Delete launcher
    if (!launcherOnSystem.delete()) {
      fail("Unable to delete launcher on system");
    }

    assertNotNull("getLauncher() should never be null", settings.getLauncher());
    assertTrue("extracted launcher should exist", settings.getLauncher().exists());
    assertTrue("launcher on system should exist", launcherOnSystem.exists());
  }

  @Test
  public void testGetLauncherWithLauncherOnSystem() {
    assertTrue("extracted launcher should exist", settings.getLauncher().exists());
    assertTrue("launcher on system should exist", launcherOnSystem.exists());
  }

  @Test
  public void testSetLauncher() {
    settings.setLauncher(fakeBinary);
    assertEquals(fakeBinary, settings.getLauncher());
  }

  @Test(expected = OperaRunnerException.class)
  public void testSetLauncherWithInvalidLauncher() {
    settings.setLauncher(new File("/path/to/invalid/launcher"));
  }

  public static class TestOperaLauncherRunnerSettings extends OperaLauncherRunnerSettings {

    public TestOperaLauncherRunnerSettings() {
      super();
    }

    public static String getLauncherNameForOS2() {
      return getLauncherNameForOS();
    }

  }

}