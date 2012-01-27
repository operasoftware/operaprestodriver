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

import com.google.common.io.Files;

import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.OperaRunnerSettings;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.fail;
import static org.openqa.selenium.Platform.WINDOWS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OperaRunnerSettingsTest extends OperaDriverTestCase {

  public OperaRunnerSettings settings;
  public TestOperaRunner runner;
  public static File fakeBinary;
  public File iniFile;
  public String profile;

  @Rule
  public TemporaryFolder temporaryProfile = new TemporaryFolder();

  // Replace OperaDriverTestCase setup and tear down so that we don't launch Opera
  @BeforeClass
  public static void setUpBeforeClass() {
    if (OperaDriverTestCase.getCurrentPlatform().is(Platform.WINDOWS)) {
      fakeBinary = new File("C:\\WINDOWS\\system32\\find.exe");
    } else {
      fakeBinary = new File("/bin/echo");
    }
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void beforeEach() {
    settings = new OperaRunnerSettings();

    // Make a new copy in a temporary file system so we don't overwrite our fixture
    // TODO(andreastt): This should be done more elegantly in OperaDriverTestCase
    try {
      iniFile = temporaryProfile.newFile("operaprefs.ini");
      Files.copy(fixtureFile("profile" + File.separator + "opera.ini"), iniFile);
    } catch (IOException e) {
      fail("Unable to copy preference fixture: " + e.getMessage());
    }

    profile = temporaryProfile.getRoot().getAbsolutePath();

    // Make sure we always reset OPERA_ARGS.  Since the constructor of OperaRunnerSettings has
    // different behaviour depending on whether its set, it is going to impact the outcome of our
    // tests.
    try {
      setEnvVar("OPERA_ARGS", "");
    } catch (IllegalAccessException e) {
    } catch (NoSuchFieldException e) {
    }

    settings = new OperaRunnerSettings();
  }

  @Test
  public void testConstruction() {
    assertNotNull(settings);
    assertNotNull(settings.getArguments());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void testConstructionWithEnvironmentalVariable() throws Exception {
    setEnvVar("OPERA_ARGS", "-foo -bar baz -bah=abc");
    OperaRunnerSettings envVarSettings = new OperaRunnerSettings();
    assertNotNull(envVarSettings);
    assertNotNull(envVarSettings.getArguments());
    assertEquals(3, envVarSettings.getArguments().getArguments().size());
  }

  @Test
  public void testBinaryString() {
    settings.setBinary(fakeBinary.getAbsolutePath());
    assertNotNull(settings.getBinary());
    assertEquals(fakeBinary.getAbsolutePath(), settings.getBinary().getAbsolutePath());
  }

  @Test(expected = OperaRunnerException.class)
  public void testInvalidBinaryString() {
    String binary = "/some/binary";
    settings.setBinary(binary);
  }

  @Test
  public void testBinaryFileObject() {
    settings.setBinary(fakeBinary);
    assertNotNull(settings.getBinary());
    assertEquals(fakeBinary, settings.getBinary());
  }

  @Test
  public void testGetDisplay() {
    assertNull(settings.getDisplay());
  }

  @Test
  public void testSetDisplay() {
    Integer display = 8;
    Exception unsupportedOperationException = null;

    try {
      settings.setDisplay(display);
    } catch (UnsupportedOperationException e) {
      unsupportedOperationException = e;
    }

    if (Platform.getCurrent() == Platform.WINDOWS) {
      assertNotNull(unsupportedOperationException);
    } else if (Platform.getCurrent() == Platform.LINUX ||
               Platform.getCurrent() == Platform.UNIX) {
      assertNull(unsupportedOperationException);
      assertEquals(display, settings.getDisplay());
    }
  }

  @Test
  public void testProduct() {
    settings.setProduct(OperaProduct.CORE);
    assertEquals(OperaProduct.CORE, settings.getProduct());
  }

  // use this profile
  @Test
  public void testSetProfileWithValidString() {
    settings.setProfile(profile);
    runner = new TestOperaRunner(settings);
    assertEquals(profile,
                 runner.getSettings().getArguments().getArguments().get(0).getValue());  // 1
    assertTrue(runner.getSettings().getProfile().getDirectory().exists());
    assertEquals(46, runner.getSettings().getProfile().preferences().size());
  }

  // use this profile, but check for invalid directory
  @Test(expected = WebDriverException.class)
  public void testSetProfileWithInvalidString() {
    if (Platform.getCurrent().is(Platform.WINDOWS)) {
      settings.setProfile("Ø:/this/does/not/exist");
    } else {
      settings.setProfile("/this/does/not/exist");
    }
    runner = new TestOperaRunner(settings);
  }

  // null, use random profile
  @Test
  public void testSetProfileRandom() {
    settings.setProfile((String) null);
    runner = new TestOperaRunner(settings);
    assertTrue(runner.getSettings().getProfile().getDirectory().exists());
  }

  // "" (empty string), use ~/.autotest
  @Test
  public void testSetProfileWithEmptyString() {
    settings.setProfile("");
    runner = new TestOperaRunner(settings);
    assertTrue(runner.getSettings().getProfile().getDirectory().exists());
    assertTrue(!runner.getSettings().getProfile().getDirectory().toString().contains("null"));
    // TODO(andreastt): Is there a good way to check that .autotest is used?
  }

  // use provided profile
  @Test
  public void testSetProfileWithProfile() {
    OperaProfile newProfile = new OperaProfile(profile);
    settings.setProfile(newProfile);
    runner = new TestOperaRunner(settings);
    assertTrue(runner.getSettings().getProfile().getDirectory().exists());
    assertEquals(46, runner.getSettings().getProfile().preferences().size());
  }

  @Test
  public void testNoQuitDefaultValue() {
    assertFalse(settings.getNoQuit());
  }

  @Test
  public void testNoQuitWithPositiveValue() {
    boolean noQuit = true;
    settings.setNoQuit(noQuit);
    assertEquals(noQuit, settings.getNoQuit());
  }

  @Test
  public void testNoQuitWithNegativeValue() {
    boolean noQuit = false;
    settings.setNoQuit(noQuit);
    assertEquals(noQuit, settings.getNoQuit());
  }

  @Test
  public void testGetHost() {
    assertEquals("127.0.0.1", settings.getHost());
  }

  @Test
  public void testSetHost() {
    String host = "1.2.3.4";
    settings.setHost(host);
    assertEquals(host, settings.getHost());
  }

  @Test
  public void testGetPort() {
    int
        userDefinedPort =
        (Integer) OperaDriver.getDefaultCapabilities().getCapability(OperaDriver.PORT);

    if (userDefinedPort == 0) {
      assertTrue(settings.getPort() > 0);
    } else if (userDefinedPort == -1) {
      assertEquals(userDefinedPort, (int) OperaIntervals.SERVER_PORT.getValue());
    }
  }

  @Test
  public void testSetPort() {
    Integer port = 1234;
    settings.setPort(port);
    assertEquals(port, settings.getPort());
  }

  @Test
  public void testSetPortToUseRandomPort() {
    // 0 means using a random port
    settings.setPort(0);
    assertTrue(settings.getPort() > 0);
  }

  @Test
  public void testSetPortToUseDefaultPort() {
    // -1 means using the default port (7001)
    settings.setPort(-1);
    assertEquals(OperaIntervals.SERVER_PORT.getValue(), (int) settings.getPort());
  }

  @Test
  public void testArguments() {
    settings.setArguments(new OperaCoreArguments());
    assertNotNull(settings.getArguments());
  }

  @Test
  public void testLoggingLevel() {
    settings.setLoggingLevel(Level.CONFIG);
    assertEquals(Level.CONFIG, settings.getLoggingLevel());
  }

  @Test
  public void testDefaultSettings() {
    OperaRunnerSettings defaultSettings = OperaRunnerSettings.getDefaultSettings();
    assertNotNull(defaultSettings);
    assertEquals(OperaProduct.DESKTOP, defaultSettings.getProduct());
    assertTrue("default port should be greater than 0", defaultSettings.getPort() > 0);
    assertTrue(defaultSettings.getArguments() instanceof OperaCoreArguments);
  }

  /**
   * Massive hack to set the environment variables inside this JVM.  Used to test if OperaPaths is
   * checking the environment variables.
   *
   * http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java/496849#496849
   *
   * @param key   the new environment variable's identifier
   * @param value the new environment variable's value
   * @throws IllegalAccessException foo
   * @throws NoSuchFieldException foo
   */
  @SuppressWarnings("unchecked")
  private static void setEnvVar(String key, String value)
      throws IllegalAccessException, NoSuchFieldException {
    Class<?>[] classes = Collections.class.getDeclaredClasses();
    Map<String, String> env = System.getenv();
    for (Class<?> cl : classes) {
      if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);
        Object obj = field.get(env);
        Map<String, String> map = (Map<String, String>) obj;
        map.put(key, value);
      }
    }
  }

  public class TestOperaRunner extends OperaRunner {

    public TestOperaRunner() {
      super();
    }

    public TestOperaRunner(OperaRunnerSettings settings) {
      super(settings);
    }

    public OperaRunnerSettings getSettings() {
      return this.settings;
    }

  }

}