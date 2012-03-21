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

import java.io.File;
import java.io.IOException;
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
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_PORT;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openqa.selenium.Platform.LINUX;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@NoDriver
@RunWith(Enclosed.class)
public class OperaSettingsTest extends OperaDriverTestCase {

  public static class Capability {

    @Test
    public void loggingLevelHasLoggingLevelInfoAsDefaultValue() {
      assertEquals(Level.INFO, LOGGING_LEVEL.getDefaultValue());
    }

    @Test
    public void loggingFileHasNullAsDefaultValue() {
      assertNull(LOGGING_FILE.getDefaultValue());
    }

    @Test
    public void binaryHasNullAsDefaultValue() {
      assertNull(BINARY.getDefaultValue());
    }

    @Test
    public void argumentsHasOperaArgumentsObjectAsDefaultValue() {
      assertTrue(ARGUMENTS.getDefaultValue() instanceof OperaArguments);
    }

    @Test
    public void hostHasNonLoopbackAddressAsDefaultValue() {
      assertEquals("127.0.0.1", HOST.getDefaultValue());
    }

    @Test
    public void portHasRandomServerPortAsDefaultValue() {
      assertEquals((int) SERVER_RANDOM_PORT_IDENTIFIER.getValue(), PORT.getDefaultValue());
    }

    @Test
    public void launcherHasNullAsDefaultValue() {
      assertNull(LAUNCHER.getDefaultValue());
    }

    @Test
    public void profileHasOperaProfileObjectAsDefaultValue() {
      assertTrue(PROFILE.getDefaultValue() instanceof OperaProfile);
    }

    @Test
    public void operaIdleHasFalseAsDefaultValue() {
      assertTrue(OPERAIDLE.getDefaultValue() instanceof Boolean);
      assertFalse((Boolean) OPERAIDLE.getDefaultValue());
    }

    @Test
    public void displayHasNullAsDefaultValue() {
      assertNull(DISPLAY.getDefaultValue());
    }

    @Test
    public void autostartHasTrueAsDefaultValue() {
      assertTrue(AUTOSTART.getDefaultValue() instanceof Boolean);
      assertTrue((Boolean) AUTOSTART.getDefaultValue());
    }

    @Test
    public void noRestartHasFalseAsDefaultValue() {
      assertTrue(NO_RESTART.getDefaultValue() instanceof Boolean);
      assertFalse((Boolean) NO_RESTART.getDefaultValue());
    }

    @Test
    public void noQuitHasFalseAsDefaultValue() {
      assertTrue(NO_QUIT.getDefaultValue() instanceof Boolean);
      assertFalse((Boolean) NO_QUIT.getDefaultValue());
    }

    @Test
    public void productHasOperaProductDesktopAsDefaultValue() {
      assertEquals(DESKTOP, PRODUCT.getDefaultValue());
    }

    @Test
    public void backendHasStringSoftwareAsDefaultValue() {
      assertEquals("software", BACKEND.getDefaultValue());
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
      environment.set("OPERA_LAUNCHER", null);
    }

    @Test
    public void construction() {
      assertNotNull(settings);
    }

    @Test
    public void loggingLevelIsInfo() {
      assertEquals(Level.INFO, settings.logging().level());
    }

    @Test
    public void loggingLevelCanBeSet() {
      settings.logging().level(Level.WARNING);
      assertEquals(Level.WARNING, settings.logging().level());
    }

    @Test
    public void loggingFileIsNull() {
      assertNull(settings.logging().file());
    }

    @Test
    public void loggingFileCanBeSet() throws IOException {
      // TODO(andreastt): Replace with fake binary from Resources
      File logFile = tmp.newFile("logfile");
      settings.logging().file(logFile);
      assertEquals(logFile, settings.logging().file());
    }

    @Test
    public void binaryIsNull() {
      assertNull(settings.getBinary());
    }

    @Test
    public void binaryCanBeSet() {
      // TODO(andreastt): Replace with fake binary from Resources
      File binary = new File("nonexistent");
      settings.setBinary(binary);
      assertEquals(binary, settings.getBinary());
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
      assertNull(settings.getLauncher());
    }

    @Test
    public void launcherReturnsValueIfPreviouslySet() {
      settings.setLauncher(resources.fakeBinary());
      assertEquals(resources.fakeBinary(), settings.getLauncher());
    }

    @Test
    public void launcherReturnsEnvironmentalLauncherIfSet() {
      environment.set("OPERA_LAUNCHER", resources.fakeBinary().getPath());
      assertEquals(resources.fakeBinary(), settings.getLauncher());
    }

    @Test
    public void launcherPrioritizesEnvironmentalLauncher() {
      environment.set("OPERA_LAUNCHER", resources.fakeBinary().getPath());
      settings.setLauncher(new File("something/else"));
      assertEquals(resources.fakeBinary(), settings.getLauncher());
    }

    @Test
    public void launcherCanBeSet() {
      // TODO(andreastt): Replace with fake binary from Resources
      File launcher = new File("nonexistent");
      settings.setLauncher(launcher);
      assertEquals(launcher, settings.getLauncher());
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