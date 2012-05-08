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

import com.google.common.collect.ImmutableList;

import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.DESKTOP;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

@NoDriver
public class OperaLauncherRunnerTest extends OperaDriverTestCase {

  private OperaSettings settings;
  private TestOperaLauncherRunner runner;

  @Before
  public void beforeEach() {
    settings = new OperaSettings();
    settings.logging().setLevel(Level.FINE);
  }

  @After
  public void afterEach() {
    try {
      if (runner != null && runner.isOperaRunning()) {
        runner.stopOpera();
        runner.shutdown();
        assertFalse(runner.isOperaRunning());
      }
    } catch (Exception e) {
      // ignore
    } finally {
      runner = null;
    }
  }

  @Test
  public void testConstructor() {
    runner = new TestOperaLauncherRunner();
    assertNotNull(runner);
  }

  @Test
  public void testConstructorWithSettingsBinary() {
    settings.setBinary(new File(OperaPaths.operaPath()));
    runner = new TestOperaLauncherRunner(settings);
    assertNotNull(runner);
  }

  @Test
  public void profileArgumentNotSetIfProductIsAll() {
    assertEquals(OperaProduct.ALL, settings.getProduct());
    runner = new TestOperaLauncherRunner(settings);
    assertThat(runner.buildArguments(), hasItem(not("-profile")));
  }

  @Test
  public void profileArgumentSetIfProductIsSpecified() {
    assertEquals(OperaProduct.ALL, settings.getProduct());

    OperaProduct product = OperaProduct.DESKTOP;
    settings.setProduct(product);
    runner = new TestOperaLauncherRunner(settings);

    List<String> arguments = runner.buildArguments();
    assertThat(arguments, hasItem("-profile"));
    assertThat(arguments, hasItem(product.toString()));
  }

  @Test
  public void testDefaultCrashedState() {
    runner = new TestOperaLauncherRunner(settings);
    assertFalse(runner.hasOperaCrashed());
  }

  @Test
  public void testDefaultIsOperaRunning() {
    runner = new TestOperaLauncherRunner(settings);
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void testStartAndStopOpera() {
    runner = new TestOperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test(expected = OperaRunnerException.class)
  public void testShutdownLauncher() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();
    // Expecting OperaRunnerException as we tried to start Opera when launcher isn't running
    runner.startOpera();
  }

  @Test
  public void testConstructorWithSettingsArguments() {
    runner = new TestOperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void testDoubleShutdown() {
    runner = new TestOperaLauncherRunner(settings);
    runner.stopOpera();
    runner.shutdown();
    // verify that a second shutdown call doesn't do any harm (shouldn't)
    runner.shutdown();
  }

  @Test
  @Ignore(products = DESKTOP, value = "mzajaczkowski_watir_1_cleaned contains fix for this")
  public void testStartAndStopOperaTenTimes() {
    runner = new TestOperaLauncherRunner(settings);

    for (int i = 0; i < 10; i++) {
      runner.startOpera();
      assertTrue(runner.isOperaRunning());
      runner.stopOpera();
      assertFalse(runner.isOperaRunning());
    }

    runner.shutdown();
  }

  @Test
  public void testBadLauncher() throws IOException {
    assertTrue("Imposter launcher exists", resources.executableBinary().exists());
    settings.setLauncher(resources.executableBinary());

    try {
      runner = new TestOperaLauncherRunner(settings);
      fail("Did not throw OperaRunnerException");
    } catch (OperaRunnerException e) {
      if (Platform.getCurrent().is(Platform.WINDOWS)) {
        assertTrue("Expected a immediate exit, got: " + e,
                   e.getMessage().toLowerCase().contains("exited immediately"));
      } else {
        assertTrue("Expected a timeout exception, got: " + e,
                   e.getMessage().toLowerCase().contains("timeout"));
      }
    }
  }

  @Test
  // TODO(andreastt): Trigger something which actually generates a crashlog
  public void testGetOperaDefaultCrashlog() {
    runner = new TestOperaLauncherRunner(settings);
    runner.startOpera();
    String crashlog = runner.getOperaCrashlog();
    assertNull(crashlog);
  }

  @Test
  public void testSaveScreenshot() {
    runner = new TestOperaLauncherRunner(settings);
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

    public TestOperaLauncherRunner() {
      super();
    }

    public TestOperaLauncherRunner(OperaSettings settings) {
      super(settings);
    }

    public ImmutableList<String> buildArguments() {
      return super.buildArguments();
    }

    public static Level toLauncherLoggingLevel(Level javaLevel) {
      return OperaLauncherRunner.toLauncherLoggingLevel(javaLevel);
    }
  }

}