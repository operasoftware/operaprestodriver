/*
Copyright 2012 Opera Software ASA

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
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaSettings.Capability.ARGUMENTS;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.OperaSettings.Capability.BACKEND;
import static com.opera.core.systems.OperaSettings.Capability.BINARY;
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
import static com.opera.core.systems.runner.launcher.OperaLauncherRunner.LAUNCHER_ENV_VAR;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_PORT;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.openqa.selenium.Platform.LINUX;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@NoDriver
@RunWith(Enclosed.class)
public class OperaSettingsTest extends OperaDriverTestCase {

  public static class Capability {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

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
    public void loggingLevelSanitizeValidStringLevelMixedCase() {
      assertEquals(Level.WARNING, LOGGING_LEVEL.sanitize("wArNiNg"));
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
      assertTrue(LOGGING_FILE.sanitize(logFile.getPath()) instanceof File);
      assertEquals(logFile.getAbsolutePath(),
                   ((File) LOGGING_FILE.sanitize(logFile.getAbsolutePath())).getAbsolutePath());
    }

    @Test
    public void loggingFileSanitizeInvalidStringPath() throws IOException {
      assertTrue(LOGGING_FILE.sanitize(resources.fakeFile().getPath()) instanceof File);
      assertEquals(resources.fakeFile().getAbsolutePath(),
                   ((File) LOGGING_FILE.sanitize(resources.fakeFile().getAbsolutePath()))
                       .getAbsolutePath());
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
      if (port == SERVER_PORT.getValue()) {
        return;
      }

      assertNotSame((int) SERVER_PORT.getValue(), PORT.getDefaultValue());
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
      assertNotSame((int) SERVER_RANDOM_PORT_IDENTIFIER.getValue(),
                    PORT.sanitize(SERVER_RANDOM_PORT_IDENTIFIER.getValue()));
    }

    @Test
    public void portSanitizeDefaultPortIdentifier() {
      assertEquals((int) SERVER_PORT.getValue(),
                   PORT.sanitize(SERVER_DEFAULT_PORT_IDENTIFIER.getValue()));
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
      assertTrue((Boolean) AUTOSTART.sanitize(true));
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

  public static class Options {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    public OperaSettings settings;

    @Before
    public void beforeEach() {
      settings = new OperaSettings();
    }

    @After
    public void afterEach() {
      environment.unset(LAUNCHER_ENV_VAR);
    }

    @Test
    public void construction() {
      assertNotNull(settings);
    }

    @Test
    public void loggingLevelIsInfo() {
      assertEquals(Level.INFO, settings.logging().getLevel());
    }

    @Test
    public void loggingLevelCanBeSet() {
      settings.logging().setLevel(Level.WARNING);
      assertEquals(Level.WARNING, settings.logging().getLevel());
    }

    @Test
    public void loggingFileIsNull() {
      assertNull(settings.logging().getFile());
    }

    @Test
    public void loggingFileCanBeSet() throws IOException {
      settings.logging().setFile(resources.fakeFile());
      assertEquals(resources.fakeFile(), settings.logging().getFile());
    }

    @Test
    public void binaryDefaultOpera() {
      assertNotNull(settings.getBinary());
      assertTrue(settings.getBinary().exists());
    }

    @Test
    public void binaryCanBeSet() {
      settings.setBinary(resources.fakeFile());
      assertEquals(resources.fakeFile(), settings.getBinary());
    }

    @Test
    public void argumentsIsOperaArgumentsObject() {
      assertNotNull(settings.arguments());
    }

    @Test
    public void hostIsNonLoopbackAddress() {
      assertEquals("127.0.0.1", settings.getHost());
    }

    @Test
    public void hostCanBeSet() {
      settings.setHost("1.2.3.4");
      assertEquals("1.2.3.4", settings.getHost());
    }

    @Test
    public void portIsRandom() {
      // Of course, it might still happen that it assigns it to 7001...
      assertNotSame((int) SERVER_PORT.getValue(), settings.getPort());
    }

    @Test
    public void portCanBeSet() {
      settings.setPort(1234);
      assertEquals(1234, settings.getPort());
    }

    @Test
    public void portSetToRandomIdentifier() {
      settings.setPort((int) SERVER_RANDOM_PORT_IDENTIFIER.getValue());
      assertNotSame((int) SERVER_PORT.getValue(), settings.getPort());
    }

    @Test
    public void portSetToDefaultIdentifier() {
      settings.setPort((int) SERVER_DEFAULT_PORT_IDENTIFIER.getValue());
      assertEquals((int) SERVER_PORT.getValue(), settings.getPort());
    }

    @Test
    public void launcherReturnsLauncherLocationByDefault() {
      assertNotNull(settings.getLauncher());
      assertEquals(OperaLauncherRunner.launcherDefaultLocation(), settings.getLauncher());
    }

    @Test
    public void launcherReturnsEnvironmentalLauncherIfSet() {
      environment.set(LAUNCHER_ENV_VAR, resources.fakeFile().getPath());
      assertEquals(resources.fakeFile(), new OperaSettings().getLauncher());
    }

    @Test
    public void launcherPrioritizesEnvironmentalLauncher() throws IOException {
      environment.set(LAUNCHER_ENV_VAR, resources.fakeFile().getPath());
      OperaSettings settings = new OperaSettings();
      settings.setLauncher(resources.executableBinary());
      assertEquals(resources.fakeFile(), settings.getLauncher());
    }

    @Test
    public void launcherCanBeSet() throws IOException {
      settings.setLauncher(resources.executableBinary());
      assertEquals(resources.executableBinary(), settings.getLauncher());
    }

    @Test(expected = IOException.class)
    public void launcherThrowsExceptionIfInvalid() throws IOException {
      settings.setLauncher(resources.fakeFile());
    }

    @Test
    public void profileIsOperaProfileObject() {
      assertNotNull(settings.profile());
    }

    @Test
    public void profileCanBeSet() {
      OperaProfile profile = new OperaProfile();
      settings.setProfile(profile);
      assertEquals(profile, settings.profile());
    }

    @Test
    public void profileCanBeSetUsingString() {
      File profileDirectory = tmp.newFolder("profile");
      settings.setProfile(profileDirectory.getPath());
      assertEquals(profileDirectory.getPath(), settings.profile().getDirectory().getPath());
    }

    @Test
    public void profileCanBeSetUsingNull() {
      settings.setProfile((String) null);
      assertNotNull(settings.profile());
    }

    @Test
    public void profileCanBeSetUsingEmptyString() {
      settings.setProfile("");
      assertFalse(settings.supportsPd());
      assertNotNull(settings.profile());
    }

    @Test
    public void idleIsFalse() {
      assertEquals(false, settings.useIdle());
    }

    @Test
    public void idleCanBeSet() {
      settings.setIdle(true);
      assertEquals(true, settings.useIdle());
    }

    @Test
    public void displayIsNull() {
      assertNull(settings.getDisplay());
    }

    @Test
    public void displayCanBeSetOnLinuxOnly() {
      if (Platform.getCurrent().is(LINUX)) {
        settings.setDisplay(7);
      } else {
        try {
          settings.setDisplay(7);
        } catch (UnsupportedOperationException e) {
          return;
        }

        fail("Non-Linux platforms are not meant to support setting display");
      }
    }

    @Test
    public void autostartIsTrue() {
      assertTrue(settings.autostart());
    }

    @Test
    public void autostartCanBeSet() {
      settings.autostart(false);
      assertFalse(settings.autostart());
    }

    @Test
    public void noRestartIsFalse() {
      assertFalse(settings.noRestart());
    }

    @Test
    public void noRestartCanBeSet() {
      settings.noRestart(true);
      assertTrue(settings.noRestart());
    }

    @Test
    public void noQuitIsFalse() {
      assertFalse(settings.noQuit());
    }

    @Test
    public void noQuitCanBeSet() {
      settings.noQuit(true);
      assertTrue(settings.noQuit());
    }

    @Test
    public void productIsDesktop() {
      assertEquals(DESKTOP, settings.getProduct());
    }

    @Test
    public void productCanBeSet() {
      settings.setProduct(CORE);
      assertEquals(CORE, settings.getProduct());
    }

    @Test
    public void backendIsSoftware() {
      assertEquals("software", settings.getBackend());
    }

    @Test
    public void backendCanBeSet() {
      settings.setBackend("hardware");
      assertEquals("hardware", settings.getBackend());
    }

    @Test
    public void supportsDebugProxyIsTrueByDefault() {
      assertTrue(settings.supportsDebugProxy());
    }

    @Test
    public void supportsDebugProxyIsFalseIfDefaultServerPort() {
      settings.setPort((int) SERVER_DEFAULT_PORT_IDENTIFIER.getValue());
      assertFalse(settings.supportsDebugProxy());
    }

    @Test
    public void supportsPdIsTrueByDefault() {
      assertTrue(settings.supportsPd());
    }

    @Test
    public void supportsPdIsFalseIfProfileIsEmptyString() {
      settings.setProfile("");
      assertFalse(settings.supportsPd());
    }

    @Test
    public void supportsPdIsFalseIfProductIsCore() {
      settings.setProduct(CORE);
      assertFalse(settings.supportsPd());
    }

    @Test
    public void supportsPdIsFalseIfProductIsCoreAndProfileIsSet() {
      settings.setProduct(CORE);
      settings.setProfile(new OperaProfile());
      assertFalse(settings.supportsPd());
    }

    @Test
    public void canBeConvertedToCapabilities() {
      Capabilities capabilities = settings.toCapabilities();

      assertNotNull(capabilities);
      assertTrue(capabilities.asMap().size() > 0);
      assertEquals(true, capabilities.getCapability(AUTOSTART.getCapability()));
    }

    @Test
    public void surplusCapabilitiesAreIncludedWhenConvertedToCapabilities() {
      DesiredCapabilities capabilities = new DesiredCapabilities();
      capabilities.setCapability("foo", "bar");

      settings.merge(capabilities);

      for (Map.Entry<String, ?> capability : settings.toCapabilities().asMap().entrySet()) {
        if (capability.getKey().equals("foo") && capability.getValue().equals("bar")) {
          return;
        }
      }

      fail("Surplus capabilities not included in OperaSettings#toCapabilities()");
    }

    @Test
    public void toJsonConvertsBooleansToIntegers() throws IOException, JSONException {
      assertEquals(1, settings.toJson().get("autostart"));
    }

    @Test
    public void toJsonConvertsProfileToJson() throws IOException, JSONException {
      assertFalse(settings.toJson().get("profile") instanceof OperaProfile);
    }

    @Test
    public void toJsonConvertsArgumentsToJson() throws IOException, JSONException {
      assertFalse(settings.toJson().get("arguments") instanceof OperaArguments);
    }

    @Test
    public void toJsonConvertsEverythingElseToStrings() throws IOException, JSONException {
      assertEquals("127.0.0.1", settings.toJson().get("host"));
    }

    @Test
    public void stringRepresentation() {
      assertNotNull(settings.toString());
    }

  }

}