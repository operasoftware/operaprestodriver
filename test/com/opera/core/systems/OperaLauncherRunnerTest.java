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
import com.opera.core.systems.testing.drivers.TestDriverBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.opera.core.systems.OperaProduct.DESKTOP;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItem;

@NoDriver
public class OperaLauncherRunnerTest extends OperaDriverTestCase {

  public Logger logger = Logger.getLogger(getClass().getName());
  public OperaSettings settings;
  public TestOperaLauncherRunner runner;

  @Before
  public void beforeEach() {
    settings = new OperaSettings();
    settings.setRunner(OperaLauncherRunner.class);
    settings.setBinary(TestDriverBuilder.detect().getSettings().getBinary());
    settings.logging().setLevel(Level.FINE);
  }

  @After
  public void afterEach() {
    try {
      if (runner != null && runner.isOperaRunning()) {
        runner.stopOpera();
        assertFalse(runner.isOperaRunning());
      }
    } catch (Exception e) {
      logger.warning("Got exception while attempting to stop Opera: " + e);
    } finally {
      if (runner != null) {
        runner.shutdown();
      }
      runner = null;
      settings = null;
    }
  }

  @Test
  public void constructor() {
    runner = new TestOperaLauncherRunner();
    assertNotNull(runner);
  }

  @Test
  public void constructorWithSettingsBinary() {
    settings.setBinary(OperaBinary.find());
    runner = new TestOperaLauncherRunner(settings);
    assertNotNull(runner);
  }

  @Test
  public void verifyDefaultStateOfOperaRunning() {
    runner = new TestOperaLauncherRunner(settings);
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void launcherInDefaultLocationIsOverwritten()
      throws IOException, NoSuchAlgorithmException {
    File outdatedLauncher = resources.executableBinary();

    try {
      Files.copy(outdatedLauncher, OperaLauncherRunner.LAUNCHER_DEFAULT_LOCATION);
    } catch (FileNotFoundException e) {
      fail("Opera instance from previous was not shutdown, and leaked over into this test: " +
           e.getMessage());
    }

    try {
      runner = new TestOperaLauncherRunner(settings);
      assertFalse("launcher should have been replaced by extracted launcher",
                  Arrays.equals(md5(outdatedLauncher),
                                md5(OperaLauncherRunner.LAUNCHER_DEFAULT_LOCATION)));
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
  public void unableToFindProductForGogi() {
    settings.setBinary(null);
    settings.setProduct(OperaProduct.CORE);

    try {
      runner = new TestOperaLauncherRunner(settings);
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(OperaRunnerException.class)));
      assertThat(e.getMessage(), containsString("Unable to find executable for product " +
                                                OperaProduct.CORE.toString()));
    }
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
    assertThat(arguments, hasItem(product.getDescriptionString()));
  }

  @Test
  public void profileArgumentNotSetIfProductIsCore() {
    settings.setProduct(OperaProduct.CORE);
    settings.setBinary(OperaBinary.find(OperaProduct.ALL));
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

  @Test
  public void startAfterShutdownShouldThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
    runner.shutdown();
    assertFalse(runner.isOperaRunning());

    try {
      runner.startOpera();
      fail("Expected OperaRunnerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(OperaRunnerException.class)));
      assertThat(e.getMessage(), containsString("launcher was shutdown"));
    }
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
      fail("Expected OperaRunnerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(OperaRunnerException.class)));
      if (Platform.getCurrent().is(Platform.WINDOWS)) {
        assertThat(e.getMessage(), containsString("exited immediately"));
      } else {
        assertThat(e.getMessage(), containsString("Timeout waiting for launcher to connect"));
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

  @Test
  public void saveScreenshotAfterShutdownShouldThrow() {
    runner = new TestOperaLauncherRunner(settings);
    runner.shutdown();

    try {
      runner.saveScreenshot(0);
      fail("Expected OperaRunnerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(OperaRunnerException.class)));
      assertThat(e.getMessage(), containsString("launcher was shutdown"));
    }
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
