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

import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.settings.OperaDriverSettings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OperaDriverSettingsTest {

  private static OperaDriverSettings settings;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testOperaDriverSettings() {
    settings = new OperaDriverSettings();
    assertNotNull(settings);
  }

  @Test
  public void testDefaultDoRunOperaLauncherFromOperaDriver() {
    // default is to run launcher from the driver, so this should always be true
    assertTrue(settings.doRunOperaLauncherFromOperaDriver());
  }

  @Test
  public void testSetRunOperaLauncherFromOperaDriver() {
    exception.expect(UnsupportedOperationException.class);
    settings.setRunOperaLauncherFromOperaDriver(false);
  }

  @Test
  public void testDefaultLauncherListeningPort() {
    // the listening port should default to 9999
    assertEquals(9999, settings.getOperaLauncherListeningPort());
  }

  @Test
  public void testSetLauncherListeningPort() {
    exception.expect(UnsupportedOperationException.class);
    settings.setOperaLauncherListeningPort(5555);
  }

  @Test
  public void testDefaultOperaBinaryLocation() {
    // the opera binary location should default to null
    assertNull("", settings.getOperaBinaryLocation());
  }

  @Test
  public void testSetOperaBinaryLocation() {
    settings.setOperaBinaryLocation("/spartan/ramdisk/launcher");
    assertEquals("/spartan/ramdisk/launcher", settings.getOperaBinaryLocation());
  }

  @Test
  public void testDefaultOperaBinaryArguments() {
    // the opera binary arguments should default to null
    assertEquals("", settings.getOperaBinaryArguments());
  }

  @Test
  public void testSetOperaBinaryArguments() {
    settings.setOperaBinaryArguments(
        "-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher");
    assertEquals("-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher",
                 settings.getOperaBinaryArguments());
  }

}