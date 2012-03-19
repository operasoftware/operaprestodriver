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

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static com.opera.core.systems.OperaOptions.Capability.ARGUMENTS;
import static com.opera.core.systems.OperaOptions.Capability.AUTOSTART;
import static com.opera.core.systems.OperaOptions.Capability.BACKEND;
import static com.opera.core.systems.OperaOptions.Capability.BINARY;
import static com.opera.core.systems.OperaOptions.Capability.DISPLAY;
import static com.opera.core.systems.OperaOptions.Capability.HOST;
import static com.opera.core.systems.OperaOptions.Capability.LAUNCHER;
import static com.opera.core.systems.OperaOptions.Capability.LOGGING_FILE;
import static com.opera.core.systems.OperaOptions.Capability.LOGGING_LEVEL;
import static com.opera.core.systems.OperaOptions.Capability.NO_QUIT;
import static com.opera.core.systems.OperaOptions.Capability.NO_RESTART;
import static com.opera.core.systems.OperaOptions.Capability.OPERAIDLE;
import static com.opera.core.systems.OperaOptions.Capability.PORT;
import static com.opera.core.systems.OperaOptions.Capability.PRODUCT;
import static com.opera.core.systems.OperaOptions.Capability.PROFILE;
import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
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
@RunWith(Enclosed.class)
public class OperaOptionsTest {

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

    public OperaOptions options;

    @Before
    public void beforeEach() {
      options = new OperaOptions();
    }

    @Test
    public void construction() {
      assertNotNull(options);
    }

    @Test
    public void loggingLevelIsInfo() {
      assertEquals(Level.INFO, options.logging().level());
    }

    @Test
    public void loggingLevelCanBeSet() {
      options.logging().level(Level.WARNING);
      assertEquals(Level.WARNING, options.logging().level());
    }

    @Test
    public void loggingFileIsNull() {
      assertNull(options.logging().file());
    }

    @Test
    public void loggingFileCanBeSet() throws IOException {
      // TODO(andreastt): Replace with fake binary from Resources
      File logFile = tmp.newFile("logfile");
      options.logging().file(logFile);
      assertEquals(logFile, options.logging().file());
    }

    @Test
    public void binaryIsNull() {
      assertNull(options.getBinary());
    }

    @Test
    public void binaryCanBeSet() {
      // TODO(andreastt): Replace with fake binary from Resources
      File binary = new File("nonexistent");
      options.setBinary(binary);
      assertEquals(binary, options.getBinary());
    }

    @Test
    public void argumentsIsOperaArgumentsObject() {
      assertNotNull(options.arguments());
    }

    @Test
    public void hostIsNonLoopbackAddress() {
      assertEquals("127.0.0.1", options.getHost());
    }

    @Test
    public void hostCanBeSet() {
      options.setHost("1.2.3.4");
      assertEquals("1.2.3.4", options.getHost());
    }

    @Test
    public void portIsRandom() {
      // Of course, it might still happen that it assigns it to 7001...
      assertNotSame((int) SERVER_PORT.getValue(), options.getPort());
    }

    @Test
    public void portCanBeSet() {
      options.setPort(1234);
      assertEquals(1234, options.getPort());
    }

    @Test
    public void portSetToRandomIdentifier() {
      options.setPort((int) SERVER_RANDOM_PORT_IDENTIFIER.getValue());
      assertNotSame((int) SERVER_PORT.getValue(), options.getPort());
    }

    @Test
    public void portSetToDefaultIdentifier() {
      options.setPort((int) SERVER_DEFAULT_PORT_IDENTIFIER.getValue());
      assertEquals((int) SERVER_PORT.getValue(), options.getPort());
    }

    @Test
    public void launcherIsNull() {
      assertNull(options.getLauncher());
    }

    @Test
    public void launcherCanBeSet() {
      // TODO(andreastt): Replace with fake binary from Resources
      File launcher = new File("nonexistent");
      options.setLauncher(launcher);
      assertEquals(launcher, options.getLauncher());
    }

    @Test
    public void profileIsOperaProfileObject() {
      assertNotNull(options.profile());
    }

    @Test
    public void profileCanBeSet() {
      OperaProfile profile = new OperaProfile();
      options.setProfile(profile);
      assertEquals(profile, options.profile());
    }

    @Test
    public void profileCanBeSetUsingString() {
      File profileDirectory = tmp.newFolder("profile");
      options.setProfile(profileDirectory.getPath());
      assertEquals(profileDirectory.getPath(), options.profile().getDirectory().getPath());
    }

    @Test
    public void profileCanBeSetUsingNull() {
      options.setProfile((String) null);
      assertNotNull(options.profile());
    }

    @Test
    public void profileCanBeSetUsingEmptyString() {
      options.setProfile("");
      assertFalse(options.supportsPd());
      assertNotNull(options.profile());
    }

    @Test
    public void idleIsFalse() {
      assertEquals(false, options.useIdle());
    }

    @Test
    public void idleCanBeSet() {
      options.setIdle(true);
      assertEquals(true, options.useIdle());
    }

    @Test
    public void displayIsNull() {
      assertNull(options.getDisplay());
    }

    @Test
    public void displayCanBeSetOnLinuxOnly() {
      if (Platform.getCurrent().is(LINUX)) {
        options.setDisplay(7);
      } else {
        try {
          options.setDisplay(7);
        } catch (UnsupportedOperationException e) {
          return;
        }

        fail("Non-Linux platforms are not meant to support setting display");
      }
    }

    @Test
    public void autostartIsTrue() {
      assertTrue(options.autostart());
    }

    @Test
    public void autostartCanBeSet() {
      options.autostart(false);
      assertFalse(options.autostart());
    }

    @Test
    public void noRestartIsFalse() {
      assertFalse(options.noRestart());
    }

    @Test
    public void noRestartCanBeSet() {
      options.noRestart(true);
      assertTrue(options.noRestart());
    }

    @Test
    public void noQuitIsFalse() {
      assertFalse(options.noQuit());
    }

    @Test
    public void noQuitCanBeSet() {
      options.noQuit(true);
      assertTrue(options.noQuit());
    }

    @Test
    public void productIsDesktop() {
      assertEquals(DESKTOP, options.getProduct());
    }

    @Test
    public void productCanBeSet() {
      options.setProduct(CORE);
      assertEquals(CORE, options.getProduct());
    }

    @Test
    public void backendIsSoftware() {
      assertEquals("software", options.getBackend());
    }

    @Test
    public void backendCanBeSet() {
      options.setBackend("hardware");
      assertEquals("hardware", options.getBackend());
    }

    @Test
    public void supportsDebugProxyIsTrueByDefault() {
      assertTrue(options.supportsDebugProxy());
    }

    @Test
    public void supportsDebugProxyIsFalseIfDefaultServerPort() {
      options.setPort((int) SERVER_DEFAULT_PORT_IDENTIFIER.getValue());
      assertFalse(options.supportsDebugProxy());
    }

    @Test
    public void supportsPdIsTrueByDefault() {
      assertTrue(options.supportsPd());
    }

    @Test
    public void supportsPdIsFalseIfProfileIsEmptyString() {
      options.setProfile("");
      assertFalse(options.supportsPd());
    }

    @Test
    public void supportsPdIsFalseIfProductIsCore() {
      options.setProduct(CORE);
      assertFalse(options.supportsPd());
    }

    @Test
    public void supportsPdIsFalseIfProductIsCoreAndProfileIsSet() {
      options.setProduct(CORE);
      options.setProfile(new OperaProfile());
      assertFalse(options.supportsPd());
    }

    @Test
    public void canBeConvertedToCapabilities() {
      Capabilities capabilities = options.toCapabilities();

      assertNotNull(capabilities);
      assertTrue(capabilities.asMap().size() > 0);
      assertEquals(true, capabilities.getCapability(AUTOSTART.getCapability()));
    }

    @Test
    public void toJsonConvertsBooleansToIntegers() throws IOException, JSONException {
      assertEquals(1, options.toJson().get("autostart"));
    }

    @Test
    public void toJsonConvertsProfileToJson() throws IOException, JSONException {
      assertFalse(options.toJson().get("profile") instanceof OperaProfile);
    }

    @Test
    public void toJsonConvertsArgumentsToJson() throws IOException, JSONException {
      assertFalse(options.toJson().get("arguments") instanceof OperaArguments);
    }

    @Test
    public void toJsonConvertsEverythingElseToStrings() throws IOException, JSONException {
      assertEquals("127.0.0.1", options.toJson().get("host"));
    }

    @Test
    public void stringRepresentation() {
      assertNotNull(options.toString());
    }

  }

}