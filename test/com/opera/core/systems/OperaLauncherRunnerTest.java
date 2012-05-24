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

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
  public void launcherInDefaultLocationIsOverwritten()
      throws IOException, NoSuchAlgorithmException {
    File outdatedLauncher = resources.executableBinary();
    Files.copy(outdatedLauncher, OperaLauncherRunner.launcherDefaultLocation());

    try {
      runner = new TestOperaLauncherRunner(settings);
      assertFalse("launcher should have been replaced by extracted launcher",
                  Arrays.equals(md5(outdatedLauncher),
                                md5(OperaLauncherRunner.launcherDefaultLocation())));
    } catch (OperaRunnerException e) {
      if (e.getMessage().contains("Timeout")) {
        fail("launcher was not replaced");
      }
    }
  }

  @Test
  public void defaultProductIsDesktop() {
    assertEquals(OperaProduct.DESKTOP, settings.getProduct());
  }

  @Test
  public void profileArgumentNotSetIfProductIsAll() {
    settings.setProduct(OperaProduct.ALL);
    runner = new TestOperaLauncherRunner(settings);
    assertThat(runner.buildArguments(), hasItem(not("-profile")));
  }

  @Test
  public void profileArgumentSetIfProductIsSpecified() {
    OperaProduct product = OperaProduct.DESKTOP;
    settings.setProduct(product);
    runner = new TestOperaLauncherRunner(settings);

    List<String> arguments = runner.buildArguments();
    assertThat(arguments, hasItem("-profile"));
    assertThat(arguments, hasItem(product.toString()));
  }

  @Test
  public void profileArgumentNotSetIfProductIsCore() {
    settings.setProduct(OperaProduct.CORE);
    runner = new TestOperaLauncherRunner(settings);
    assertThat(runner.buildArguments(), hasItem(not("-profile")));
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
  public void startAfterShutdownShouldThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
    runner.shutdown();
    assertFalse(runner.isOperaRunning());
    runner.startOpera();
  }

  @Test(expected = OperaRunnerException.class)
  public void stopAfterShutdownShouldThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();
    runner.stopOpera();
  }

  @Test
  public void shutdownShouldNotThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();
  }

  @Test
  public void shutdownTwiceShouldNotThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();
    runner.shutdown();
  }

  @Test
  public void constructorWithSettingsArguments() {
    runner = new TestOperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
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
  public void badLauncherShouldThrow() throws IOException {
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
  public void isOperaRunning() {
    runner = new TestOperaLauncherRunner(settings);
    assertFalse(runner.isOperaRunning());
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void isOperaRunningShouldNotThrowAfterShutdown() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();
    assertFalse(runner.isOperaRunning());
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
  public void saveScreenshot() {
    runner = new TestOperaLauncherRunner(settings);
    ScreenShotReply screenshot = runner.saveScreenshot(0);
    assertNotNull(screenshot);
  }

  @Test(expected = OperaRunnerException.class)
  public void saveScreenshotAfterShutdownShouldThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();
    runner.saveScreenshot(0);
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

  /**
   * Get the MD5 hash of the given file.
   *
   * @param file file to compute a hash on
   * @return a byte array of the MD5 hash
   * @throws IOException              if file cannot be found
   * @throws NoSuchAlgorithmException if MD5 is not available
   */
  private static byte[] md5(File file) throws NoSuchAlgorithmException, IOException {
    return Files.hash(file, Hashing.md5()).asBytes();
  }

  private static class TestOperaLauncherRunner extends OperaLauncherRunner {

    public TestOperaLauncherRunner() {
      super();
    }

    public TestOperaLauncherRunner(OperaSettings settings) {
      super(settings);
    }

    public List<String> buildArguments() {
      return super.buildArguments();
    }

    public static Level toLauncherLoggingLevel(Level javaLevel) {
      return OperaLauncherRunner.toLauncherLoggingLevel(javaLevel);
    }
  }

}