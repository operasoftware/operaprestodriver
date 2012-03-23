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

import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.testing.Ignore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.io.File;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.DESKTOP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperaLauncherRunnerTest {

  private OperaSettings settings;
  private OperaLauncherRunner runner;

  @Before
  public void beforeEach() {
    settings = new OperaSettings();
    settings.logging().level(Level.FINE);
  }

  @After
  public void afterEach() {
    if (runner != null && runner.isOperaRunning()) {
      runner.stopOpera();
      runner.shutdown();
      assertFalse(runner.isOperaRunning());
    }
  }

  @Test
  public void testConstructor() {
    runner = new OperaLauncherRunner();
    assertNotNull(runner);
  }

  @Test
  public void testConstructorWithSettingsBinary() {
    settings.setBinary(new File(OperaPaths.operaPath()));
    runner = new OperaLauncherRunner(settings);
    assertNotNull(runner);
  }

  @Test
  public void testDefaultCrashedState() {
    runner = new OperaLauncherRunner(settings);
    assertFalse(runner.hasOperaCrashed());
  }

  @Test
  public void testDefaultIsOperaRunning() {
    runner = new OperaLauncherRunner(settings);
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOpera() {
    runner = new OperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test(expected = OperaRunnerException.class)
  public void testShutdownLauncher() {
    runner = new OperaLauncherRunner(settings);
    runner.shutdown();
    // Expecting OperaRunnerException as we tried to start Opera when launcher isn't running
    runner.startOpera();
  }

  @Test
  public void testConstructorWithSettingsArguments() {
    runner = new OperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testDoubleShutdown() {
    runner = new OperaLauncherRunner(settings);
    runner.stopOpera();
    runner.shutdown();
    // verify that a second shutdown call doesn't do any harm (shouldn't)
    runner.shutdown();
  }

  @Test
  @Ignore(products = DESKTOP, value = "mzajaczkowski_watir_1_cleaned contains fix for this")
  public void testStartAndStopOperaTenTimes() {
    runner = new OperaLauncherRunner(settings);

    for (int i = 0; i < 10; i++) {
      runner.startOpera();
      assertTrue(runner.isOperaRunning());
      runner.stopOpera();
      assertFalse(runner.isOperaRunning());
    }

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

    settings.setLauncher(fakeLauncher.getCanonicalFile());

    try {
      runner = new OperaLauncherRunner(settings);
      fail("Did not throw OperaRunnerException");
    } catch (OperaRunnerException e) {
      assertTrue("Throws timeout error", e.getMessage().toLowerCase().contains("timeout"));
    }
  }

  @Test
  // TODO(andreastt): Trigger something which actually generates a crashlog
  public void testGetOperaDefaultCrashlog() {
    runner = new OperaLauncherRunner(settings);
    runner.startOpera();
    String crashlog = runner.getOperaCrashlog();
    assertNull(crashlog);
  }

  @Test
  public void testSaveScreenshot() {
    runner = new OperaLauncherRunner(settings);
    ScreenShotReply screenshot = runner.saveScreenshot(0);
    assertNotNull(screenshot);
    runner.shutdown();
  }

  @Test
  public void testLoggingLevel() {
    assertEquals(Level.SEVERE, TestOperaLauncherRunner.toLauncherLoggingLevel(Level.SEVERE));
  }

  @Test
  public void testLoggingLevelToAll() {
    assertEquals(Level.FINEST, TestOperaLauncherRunner.toLauncherLoggingLevel(Level.ALL));
  }

  @Test
  public void testLoggingLevelToConfig() {
    assertEquals(Level.FINE, TestOperaLauncherRunner.toLauncherLoggingLevel(Level.CONFIG));
  }

  @Test
  public void testLoggingLevelToFiner() {
    assertEquals(Level.FINE, TestOperaLauncherRunner.toLauncherLoggingLevel(Level.FINER));
  }

  @Test
  public void testLoggingLevelToOff() {
    assertEquals(Level.OFF, TestOperaLauncherRunner.toLauncherLoggingLevel(Level.OFF));
  }

  private static class TestOperaLauncherRunner extends OperaLauncherRunner {

    public static Level toLauncherLoggingLevel(Level javaLevel) {
      return OperaLauncherRunner.toLauncherLoggingLevel(javaLevel);
    }
  }

}