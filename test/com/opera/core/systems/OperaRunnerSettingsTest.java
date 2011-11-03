package com.opera.core.systems;

import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.OperaRunnerSettings;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OperaRunnerSettingsTest {

  private OperaRunnerSettings settings;
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
  public void beforeEach() throws Exception {
    // Make sure we always reset OPERA_ARGS.  Since the constructor of OperaRunnerSettings has
    // different behaviour depending on whether its set, it is going to impact the outcome of our
    // tests.
    setEnvVar("OPERA_ARGS", "");
    settings = new OperaRunnerSettings();
  }

  @Test
  public void testConstruction() {
    assertNotNull(settings);
    assertNotNull(settings.getArguments());
  }

  @Test
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
    String product = "core-gogi";
    settings.setProduct(product);
    assertEquals(product, settings.getProduct());
  }

  @Test
  public void testProfile() {
    String profile = "/path/that/does/not/exist";
    settings.setProfile(profile);
    assertEquals(profile, settings.getProfile());
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
    assertNull(defaultSettings.getProduct());
    assertEquals(new Integer(0), defaultSettings.getPort());
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

}