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

import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.settings.OperaDriverSettings;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.Platform;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperaLauncherRunnerSettingsTest {

  private static OperaDriverSettings settings;
  private static OperaLauncherRunner runner;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testOperaDriverSettings() {
    settings = new OperaDriverSettings();
    Assert.assertNotNull(settings);
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
    Assert.assertNull(settings.getOperaBinaryLocation());
  }

  @Test
  public void testSetOperaBinaryLocation() {
    settings.setOperaBinaryLocation("/spartan/ramdisk/launcher");
    assertEquals("/spartan/ramdisk/launcher", settings.getOperaBinaryLocation());
  }

  @Test
  public void testDefaultOperaBinaryArguments() {
    // the opera binary arguments should default to null
    assertEquals(settings.getOperaBinaryArguments(), "");
  }

  @Test
  public void testSetOperaBinaryArguments() {
    settings.setOperaBinaryArguments(
        "-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher");
    assertEquals("-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher",
                 settings.getOperaBinaryArguments());
  }

  @Test
  public void testOperaLauncherRunnerConstructorWithSettings() {
    OperaPaths paths = new OperaPaths();
    settings.setOperaBinaryArguments("");
    settings.setOperaBinaryLocation(paths.operaPath());
    settings.setOperaLauncherBinary(paths.launcherPath());

    runner = new OperaLauncherRunner(settings);
    Assert.assertNotNull(runner);
  }

  @Test
  public void testDefaultCrashedState() {
    assertFalse(runner.hasOperaCrashed());
  }

  @Test
  public void testDefaultIsOperaRunning() {
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartOpera() {
    Assert.assertNotNull(runner);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testShutdownOpera() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testShutdownLauncher() {
    runner.shutdown();

    try {
      runner.startOpera();
      fail(
          "This test should have generated a OperaRunnerException as we tried to start Opera when the Launcher isn't running...");
    } catch (OperaRunnerException e) {
    }
  }

  @Test
  public void testOperaLauncherRunnerConstructorWithSettings2() {
    settings.setOperaBinaryArguments("-geometry 1024x768");
    runner = new OperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testShutdown2() {
    runner.stopOpera();
    runner.shutdown();

    // verify that a second shutdown call doesn't do any harm (shouldn't)
    runner.shutdown();
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundOneStart() {
    settings.setOperaBinaryArguments("-geometry 640x480");
    runner = new OperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundOneStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundTwoStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundTwoStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundThreeStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundThreeStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundFourStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundFourStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundFiveStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundFiveStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundSixStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundSixStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundSevenStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundSevenStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundEightStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundEightStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundNineStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundNineStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundTenStart() {
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOperaTenTimesRoundTenStop() {
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
    runner.shutdown();
  }

  @Test
  public void testBadLauncher() throws Exception {
    File fakeLauncher;
    // Programs that should be installed that have no side effects when run
    if (Platform.getCurrent().is(Platform.WINDOWS)) {
      fakeLauncher = new File("C:\\WINDOWS\\system32\\find.exe");
    } else {
      fakeLauncher = new File("/bin/echo");
    }

    assertTrue("Imposter launcher exists", fakeLauncher.exists());

    settings.setOperaLauncherBinary(fakeLauncher.getCanonicalPath());

    try {
      runner = new OperaLauncherRunner(settings);
      Assert.fail("Did not throw OperaRunnerException");
    } catch (OperaRunnerException e) {
      assertTrue("Throws timeout error", e.getMessage().toLowerCase().contains("timeout"));
    }
  }

}