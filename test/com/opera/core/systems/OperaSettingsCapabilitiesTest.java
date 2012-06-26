package com.opera.core.systems;

import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaSettings.Capability.ARGUMENTS;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.OperaSettings.Capability.BACKEND;
import static com.opera.core.systems.OperaSettings.Capability.BINARY;
import static com.opera.core.systems.OperaSettings.Capability.DETACH;
import static com.opera.core.systems.OperaSettings.Capability.DISPLAY;
import static com.opera.core.systems.OperaSettings.Capability.HOST;
import static com.opera.core.systems.OperaSettings.Capability.LAUNCHER;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_FILE;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_LEVEL;
import static com.opera.core.systems.OperaSettings.Capability.NO_QUIT;
import static com.opera.core.systems.OperaSettings.Capability.NO_RESTART;
import static com.opera.core.systems.OperaSettings.Capability.OPERAIDLE;
import static com.opera.core.systems.OperaSettings.Capability.PORT;
import static com.opera.core.systems.OperaSettings.Capability.PRODUCT;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static com.opera.core.systems.scope.internal.OperaFlags.SERVER_DEFAULT_PORT;
import static com.opera.core.systems.scope.internal.OperaFlags.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaFlags.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@NoDriver
public class OperaSettingsCapabilitiesTest extends OperaDriverTestCase {

  @Rule
  public TemporaryFolder tmp;

  @Before
  public void beforeEach() {
    tmp = new TemporaryFolder();
  }

  @Test
  public void loggingLevelHasLoggingLevelInfoAsDefaultValue() {
    assertEquals(Level.INFO, LOGGING_LEVEL.getDefaultValue());
  }

  @Test
  public void loggingLevelSanitizeNull() {
    assertNull(LOGGING_LEVEL.sanitize(null));
  }

  @Test
  public void loggingLevelSanitizeValidStringLevelUpperCase() {
    assertEquals(Level.WARNING, LOGGING_LEVEL.sanitize(Level.WARNING.toString()));
  }

  @Test
  public void loggingLevelSanitizeValidStringLevelLowerCase() {
    assertEquals(Level.WARNING, LOGGING_LEVEL.sanitize(Level.WARNING.toString().toLowerCase()));
  }

  @Test
  public void loggingLevelSanitizeValidStringLevelMixedCase() {
    assertEquals(Level.WARNING, LOGGING_LEVEL.sanitize("wArNiNg"));
  }

  @Test
  public void loggingLevelSanitizeLevelObject() {
    assertEquals(Level.CONFIG, LOGGING_LEVEL.sanitize(Level.CONFIG));
  }

  @Test
  public void loggingFileHasNullAsDefaultValue() {
    assertNull(LOGGING_FILE.getDefaultValue());
  }

  @Test
  public void loggingFileSanitizeNull() {
    assertNull(LOGGING_FILE.sanitize(null));
  }

  @Test
  public void loggingFileSanitizeValidStringPath() throws IOException {
    File logFile = tmp.newFile();
    Object sanitizedLogFile = LOGGING_FILE.sanitize(logFile.getPath());
    assertTrue("Expected log file to be of type File, got: " +
               sanitizedLogFile.getClass().getName(),
               sanitizedLogFile instanceof File);
    assertEquals(logFile.getAbsolutePath(), ((File) sanitizedLogFile).getAbsolutePath());
  }

  @Test
  public void loggingFileSanitizeInvalidStringPath() throws IOException {
    Object sanitizedLogFile = LOGGING_FILE.sanitize(resources.fakeFile().getPath());
    assertTrue("Expected log file to by of type File, got: " +
               sanitizedLogFile.getClass().getName(),
               sanitizedLogFile instanceof File);
    assertEquals(resources.fakeFile().getAbsolutePath(),
                 ((File) sanitizedLogFile).getAbsolutePath());
  }

  @Test
  public void binaryHasNullAsDefaultValue() {
    assertNotNull(BINARY.getDefaultValue());
  }

  @Test
  public void binarySanitizeNull() {
    assertEquals(new File(OperaPaths.operaPath()).getAbsolutePath(),
                 ((File) BINARY.sanitize(null)).getAbsolutePath());
  }

  @Test
  public void binarySanitizeValidStringPath() {
    assertEquals(new File(OperaPaths.operaPath()).getAbsolutePath(),
                 ((File) BINARY.sanitize(OperaPaths.operaPath())).getAbsolutePath());
  }

  @Test
  public void binarySanitizeInvalidStringPath() {
    assertEquals(resources.fakeFile().getAbsolutePath(),
                 ((File) BINARY.sanitize(resources.fakeFile().getAbsolutePath()))
                     .getAbsolutePath());
  }

  @Test
  public void argumentsHasOperaArgumentsObjectAsDefaultValue() {
    assertTrue(ARGUMENTS.getDefaultValue() instanceof OperaArguments);
  }

  @Test
  public void argumentsSanitizeNull() {
    Object arguments = ARGUMENTS.sanitize(null);
    assertNotNull(arguments);
    assertTrue(arguments instanceof OperaArguments);
    assertEquals(new OperaArguments().size(), ((OperaArguments) arguments).size());
  }

  @Test
  public void argumentsSanitizeStringList() {
    OperaArguments reference = new OperaArguments("-foo bar -bah");
    Object arguments = ARGUMENTS.sanitize(reference.toString());
    assertTrue(arguments instanceof OperaArguments);
    assertEquals(reference.size(), ((OperaArguments) arguments).size());
    assertEquals(reference.getArgumentsAsStringList(),
                 ((OperaArguments) arguments).getArgumentsAsStringList());
  }

  @Test
  public void argumentsSanitizeOperaArgumentsObject() {
    OperaArguments reference = new OperaArguments("-foo bar -bah");
    Object arguments = ARGUMENTS.sanitize(reference);
    assertTrue(arguments instanceof OperaArguments);
    assertEquals(reference.size(), ((OperaArguments) arguments).size());
  }

  @Test
  public void hostHasNonLoopbackAddressAsDefaultValue() {
    assertEquals("127.0.0.1", HOST.getDefaultValue());
  }

  @Test
  public void hostSanitizeNull() {
    assertNull(HOST.sanitize(null));
  }

  @Test
  public void hostSanitizeString() {
    assertEquals("1.2.3.4", HOST.sanitize("1.2.3.4"));
  }

  @Test
  public void hostSanitizeInteger() {
    assertEquals("1234", HOST.sanitize(1234));
  }

  @Test
  public void portHasRandomServerPortAsDefaultValue() {
    int port = (Integer) PORT.getDefaultValue();

    // In the highly unlikely case that it assigns the random port to 7001...
    if (port == SERVER_DEFAULT_PORT) {
      return;
    }

    assertNotSame((int) SERVER_DEFAULT_PORT, PORT.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void portSanitizeNull() {
    PORT.sanitize(null);
  }

  @Test
  public void portSanitizeString() {
    assertEquals(1234, PORT.sanitize("1234"));
  }

  @Test
  public void portSanitizeInteger() {
    assertEquals(6543, PORT.sanitize(6543));
  }

  @Test
  public void portSanitizeRandomPortIdentifier() {
    assertNotSame(SERVER_RANDOM_PORT_IDENTIFIER,
                  PORT.sanitize(SERVER_RANDOM_PORT_IDENTIFIER));
  }

  @Test
  public void portSanitizeDefaultPortIdentifier() {
    assertEquals(SERVER_DEFAULT_PORT,
                 PORT.sanitize(SERVER_DEFAULT_PORT_IDENTIFIER));
  }

  @Test
  public void launcherHasDefaultLocationAsDefaultValue() {
    assertNotNull(LAUNCHER.getDefaultValue());
    assertEquals(OperaLauncherRunner.launcherDefaultLocation(), LAUNCHER.getDefaultValue());
  }

  @Test
  public void launcherSanitizeNull() {
    assertNull(LAUNCHER.sanitize(null));
  }

  @Test
  public void launcherSanitizeStringPath() {
    assertEquals(resources.executableBinary().getAbsolutePath(),
                 ((File) LAUNCHER.sanitize(resources.executableBinary().getAbsolutePath()))
                     .getAbsolutePath());
  }

  @Test
  public void launcherSanitizeFile() {
    assertEquals(resources.executableBinary().getAbsolutePath(),
                 ((File) LAUNCHER.sanitize(resources.executableBinary())).getAbsolutePath());
  }

  @Test
  public void profileHasOperaProfileObjectAsDefaultValue() {
    assertTrue(PROFILE.getDefaultValue() instanceof OperaProfile);
  }

  @Test
  public void profileSanitizeStringPath() throws IOException {
    File path = tmp.newFolder();
    Object profile = PROFILE.sanitize(path.getPath());
    assertTrue(profile instanceof OperaProfile);
    assertEquals(path.getAbsolutePath(),
                 ((OperaProfile) profile).getDirectory().getAbsolutePath());
  }

  @Test
  public void profileSanitizeOperaProfileInstance() {
    OperaProfile reference = new OperaProfile();
    Object profile = PROFILE.sanitize(reference);
    assertTrue(profile instanceof OperaProfile);
    assertEquals(reference.getDirectory().getAbsolutePath(),
                 ((OperaProfile) profile).getDirectory().getAbsolutePath());
  }

  @Test
  public void profileSanitizeNull() {
    assertTrue(PROFILE.sanitize(null) instanceof OperaProfile);
  }

  @Test
  public void profileSanitizeEmptyString() {
    assertTrue(PROFILE.sanitize("") instanceof OperaProfile);
    assertThat(((OperaProfile) PROFILE.sanitize("")).getDirectory().getPath(),
               containsString("autotest"));
  }

  @Test
  public void operaIdleHasFalseAsDefaultValue() {
    assertTrue(OPERAIDLE.getDefaultValue() instanceof Boolean);
    assertFalse((Boolean) OPERAIDLE.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void operaIdleSanitizeNull() {
    OPERAIDLE.sanitize(null);
  }

  @Test
  public void operaIdleSanitizeBoolean() {
    assertTrue(OPERAIDLE.sanitize(true) instanceof Boolean);
    assertTrue((Boolean) OPERAIDLE.sanitize(true));
  }

  @Test
  public void operaIdleSanitizeString() {
    assertTrue((Boolean) OPERAIDLE.sanitize("true"));
  }

  @Test
  public void operaIdleSanitizeInteger() {
    assertTrue((Boolean) OPERAIDLE.sanitize(1));
  }

  @Test
  public void displayHasNullAsDefaultValue() {
    assertNull(DISPLAY.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void displaySanitizeNull() {
    DISPLAY.sanitize(null);
  }

  @Test
  public void displaySanitizeString() {
    assertEquals(7, DISPLAY.sanitize("7"));
  }

  @Test
  public void displaySanitizeInteger() {
    assertEquals(7, DISPLAY.sanitize(7));
  }

  @Test
  public void autostartHasTrueAsDefaultValue() {
    assertTrue(AUTOSTART.getDefaultValue() instanceof Boolean);
    assertTrue((Boolean) AUTOSTART.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void autostartSanitizeNull() {
    AUTOSTART.sanitize(null);
  }

  @Test
  public void autostartSanitizeBoolean() {
    assertTrue(AUTOSTART.sanitize(true) instanceof Boolean);
    assertTrue((Boolean) AUTOSTART.sanitize(true));
  }

  @Test
  public void autostartSanitizeString() {
    assertTrue((Boolean) AUTOSTART.sanitize("true"));
  }

  @Test
  public void autostartSanitizeInteger() {
    assertTrue((Boolean) AUTOSTART.sanitize(1));
  }

  @Test
  public void noRestartHasFalseAsDefaultValue() {
    assertTrue(NO_RESTART.getDefaultValue() instanceof Boolean);
    assertFalse((Boolean) NO_RESTART.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void noRestartSanitizeNull() {
    NO_RESTART.sanitize(null);
  }

  @Test
  public void noRestartSanitizeBoolean() {
    assertTrue(NO_RESTART.sanitize(true) instanceof Boolean);
    assertTrue((Boolean) NO_RESTART.sanitize(true));
  }

  @Test
  public void noRestartSanitizeString() {
    assertTrue((Boolean) NO_RESTART.sanitize("true"));
  }

  @Test
  public void noRestartSanitizeInteger() {
    assertTrue((Boolean) NO_RESTART.sanitize(1));
  }

  @Test
  public void noQuitHasFalseAsDefaultValue() {
    assertTrue(NO_QUIT.getDefaultValue() instanceof Boolean);
    assertFalse((Boolean) NO_QUIT.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void noQuitSanitizeNull() {
    NO_QUIT.sanitize(null);
  }

  @Test
  public void noQuitSanitizeBoolean() {
    assertTrue(NO_QUIT.sanitize(true) instanceof Boolean);
    assertTrue((Boolean) NO_QUIT.sanitize(true));
  }

  @Test
  public void noQuitSanitizeString() {
    assertTrue((Boolean) NO_QUIT.sanitize("true"));
  }

  @Test
  public void noQuitSanitizeInteger() {
    assertTrue((Boolean) NO_QUIT.sanitize(1));
  }

  @Test
  public void detachHasFalseAsDefaultValue() {
    assertTrue(DETACH.getDefaultValue() instanceof Boolean);
    assertFalse((Boolean) DETACH.getDefaultValue());
  }

  @Test(expected = NullPointerException.class)
  public void detachSanitizeNull() {
    DETACH.sanitize(null);
  }

  @Test
  public void detachSanitizeBoolean() {
    assertTrue(DETACH.sanitize(true) instanceof Boolean);
    assertTrue((Boolean) DETACH.sanitize(true));
  }

  @Test
  public void detachSanitizeString() {
    assertTrue((Boolean) NO_QUIT.sanitize("true"));
  }

  @Test
  public void detachSanitizeInteger() {
    assertTrue((Boolean) NO_QUIT.sanitize("true"));
  }

  @Test
  public void productHasOperaProductDesktopAsDefaultValue() {
    assertEquals(DESKTOP, PRODUCT.getDefaultValue());
  }

  @Test
  public void productSanitizeNull() {
    assertNull(PRODUCT.sanitize(null));
  }

  @Test
  public void productSanitizeValidString() {
    Object product = PRODUCT.sanitize("mobile");
    assertTrue(product instanceof OperaProduct);
    assertEquals(OperaProduct.MOBILE, product);
  }

  @Test(expected = WebDriverException.class)
  public void productSanitizeInvalidString() {
    PRODUCT.sanitize("hoobaflooba");
  }

  @Test
  public void productSanitizeOperaProductObject() {
    assertEquals(OperaProduct.MOBILE, PRODUCT.sanitize(OperaProduct.MOBILE));
  }

  @Test
  public void backendHasStringSoftwareAsDefaultValue() {
    assertEquals("software", BACKEND.getDefaultValue());
  }

  @Test
  public void backendSanitizeNull() {
    assertNull(BACKEND.sanitize(null));
  }

  @Test
  public void backendSanitizeString() {
    assertEquals("backend", BACKEND.sanitize("backend"));
  }

  @Test
  public void constructionOfSingleWord() {
    assertEquals("binary", BINARY.getIdentifier());
    assertEquals("opera.binary", BINARY.getCapability());
  }

  @Test
  public void constructionOfMultipleWords() {
    assertEquals("noRestart", NO_RESTART.getIdentifier());
    assertEquals("opera.no_restart", NO_RESTART.getCapability());
  }

  @Test
  public void constructionWithOverride() {
    assertEquals("operaidle", OPERAIDLE.getIdentifier());
    assertEquals("opera.idle", OPERAIDLE.getCapability());
  }

  @Test
  public void constructionWithHierarchicalOverride() {
    assertEquals("loggingLevel", LOGGING_LEVEL.getIdentifier());
    assertEquals("opera.logging.level", LOGGING_LEVEL.getCapability());
  }

  @Test
  public void stringRepresentation() {
    assertNotNull(LOGGING_LEVEL.toString());
  }

}