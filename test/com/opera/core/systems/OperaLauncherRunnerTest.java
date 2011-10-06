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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperaLauncherRunnerTest {

  private static DesiredCapabilities capabilities;
  private static OperaLauncherRunner runner;

  @Test
  public void testOperaDriverSettings() {
    capabilities = (DesiredCapabilities) OperaDriver.getDefaultCapabilities();
    Assert.assertNotNull(capabilities);
  }

  @Test
  public void testDefaultOperaBinaryLocation() {
    // the opera binary location should default to null
    Assert.assertNull(capabilities.getCapability(OperaDriver.BINARY));
  }

  @Test
  public void testSetOperaBinaryLocation() {
    capabilities.setCapability(OperaDriver.BINARY, "/spartan/ramdisk/launcher");
    Assert
        .assertEquals("/spartan/ramdisk/launcher", capabilities.getCapability(OperaDriver.BINARY));
  }

  @Test
  public void testDefaultOperaBinaryArguments() {
    // the opera binary arguments should default to null
    assertEquals(capabilities.getCapability(OperaDriver.ARGUMENTS), "");
  }

  @Test
  public void testSetOperaBinaryArguments() {
    capabilities.setCapability(OperaDriver.ARGUMENTS,
                               "-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher");
    assertEquals("-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher",
                 capabilities.getCapability(OperaDriver.ARGUMENTS));
  }

  @Test
  public void testOperaLauncherRunnerConstructorWithSettings() {
    OperaPaths paths = new OperaPaths();

    capabilities.setCapability(OperaDriver.ARGUMENTS, "");
    capabilities.setCapability(OperaDriver.BINARY, paths.operaPath());
    capabilities.setCapability(OperaDriver.LAUNCHER, paths.launcherPath());

    runner = new OperaLauncherRunner(capabilities);
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
    capabilities.setCapability(OperaDriver.ARGUMENTS, "-geometry 1024x768");
    runner = new OperaLauncherRunner(capabilities);
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
    capabilities.setCapability(OperaDriver.ARGUMENTS, "-geometry 640x480");
    runner = new OperaLauncherRunner(capabilities);
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

    capabilities.setCapability(OperaDriver.LAUNCHER, fakeLauncher.getCanonicalPath());

    try {
      runner = new OperaLauncherRunner(capabilities);
      Assert.fail("Did not throw OperaRunnerException");
    } catch (OperaRunnerException e) {
      assertTrue("Throws timeout error", e.getMessage().toLowerCase().contains("timeout"));
    }
  }

}